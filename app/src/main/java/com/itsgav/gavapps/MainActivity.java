package com.itsgav.gavapps;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    WebView myWebView;
    ProgressBar progressBar;
    ImageView splash;
    String homeURL = "http://apps.itsgav.com/app/";


    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;
            //Check if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == FCR){
                    if(null == mUMA){
                        return;
                    }
                    if(intent == null){
                        //Capture Photo if no image available
                        if(mCM != null){
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    }else{
                        String dataString = intent.getDataString();
                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        }else{
            if(requestCode == FCR){
                if(null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.loading);
        splash = (ImageView) findViewById(R.id.splash);
        myWebView =(WebView) findViewById(R.id.webview);
        assert myWebView != null;
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);

        if(Build.VERSION.SDK_INT >= 21){
            webSettings.setMixedContentMode(0);
            myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT >= 19){
            myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT < 19){
            myWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }



        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted (WebView view, String url, Bitmap favicon){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.INVISIBLE);
                progressBar.setPadding(0,0,0,0);
                splash.setVisibility(View.GONE);
            }



            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                loadErrorPage(description);
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                loadErrorPage(error.getDescription().toString());
            }


        });

        myWebView.setWebChromeClient(new WebChromeClient(){
            //For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i,"File Chooser"), FCR);
            }
            // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
            public void openFileChooser(ValueCallback uploadMsg, String acceptType){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FCR);
            }
            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FCR);
            }
            //For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams){
                if(mUMA != null){
                    mUMA.onReceiveValue(null);
                }
                mUMA = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null){
                    File photoFile = null;
                    try{
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCM);
                    }catch(IOException ex){
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if(photoFile != null){
                        mCM = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    }else{
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;
                if(takePictureIntent != null){
                    intentArray = new Intent[]{takePictureIntent};
                }else{
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FCR);
                return true;
            }
        });


        //get link and load
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("link/gavapps");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                homeURL = value;
                Log.d(TAG, "onDataChange: Get this value: "+value);

                myWebView.loadUrl(homeURL);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onDataChange: FAILED TO GET VALUE");
                loadErrorPage("Failed to access firebase");
            }
        });

        //check if internet not connect after 5 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (homeURL == "http://apps.itsgav.com/app/"){
                    loadErrorPage("No Internet");
                }
            }
        }, 5 * 1000);

        FirebaseMessaging.getInstance().subscribeToTopic("gavapps");

    }


    // Create an image file
    private File createImageFile() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }


    public void loadErrorPage(String description){
        if(myWebView!=null){

            String htmlData ="<html>\n" +
                    "<body>\n" +
                    "\t<style>\n" +
                    "\tbody{\n" +
                    "\t\tfont-family: Arial, Helvetica, sans-serif;\n" +
                    "\t}\n" +
                    "\t\t.button {\n" +
                    "\t\t\tbackground-color: #BDC3C7;\n" +
                    "\t\t\tborder: none;\n" +
                    "\t\t\tpadding: 10px 15px;\n" +
                    "\t\t\ttext-align: center;\n" +
                    "\t\t\tfont-size: 23px;\n" +
                    "\t\t\tfont-weight: 700;\n" +
                    "\t\t\tmargin: 10px 2px 60px 2px;\n" +
                    "\t\t\tborder-radius: 3px;\n" +
                    "\t\t}\n" +
                    "\n" +
                    "\t\t.button:hover{\n" +
                    "\t\t\tbackground-color: #6C7A89;\n" +
                    "\t\t}\n" +
                    "\t</style>\n" +
                    "\t<div align=\"center\" style=\"margin-top: 35vh; font-size: 13px;\">\n" +
                    "\tFailed to load page<br> \n" +
                    "\t<a href=\""+homeURL+"\"> <button class=\"button\"> Reload </button></a>\n" +
//                    "\t<br> \n" + description +
                    "\t</div>\n" +
                    "</body>\n" +
                    "</html>";

            myWebView.loadUrl("about:blank");
            myWebView.loadDataWithBaseURL(null,htmlData, "text/html", "UTF-8",null);
            myWebView.invalidate();

        }
    }


    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            exitingApp();
        }
    }

    public void exitingApp(){
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

    public void clickShare(View view){
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "GAV");
            String sAux = "Install aplikasi ini!\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=com.itsgav.gavapps \n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "choose one"));
        } catch(Exception e) {
            //e.toString();
        }
    }

    public void clickRefresh(View view){
        myWebView.loadUrl( "javascript:window.location.reload( true )" );
    }

}
