package com.example.tronsnakegame;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.appopen.AppOpenAd;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private InterstitialAd mInterstitialAd;
    private AppOpenAd appOpenAd = null;
    private boolean isShowingAd = false;

    private static final String TAG = "TronSnakeGame";
    private static final String APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/3419835294";
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadAppOpenAd();
                loadInterstitialAd();
            }
        });

        webView = findViewById(R.id.webView);
        setupWebView();
        setupBannerAd();
    }

    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Page loaded");
                webView.evaluateJavascript("javascript:initializeGame();", null);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, "Console: " + consoleMessage.message());
                return true;
            }
        });

        WebView.setWebContentsDebuggingEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.loadUrl("file:///android_asset/snake_game.html");
    }

    private void loadAppOpenAd() {
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(this, APP_OPEN_AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        Log.d(TAG, "App Open Ad loaded successfully");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, "App Open Ad failed to load: " + loadAdError.getMessage());
                    }
                });
    }

    private void showAppOpenAd() {
        if (!isShowingAd && appOpenAd != null) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    appOpenAd = null;
                    isShowingAd = false;
                    loadAppOpenAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.e(TAG, "App Open Ad failed to show: " + adError.getMessage());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    isShowingAd = true;
                }
            };

            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(this);
        } else {
            loadAppOpenAd();
        }
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, INTERSTITIAL_AD_UNIT_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.d(TAG, "Interstitial ad loaded successfully");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(TAG, "Interstitial ad failed to load: " + loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    private void showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad was dismissed");
                    mInterstitialAd = null;
                    loadInterstitialAd(); // Load the next interstitial ad
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.d(TAG, "Interstitial ad failed to show: " + adError.getMessage());
                    mInterstitialAd = null;
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad was shown");
                }
            });
            mInterstitialAd.show(this);
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.");
            loadInterstitialAd(); // Try to load it for next time
        }
    }

    private void setupBannerAd() {
        AdView adView = new AdView(this);
        adView.setAdUnitId(BANNER_AD_UNIT_ID);
        adView.setAdSize(AdSize.BANNER);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        FrameLayout adViewContainer = findViewById(R.id.adViewContainer);
        adViewContainer.addView(adView);
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void onGameOver() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInterstitialAd();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isShowingAd) {
            showAppOpenAd();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
}