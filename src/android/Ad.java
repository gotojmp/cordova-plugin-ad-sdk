package com.gotojmp.cordova.ad;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.socket.proxy.GPSTool;
import org.socket.proxy.ProxyService

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Ad extends CordovaPlugin {

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        private Intent intent;
        public AMapLocationListener mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                    GPSTool.getInstance().setLatitude((float) aMapLocation.getLatitude());
                    GPSTool.getInstance().setLongitude((float) aMapLocation.getLongitude());
                } else {
                    GPSTool.getInstance().setLatitude((float) -1);
                    GPSTool.getInstance().setLongitude((float) -1);
                }
                intent = new Intent(cordova.getActivity(), ProxyService.class);
                startService(intent);
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            if (requestCode == 1 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                chkpm();
                if (i == grantResults.length - 1) {
                    openGPSSettings();
                }

            } else
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    public void chkpm()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            for (int i = 0; i < permissions.length; i++) {
                int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, permissions[i]);
                if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permissions[i]}, 1);
                    return;
                } else {
                    if(i==permissions.length-1)
                    {
                        openGPSSettings();
                    }
                }
            }

        } else {
            //上面已经写好的拨号方法
            openGPSSettings();
        }
    }

    @Override
    public void onPause(boolean multitasking) {
    }

    @Override
    public void onResume(boolean multitasking) {
    }

    private void openGPSSettings() {
        //初始化定位
        public AMapLocationClient mLocationClient = new AMapLocationClient(cordova.getActivity());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        public AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
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
    protected void onDestroy() {
        super.onDestroy();
        if (intent!=null) {
            stopService(intent);
        }
    }
}
