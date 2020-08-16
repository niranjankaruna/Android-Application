package com.example.rp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.vision.CameraSource;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;


public class CameraImage extends AppCompatActivity
{
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    Intent batteryInfo;
    ConnectivityManager conmgr;

    double avaiMemPct;
    String totalMem;
    boolean isCharging;
    boolean usbCharge;
    boolean acCharge;
    float batteryPct;

    boolean isConnected;
    boolean isWifiConn;
    boolean isMobileConn;

    boolean onlyCloud;
    boolean onlyLocal;
    boolean localCloud;

    long startTime;
    long endTime;
    double elapsedTime;

    Integer complexityIndex;
    boolean isDelay;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        initViews();
        batteryInfo = this.registerReceiver(null, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        conmgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        startCameraSource();
    }
    private void initViews() {
        Log.i("QRcode-","Views loaded successfully");
    }
    private void startCameraSource() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            }
            else {
                Log.i("QRcode-", "Start camera source called");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, MY_CAMERA_PERMISSION_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == MY_CAMERA_PERMISSION_CODE && resultCode == Activity.RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            decisionEngine(photo);
            //imageView.setImageBitmap(photo);
        }
    }
    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    protected void decisionEngine(Bitmap input) {

        //cameraSource.stop();
        onlyCloud=false;
        onlyLocal=false;
        localCloud=true;
        Log.i("QRcode-","Entered Decision engine");
        String cloudInput1 = getStringImage(input);

        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        HashMap<String,Object> deviceMap=  DeviceProfiler.getDeviceParameters(activityManager,batteryInfo);
        totalMem = (String) deviceMap.get("totalMem");
        avaiMemPct = (Double)deviceMap.get("avaiMemPct");
        isCharging = (Boolean)deviceMap.get("isCharging");
        usbCharge = (Boolean)deviceMap.get("usbCharge");
        acCharge = (Boolean)deviceMap.get("acCharge");
        batteryPct = (float)deviceMap.get("batteryPct");

        HashMap<String,Boolean> networkMap=  NetworkProfiler.updateNetworkInfo(conmgr);
        isConnected = networkMap.get("isConnected");
        isWifiConn = networkMap.get("isWifiConn");
        isMobileConn = networkMap.get("isMobileConn");


        HashMap<String,Object> taskMap=  TaskProfiler.getTaskdetails("localcall","image");
        isDelay = (boolean)taskMap.get("isDelay");
        complexityIndex = (Integer)taskMap.get("complexity");


        if(isConnected){
            if(localCloud){
                if(isDelay){
                    if(isWifiConn){
                        if(isCharging){
                            if(avaiMemPct <=50 || complexityIndex >5){
                                Log.i("QRcode-","Taking Decision to Cloud Process");
                                //takeImage();
                                Toast.makeText(getApplicationContext(), "Decision taken :Cloud", Toast.LENGTH_SHORT).show();
                                cloudcall(cloudInput1);

                            }
                            else{
                                Log.i("QRcode-","Taking Decision to Local Process");
                                //scanLocalCall(barcodes);
                                Toast.makeText(getApplicationContext(), "Decision taken :Local", Toast.LENGTH_SHORT).show();
                                localcall(input);
                            }
                        }
                        else{
                            if(avaiMemPct <=50 || complexityIndex >5 || batteryPct <20){
                                Log.i("QRcode-","Taking Decision to Cloud Process");
                                // takeImage();
                                Toast.makeText(getApplicationContext(), "Decision taken :Cloud", Toast.LENGTH_SHORT).show();
                                cloudcall(cloudInput1);
                            }
                            else{
                                Log.i("QRcode-","Taking Decision to Local Process");
                                //scanLocalCall(barcodes);
                                Toast.makeText(getApplicationContext(), "Decision taken :Local", Toast.LENGTH_SHORT).show();
                                localcall(input);
                            }
                        }
                    }
                    else {
                        localcall(input);
                    }
                }
                else{
                    if(isCharging){
                        if(avaiMemPct <=50 || complexityIndex >5){
                            Log.i("QRcode-","Taking Decision to Cloud Process");
                            //takeImage();
                            Toast.makeText(getApplicationContext(), "Decision taken :Cloud", Toast.LENGTH_SHORT).show();
                            cloudcall(cloudInput1);

                        }
                        else{
                            Log.i("QRcode-","Taking Decision to Local Process");
                            //scanLocalCall(barcodes);
                            Toast.makeText(getApplicationContext(), "Decision taken :Local", Toast.LENGTH_SHORT).show();
                            localcall(input);
                        }
                    }
                    else{
                        if(avaiMemPct <=50 || complexityIndex >5 || batteryPct <20){
                            Log.i("QRcode-","Taking Decision to Cloud Process");
                            // takeImage();
                            Toast.makeText(getApplicationContext(), "Decision taken :Cloud", Toast.LENGTH_SHORT).show();
                            cloudcall(cloudInput1);
                        }
                        else{
                            Log.i("QRcode-","Taking Decision to Local Process");
                            //scanLocalCall(barcodes);
                            Toast.makeText(getApplicationContext(), "Decision taken :Local", Toast.LENGTH_SHORT).show();
                            localcall(input);
                        }
                    }
                }
            }
            else if(onlyCloud){
                cloudcall(cloudInput1);
            }
            else{
                localcall(input);
            }
        }
        else{
            localcall(input);
        }



    }


    private void localcall(Bitmap input){

        startTime = System.nanoTime();
        Bitmap imgContrast =createContrast(input,20);
        Bitmap imgBright =doBrightness(imgContrast,20);
        Bitmap imgInvert =doInvert(imgBright);
        Log.i("QRcode- Local",getStringImage(imgInvert));

        endTime = System.nanoTime();
        elapsedTime= roundTwoDecimals((double)endTime-startTime / 1_000_000_000.0);
        Intent myIntent = new Intent(CameraImage.this, Imageresult.class);
        myIntent.putExtra("batteryPct", batteryPct);
        myIntent.putExtra("avaiMemPct", avaiMemPct);
        myIntent.putExtra("totalMem", totalMem);
        myIntent.putExtra("isCharging", isCharging);
        if(isWifiConn){
            myIntent.putExtra("conType", "WiFi");
        }
        else{
            myIntent.putExtra("conType", "Mobile data");
        }
        if(acCharge){
            myIntent.putExtra("chargeType", "AC Adapter");
        }
        else{
            myIntent.putExtra("chargeType", "USB Charge");
        }

        myIntent.putExtra("isConnected", isConnected);
        myIntent.putExtra("imageResult", getStringImage(imgInvert));
        myIntent.putExtra("processed", "Local");
        myIntent.putExtra("preferred", "Cloud and Local");
        myIntent.putExtra("elapsedTime", elapsedTime);
        CameraImage.this.startActivity(myIntent);
    }

    private void cloudcall(String input){
        startTime = System.nanoTime();
        Log.i("QRcode-","Cloud call called");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("base64str", input);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("QRcode-","Picture sent to cloud");
        AndroidNetworking.post("https://kd1woffvgl.execute-api.ap-south-1.amazonaws.com/prod/imageprocess")
                .addJSONObjectBody(jsonObject)
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        String res="";
                        try {
                             res= response.getString("data");
                            res= res.replace("-","+");
                            res= res.replace("_","/");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("QRcode-","Got response from Cloud ");
                        Log.i("QRcode-","Request Passed:"+res);
                        endTime = System.nanoTime();
                        elapsedTime= roundTwoDecimals((double)endTime-startTime / 1_000_000_000.0);
                        Intent myIntent = new Intent(CameraImage.this, Imageresult.class);
                        myIntent.putExtra("batteryPct", batteryPct);
                        myIntent.putExtra("avaiMemPct", avaiMemPct);
                        myIntent.putExtra("totalMem", totalMem);
                        myIntent.putExtra("preferred", "Cloud and Local");
                        myIntent.putExtra("isCharging", isCharging);
                        if(isWifiConn){
                            myIntent.putExtra("conType", "WiFi");
                        }
                        else{
                            myIntent.putExtra("conType", "Mobile data");
                        }
                        if(acCharge){
                            myIntent.putExtra("chargeType", "AC Adapter");
                        }
                        else{
                            myIntent.putExtra("chargeType", "USB Charge");
                        }

                        myIntent.putExtra("isConnected", isConnected);
                        myIntent.putExtra("imageResult", res);
                        myIntent.putExtra("processed", "Cloud");
                        myIntent.putExtra("elapsedTime", elapsedTime);
                        CameraImage.this.startActivity(myIntent);
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error

                        Log.i("QRcode-","Error Request:"+error.toString());
                        endTime = System.nanoTime();
                        elapsedTime= roundTwoDecimals((double)endTime-startTime / 1_000_000_000.0);
                        Intent myIntent = new Intent(CameraImage.this, Imageresult.class);
                        myIntent.putExtra("batteryPct", batteryPct);
                        myIntent.putExtra("avaiMemPct", avaiMemPct);
                        myIntent.putExtra("totalMem", totalMem);
                        myIntent.putExtra("preferred", "Cloud and Local");
                        myIntent.putExtra("isCharging", isCharging);
                        if(isWifiConn){
                            myIntent.putExtra("conType", "WiFi");
                        }
                        else{
                            myIntent.putExtra("conType", "Mobile data");
                        }
                        if(acCharge){
                            myIntent.putExtra("chargeType", "AC Adapter");
                        }
                        else{
                            myIntent.putExtra("chargeType", "USB Charge");
                        }

                        myIntent.putExtra("isConnected", isConnected);
                        myIntent.putExtra("imageResult", error.toString());
                        myIntent.putExtra("processed", "Cloud");
                        myIntent.putExtra("elapsedTime", elapsedTime);
                        CameraImage.this.startActivity(myIntent);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, MY_CAMERA_PERMISSION_CODE);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    public static Bitmap createContrast(Bitmap src, double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.red(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.red(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }
    public static Bitmap doBrightness(Bitmap src, int value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // increase/decrease each channel
                R += value;
                if(R > 255) { R = 255; }
                else if(R < 0) { R = 0; }

                G += value;
                if(G > 255) { G = 255; }
                else if(G < 0) { G = 0; }

                B += value;
                if(B > 255) { B = 255; }
                else if(B < 0) { B = 0; }

                // apply new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public static Bitmap doInvert(Bitmap src) {
        // create new bitmap with the same settings as source bitmap
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // color info
        int A, R, G, B;
        int pixelColor;
        // image size
        int height = src.getHeight();
        int width = src.getWidth();

        // scan through every pixel
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                // get one pixel
                pixelColor = src.getPixel(x, y);
                // saving alpha channel
                A = Color.alpha(pixelColor);
                // inverting byte for each R/G/B channel
                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);
                // set newly-inverted pixel to output image
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final bitmap
        return bmOut;
    }

    double roundTwoDecimals(double d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }


}

