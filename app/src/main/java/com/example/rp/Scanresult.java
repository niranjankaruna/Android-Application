package com.example.rp;


import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Scanresult extends AppCompatActivity {

    SurfaceView surfaceView;
    TextView scanresulttext;
    TextView batteryPct;
    TextView avaiMemPct;
    TextView isCharging;
    TextView isConnected;
    TextView processed;
    TextView elapsedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_result);
        initViews();
        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
            batteryPct.setText(" = "+String.valueOf(extras.get("batteryPct")));
            avaiMemPct.setText(" = "+String.valueOf(extras.get("avaiMemPct"))+"% of "+ String.valueOf(extras.get("totalMem")));
            if(String.valueOf(extras.get("isCharging"))=="true"){
                isCharging.setText(" = "+"Charging : "+ String.valueOf(extras.get("chargeType")));
            }
            else{
                isCharging.setText(" = "+"Not Charging");
            }

            if(String.valueOf(extras.get("isConnected"))=="true"){
                isConnected.setText(" = "+"Connected : "+ String.valueOf(extras.get("conType")));
            }
            else{
                isConnected.setText(" = "+"Not Connected");
            }
            processed.setText(" = "+String.valueOf(extras.get("processed"))+" (Preferred = "+String.valueOf(extras.get("preferred"))+")");
            scanresulttext.setText(" = "+String.valueOf(extras.get("scanresulttext")));
            elapsedTime.setText(" = "+String.valueOf(extras.get("elapsedTime"))+ " seconds");

        }
    }

    private void initViews() {
        scanresulttext = findViewById(R.id.scanresulttext);
        batteryPct = findViewById(R.id.batteryPct);
        avaiMemPct = findViewById(R.id.avaiMemPct);
        isCharging = findViewById(R.id.isCharging);
        isConnected = findViewById(R.id.isConnected);
        processed = findViewById(R.id.processed);
        elapsedTime = findViewById(R.id.elapsedTime);
    }

}
