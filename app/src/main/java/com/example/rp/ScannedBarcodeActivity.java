package com.example.rp;
import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class ScannedBarcodeActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String intentData = "";
    long startTime;
    long endTime;
    double elapsedTime;


    Intent batteryInfo;
    ConnectivityManager conmgr;
    boolean isintialize=false;
    double avaiMemPct;
    String totalMem;
    boolean isCharging;
    boolean usbCharge;
    boolean acCharge;
    float batteryPct;

    boolean isConnected;
    boolean isWifiConn;
    boolean isMobileConn;

    Integer complexityIndex;
    boolean isDelay;
    boolean onlyCloud;
    boolean onlyLocal;
    boolean localCloud;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_barcode);
        initViews();
         batteryInfo = this.registerReceiver(null, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        conmgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.i("QRcode-","Oncreate method");
    }

    private void initViews() {

        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
    }

    private void initialiseDetectorsAndSources() {


        isintialize=true;
if(decisionEngine()){
    startCameraSource();
}
else {
    Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
    barcodeDetector = new BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build();

    cameraSource = new CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build();

    surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (ActivityCompat.checkSelfPermission(ScannedBarcodeActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraSource.start(surfaceView.getHolder());
                    Log.i("QRcode-", "Camera started");
                } else {
                    ActivityCompat.requestPermissions(ScannedBarcodeActivity.this, new
                            String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            cameraSource.stop();
        }
    });


    barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
        @Override
        public void release() {
            Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
        }


        @Override
        public void receiveDetections(Detector.Detections<Barcode> detections) {
            Log.i("QRcode-", "receive detection");
            final SparseArray<Barcode> barcodes = detections.getDetectedItems();
            if (barcodes.size() != 0) {
                Log.i("QRcode-", "Barcode detected");
                txtBarcodeValue.post(new Runnable() {
                    @Override
                    public void run() {
                        txtBarcodeValue.removeCallbacks(null);
                        intentData = barcodes.valueAt(0).displayValue;
                        endTime = System.nanoTime();
                        txtBarcodeValue.setText(intentData);
                        elapsedTime= roundTwoDecimals((double)endTime-startTime / 1_000_000_000.0);
                        Intent myIntent = new Intent(ScannedBarcodeActivity.this, Scanresult.class);
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
                        myIntent.putExtra("scanresulttext", intentData);
                        myIntent.putExtra("processed", "Local");
                        myIntent.putExtra("elapsedTime", elapsedTime);
                        ScannedBarcodeActivity.this.startActivity(myIntent);
                        cameraSource.stop();
                    }
                });
            }
        }
    });
}
    }

    private void startCameraSource() {
        Log.i("QRcode-","Start camera source called");
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            startTime = System.nanoTime();

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            //decisionEngine(photo);
            Log.i("QRcode-","OnActivity Result");
            String photoStr= getStringImage(photo);
            cloudcall(photoStr);
            //imageView.setImageBitmap(photo);
        }
    }


    protected Boolean decisionEngine() {

        //cameraSource.stop();
        onlyCloud=false;
        onlyLocal=false;
        localCloud=true;
        Log.i("QRcode-","Entered Decision engine");

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


        HashMap<String,Object> taskMap=  TaskProfiler.getTaskdetails("scanLocalCall","scanner");
        isDelay = (boolean)taskMap.get("isDelay");
        complexityIndex = (Integer)taskMap.get("complexity");
       // avaiMemPct=54.3;

        //takeImage();
        //scanLocalCall(barcodes);
        //return false;
        if(isConnected){
            if(localCloud){
                if(isDelay){
                    if(isWifiConn){
                        if(isCharging){
                            if(avaiMemPct <=50 || complexityIndex >5){
                                Log.i("QRcode-","Taking Decision to Cloud Process");
                                //takeImage();
                                Toast.makeText(getApplicationContext(), "Decision taken :Cloud", Toast.LENGTH_SHORT).show();
                                return true;

                            }
                            else{
                                Log.i("QRcode-","Taking Decision to Local Process");
                                //scanLocalCall(barcodes);
                                Toast.makeText(getApplicationContext(), "Decision taken :Local", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }
                        else{
                            if(avaiMemPct <=50 || complexityIndex >5 || batteryPct <20){
                                Log.i("QRcode-","Taking Decision to Cloud Process");
                                // takeImage();
                                Toast.makeText(getApplicationContext(), "Decision taken :Cloud", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            else{
                                Log.i("QRcode-","Taking Decision to Local Process");
                                //scanLocalCall(barcodes);
                                Toast.makeText(getApplicationContext(), "Decision taken :Local", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }
                    }
                    else {
                        return false;
                    }
                }
                else{
                    if(isCharging){
                        if(avaiMemPct <=50 || complexityIndex >5){
                            Log.i("QRcode-","Taking Decision to Cloud Process");
                            //takeImage();
                            Toast.makeText(getApplicationContext(), "Decision taken :Cloud", Toast.LENGTH_SHORT).show();
                            return true;

                        }
                        else{
                            Log.i("QRcode-","Taking Decision to Local Process");
                            //scanLocalCall(barcodes);
                            Toast.makeText(getApplicationContext(), "Decision taken :Local", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                    else{
                        if(avaiMemPct <=50 || complexityIndex >5 || batteryPct <20){
                            Log.i("QRcode-","Taking Decision to Cloud Process");
                            // takeImage();
                            Toast.makeText(getApplicationContext(), "Decision taken :Cloud", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        else{
                            Log.i("QRcode-","Taking Decision to Local Process");
                            //scanLocalCall(barcodes);
                            Toast.makeText(getApplicationContext(), "Decision taken :Local", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                }
            }
            else if(onlyCloud){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }


    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void cloudcall(String input){
        Log.i("QRcode-","Cloud call called");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("base64str", input);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("QRcode-","Picture sent to cloud");
        AndroidNetworking.post("https://kd1woffvgl.execute-api.ap-south-1.amazonaws.com/prod/qrcode")
                .addJSONObjectBody(jsonObject)
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.i("QRcode-","Got response from Cloud ");
                        Log.i("QRcode-","Request Passed:"+response.toString());
                        endTime = System.nanoTime();
                        txtBarcodeValue.setText(response.toString());
                        elapsedTime= roundTwoDecimals((double)endTime-startTime / 1_000_000_000.0);
                        Intent myIntent = new Intent(ScannedBarcodeActivity.this, Scanresult.class);
                        myIntent.putExtra("batteryPct", batteryPct);
                        myIntent.putExtra("avaiMemPct", avaiMemPct);
                        myIntent.putExtra("preferred", "Cloud and Local");
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
                        try {
                            myIntent.putExtra("scanresulttext", response.getString("data"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        myIntent.putExtra("processed", "Cloud");
                        myIntent.putExtra("elapsedTime", elapsedTime);
                        ScannedBarcodeActivity.this.startActivity(myIntent);
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        endTime = System.nanoTime();
                        elapsedTime= roundTwoDecimals((double)endTime-startTime / 1_000_000_000.0);
                        Log.i("QRcode-","Error Request:"+error.toString());
                        txtBarcodeValue.setText(error.toString());
                        Intent myIntent = new Intent(ScannedBarcodeActivity.this, Scanresult.class);
                        myIntent.putExtra("batteryPct", batteryPct);
                        myIntent.putExtra("preferred", "Cloud and Local");
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
                        myIntent.putExtra("scanresulttext", error.toString());
                        myIntent.putExtra("processed", "Cloud");
                        myIntent.putExtra("elapsedTime", elapsedTime);
                        ScannedBarcodeActivity.this.startActivity(myIntent);
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isintialize) {
            initialiseDetectorsAndSources();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    double roundTwoDecimals(double d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }
}
