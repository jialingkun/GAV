package com.itsgav.gavapps;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;
//import com.pusher.android.PusherAndroid;
//import com.pusher.android.notifications.ManifestValidator;
//import com.pusher.android.notifications.PushNotificationRegistration;
//import com.pusher.android.notifications.gcm.GCMPushNotificationReceivedListener;
//import com.pusher.client.Pusher;
//import com.pusher.client.PusherOptions;
//import com.pusher.client.channel.Channel;
//import com.pusher.client.channel.SubscriptionEventListener;

public class MainActivity extends AppCompatActivity {

//    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
//    private static final String TAG = "yourtag";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (playServicesAvailable()){
//
//            PusherAndroid pusher = new PusherAndroid("c2739a30fa336010d596"); //API KEY
//            PushNotificationRegistration nativePusher = pusher.nativePusher();
//            String defaultSenderId = getString(R.string.gcm_defaultSenderId); // fetched from your google-services.json
//            try {
//                nativePusher.registerGCM(this, defaultSenderId);
//
//            } catch (ManifestValidator.InvalidManifestException e) {
//                e.printStackTrace();
//            }
//
//            nativePusher.subscribe("itsgav"); // the client is interested in donuts
//
//
//            nativePusher.setGCMListener(new GCMPushNotificationReceivedListener() {
//                @Override
//                public void onMessageReceived(String from, Bundle data) {
//                    // do something magical 🔮
//                    String message = data.getString("message");
//                    Log.d(TAG, "Received push notification from: " + from);
//                    Log.d(TAG, "Message: " + message);
//                }
//            });
//
//
//        }


        WebView myWebView =(WebView) findViewById(R.id.webview);
        myWebView.setWebChromeClient(new WebChromeClient());
        //myWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        //myWebView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.loadUrl("http://www.proto.gavapps.asia");
        //myWebView.loadUrl("http://www.itsgav.com/proto");
        //myWebView.loadUrl("http://www.youtube.com");

        FirebaseMessaging.getInstance().subscribeToTopic("itsgav");

    }

    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }









//    private boolean playServicesAvailable() {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (apiAvailability.isUserResolvableError(resultCode)) {
//                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
//            } else {
//                Log.i(TAG, "This device is not supported.");
//                finish();
//            }
//            return false;
//        }
//        return true;
//    }
}
