package com.gotojmp.cordova.adsdk;


import cn.com.ad4.quad.ad.QUAD;
import cn.com.ad4.quad.base.QuadNativeAd;
import cn.com.ad4.quad.listener.QuadBannerAdLoadListener;
import cn.com.ad4.quad.listener.QuadInterstitialAdLoadListener;
import cn.com.ad4.quad.listener.QuadNativeAdLoadListener;
import cn.com.ad4.quad.listener.QuadSplashAdLoadListener;
import cn.com.ad4.quad.loader.QuadBannerAdLoader;
import cn.com.ad4.quad.loader.QuadInterstitialAdLoader;
import cn.com.ad4.quad.loader.QuadNativeAdLoader;
import cn.com.ad4.quad.loader.QuadSplashAdLoader;
import cn.com.ad4.quad.view.QuadBannerAd;
import cn.com.ad4.quad.view.QuadInterstitialAd;
import cn.com.ad4.quad.view.QuadSplashAd;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

public class AdSdk extends CordovaPlugin {

    private FrameLayout splashLayout = null;
    private RelativeLayout adLayout = null;

    private QuadSplashAd quadSplashAdCtrl = null;
    private QuadSplashAd quadSplashAd = null;
    private QuadBannerAd quadBannerAd = null;
    private QuadInterstitialAd quadInterstitialAd = null;

    private int nid = 0;
    private HashMap<String, QuadNativeAd> nativeAds = new HashMap<String, QuadNativeAd>();


    @Override
    public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
        super.initialize(cordova, webView);

        final long tmStart =  System.currentTimeMillis();

        adLayout = new RelativeLayout(cordova.getActivity());
        splashLayout = new FrameLayout(cordova.getActivity());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cordova.getActivity().addContentView(adLayout, layoutParams);
        cordova.getActivity().addContentView(splashLayout, layoutParams);

        final String appkey = preferences.getString("ADSDK_APPKEY", "");
        final String pid = preferences.getString("ADSDK_SPLASH_PID", "");
        final boolean isTest = preferences.getBoolean("ADSDK_ISTEST", false);
        QUAD.initSdk(cordova.getActivity(), appkey, isTest, -1, -1);

        if (pid.equals("")) return;

        if (quadSplashAdCtrl == null) {
            quadSplashAdCtrl = new QuadSplashAd(cordova.getActivity());
        }

