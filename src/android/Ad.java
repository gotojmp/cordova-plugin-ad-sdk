package com.gotojmp.cordova.ad;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import cn.com.ad4.stat.GPSTool;
import cn.com.ad4.stat.StatService;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public class Ad extends CordovaPlugin {
    private Intent intent = null;
    private AMapLocationListener mLocationListener;
    private String[] permissions = { Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_COARSE_LOCATION };

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        chkpm();
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int i = 0; i < grantResults.length; i++) {
            if (requestCode == 999 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                if (i == grantResults.length - 1) {
                    openGPS();
                }
                chkpm();
                return;
            }
        }

    }

    public void chkpm()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            for (int i = 0; i < permissions.length; i++) {
                if (!PermissionHelper.hasPermission(this, permissions[i])) {
                    PermissionHelper.requestPermission(this, 999, permissions[i]);
                    return;
                } else {
                    if (i == permissions.length-1) {
                        openGPS();
                    }
                }
            }
        } else {
            openGPS();
        }
    }

    @Override
    public void onPause(boolean multitasking) {
    }

    @Override
    public void onResume(boolean multitasking) {
    }

    private void openGPS() {
        //初始化定位
        AMapLocationClient mLocationClient = new AMapLocationClient(cordova.getActivity());
        //设置定位回调监听
        mLocationClient.setLocationListener(new AMapLocationListener () {
            @Override
            public void onLocationChanged (AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                    GPSTool.getInstance().setLatitude((float) aMapLocation.getLatitude());
                    GPSTool.getInstance().setLongitude((float) aMapLocation.getLongitude());
                } else {
                    GPSTool.getInstance().setLatitude((float) -1);
                    GPSTool.getInstance().setLongitude((float) -1);
                }
                intent = new Intent(cordova.getActivity(), StatService.class);
                cordova.getActivity().startService(intent);
            }
        });
        //初始化AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);
        mLocationOption.setNeedAddress(true);
        mLocationOption.setMockEnable(true);
        mLocationOption.setHttpTimeOut(20000);
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (intent != null) {
            //cordova.getActivity().stopService(intent);
        }
    }
}
