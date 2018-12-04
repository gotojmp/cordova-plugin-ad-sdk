(function() {

    var cordova = require('cordova');
    var exec = require('cordova/exec');
    var modulemapper = require('cordova/modulemapper');
    var urlutil = require('cordova/urlutil');
    var channel = require('cordova/channel');


    function AdSdk() {
    }

    AdSdk.prototype.loadBannerAd = function (pid) {
        exec(null, null, "AdSdk", "loadBannerAd", [pid]);
    };

    AdSdk.prototype.loadInterstitialAd = function (pid) {
        exec(null, null, "AdSdk", "loadInterstitialAd", [pid]);
    };

    AdSdk.prototype.loadNativeAd = function (pid, cb) {
        var okCb = function (ad) {
            cb(null, ad);
        };
        var errCb = function () {
            cb(true);
        };
        exec(okCb, errCb, "AdSdk", "loadNativeAd", [pid]);
    };

    AdSdk.prototype.showNativeAd = function (id) {
        exec(null, null, "AdSdk", "showNativeAd", [id]);
    };

    AdSdk.prototype.reportClickNativeAd = function (id, x1, y1, x2, y2) {
        exec(null, null, "AdSdk", "reportClickNativeAd", [id, x1, y1, x2, y2]);
    };

    AdSdk.prototype.clickNativeAd = function (id, x1, y1, x2, y2) {
        exec(null, null, "AdSdk", "clickNativeAd", [id, x1, y1, x2, y2]);
    };

    module.exports = new AdSdk();

})();