        final long delay = System.currentTimeMillis() - tmStart;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final QuadSplashAdLoader splashAdLoader = QUAD.getSplashAdLoader(cordova.getActivity(), pid, new QuadSplashAdLoadListener() {
                    @Override
                    public void onAdDismissed() {
                        if (quadSplashAd != null) {
                            ((ViewGroup) quadSplashAd.getParent()).removeView(quadSplashAd);
                        }
                    }

                    @Override
                    public void onAdReady(QuadSplashAd splashAd) {
                        final long tm = System.currentTimeMillis() - tmStart - delay;
                        if (tm < 4000) {
                            quadSplashAd = splashAd;
                            splashLayout.addView(quadSplashAd);
                        }
                    }

                    @Override
                    public void onAdShowed() {
                        (new Handler()).postDelayed(new Runnable() {
                            public void run() {
                                webView.postMessage("splashscreen", "hide");
                            }
                        }, delay);
                    }

                    @Override
                    public void onAdClick() {
                    }

                    @Override
                    public void onAdFailed(int code, String msg) {
                    }
                });
                if (splashAdLoader != null) {
                    splashAdLoader.loadAds();
                }
            }
        });
    }

    private int dpToPixels(int dipValue) {
        int value = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,
                (float) dipValue,
                cordova.getActivity().getResources().getDisplayMetrics()
        );

        return value;
    }

    private int getDeviceWidth() {
        try {
            DisplayMetrics displayMetrics = cordova.getActivity().getResources()
                    .getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            return width;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action the action to execute.
     * @param args JSONArry of arguments for the plugin.
     * @param callbackContext the callbackContext used when calling back into JavaScript.
     * @return A PluginResult object with a status and message.
     */
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("loadBannerAd")) {
            final String pid = args.getString(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final QuadBannerAdLoader adLoader = QUAD.getBannerAdLoader(cordova.getActivity(), pid, new QuadBannerAdLoadListener() {
                        @Override
                        public void onAdReady(QuadBannerAd bannerAd) {
                            if (quadBannerAd != null) {
                                ((ViewGroup)quadBannerAd.getParent()).removeView(quadBannerAd);
                            }
                            quadBannerAd = bannerAd;

                            TextView closeBtn = new TextView(cordova.getActivity());
                            RelativeLayout.LayoutParams lpClose = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lpClose.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            lpClose.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            closeBtn.setLayoutParams(lpClose);
                            closeBtn.setTextColor(0xffffffff);
                            closeBtn.setPadding(dpToPixels(5), dpToPixels(-10), dpToPixels(5), dpToPixels(-5));
                            closeBtn.setBackgroundColor(0x99999999);
                            closeBtn.setText("×");
                            closeBtn.setTextSize(30);
                            closeBtn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    ((ViewGroup)quadBannerAd.getParent()).removeView(quadBannerAd);
                                    quadBannerAd = null;
                                }
                            });

                            int w = quadBannerAd.getW();
                            int h = quadBannerAd.getH();
                            int sw = getDeviceWidth();
                            int bh = dpToPixels(100);
                            if (w > 0 && h > 0 && sw > 0) {
                                bh = h * sw / w;
                            }
                            RelativeLayout.LayoutParams lpBanner = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bh);
                            lpBanner.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            quadBannerAd.setLayoutParams(lpBanner);
                            quadBannerAd.addView(closeBtn);

                            adLayout.addView(quadBannerAd);
                        }

                        @Override
                        public void onAdShowed() {
                        }

                        @Override
                        public void onAdClick() {
                        }

                        @Override
                        public void onAdFailed(int code, String msg) {
                        }
                    });
                    if (adLoader != null) {
                        adLoader.loadAds();
                    }
                }
            });
            callbackContext.success(0);
        }
        else if (action.equals("loadInterstitialAd")) {
            final String pid = args.getString(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final QuadInterstitialAdLoader adLoader = QUAD.getQuadIntersititialAdLoader(cordova.getActivity(), pid, new QuadInterstitialAdLoadListener() {
                        @Override
                        public void onAdReady(QuadInterstitialAd interstitialAd) {
                            quadInterstitialAd = interstitialAd;
                            quadInterstitialAd.show();
                        }

                        @Override
                        public void onAdShowed() {
                        }

                        @Override
                        public void onAdClick() {
                        }

                        @Override
                        public void onAdFailed(int code, String msg) {
                        }

                        @Override
                        public void onAdClosed() {
                        }
                    });
                    if (adLoader != null) {
                        adLoader.loadAds();
                    }
                }
            });
            callbackContext.success(0);
        }
        else if (action.equals("loadNativeAd")) {
            final String pid = args.getString(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final QuadNativeAdLoader adLoader = QUAD.getNativeAdLoader(cordova.getActivity(), pid, new QuadNativeAdLoadListener() {
                        @Override
                        public void onAdLoadSuccess(QuadNativeAd nativeAd) {
                            nid++;
                            nativeAds.put("native-"+nid, nativeAd);
                            JSONObject json = nativeAd.getContent();
                            try {
                                json.put("__id", nid);
                            } catch (Exception e) {
                            }
                            callbackContext.success(json);
                        }

                        @Override
                        public void onAdLoadFailed(int code, String msg) {
                            callbackContext.error(code);
                        }
                    });
                    if (adLoader != null) {
                        adLoader.loadAds();
                    }
                }
            });
        }
        else if (action.equals("showNativeAd")) {
            final int id = args.getInt(0);
            if (id > 0) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        QuadNativeAd nativeAd = nativeAds.get("native-" + id);
                        if (nativeAd != null) {
                            try {
                                nativeAd.onAdShowed(webView.getView());
                            } catch (Exception e) {
                            }
                        }
                    }
                });
            }
            callbackContext.success(0);
        }
        else if (action.equals("clickNativeAd")) {
            final int id = args.getInt(0);
            final String x1 = args.getString(1);
            final String y1 = args.getString(2);
            final String x2 = args.getString(3);
            final String y2 = args.getString(4);
            if (id > 0) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        QuadNativeAd nativeAd = nativeAds.get("native-" + id);
                        if (nativeAd != null) {
                            nativeAd.onAdClick(cordova.getActivity(), webView.getView(), x1, y1, x2, y2);
                        }
                    }
                });
            }
            callbackContext.success(0);
        }
        else {
            return false;
        }
        return true;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        if (quadSplashAdCtrl != null) {
            quadSplashAdCtrl.setSplash(false);
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if (quadSplashAdCtrl != null) {
            quadSplashAdCtrl.next();
            quadSplashAdCtrl.setSplash(true);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
