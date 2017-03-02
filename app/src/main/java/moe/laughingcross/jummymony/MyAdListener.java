package moe.laughingcross.jummymony;

import android.util.Log;

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.DefaultAdListener;
import com.amazon.device.ads.AdListener;

public class MyAdListener extends DefaultAdListener {
    public void onAdLoaded(AdLayout view, AdProperties adProperties) {
        Log.d("JunaCoffeeAds", "Ad Loaded");
    }

    public void onAdFailedToLoad(AdLayout view, AdError error) {
        Log.d("JunaCoffeeAds", "Ad Failed to load");
    }
}
