package com.example.rp;

import java.util.HashMap;

public class TaskProfiler {



    public static HashMap<String, Object> getTaskdetails(String methodName, String taskName){

        HashMap<String, Integer> complexityMap = new HashMap<String, Integer>();
        complexityMap.put("scanner-complexityUpdate",1);
        complexityMap.put("scanner-BroadcastReceiver",1 );
        complexityMap.put("scanner-onCreate", 1);
        complexityMap.put("scanner-initViews",3);
        complexityMap.put("scanner-initialiseDetectorsAndSources",4);
        complexityMap.put("scanner-scanLocalCall",2);
        complexityMap.put("scanner-decisionEngine",14);
        complexityMap.put("scanner-takeImage",3);
        complexityMap.put("scanner-cloudcall",2);
        complexityMap.put("scanner-onPause",1);
        complexityMap.put("scanner-onResume",1);
        complexityMap.put("image-onCreate", 1);
        complexityMap.put("image-initViews",3);
        complexityMap.put("image-startCameraSource",1);
        complexityMap.put("image-onActivityResult",3);
        complexityMap.put("image-getStringImage",1);
        complexityMap.put("image-decisionEngine",1);
        complexityMap.put("image-localcall",1);
        complexityMap.put("image-cloudcall",2);
        complexityMap.put("image-onRequestPermissionsResult",3);
        complexityMap.put("image-createContrast",9 );
        complexityMap.put("image-doBrightness",9 );
        complexityMap.put("image-doInvert",3 );


        HashMap<String, Object> taskMap = new HashMap<String, Object>();
        taskMap.put("complexity",complexityMap.get(taskName+"-"+methodName));
        if(taskName.contentEquals("scanner")){
            taskMap.put("isDelay",true );
        }
        else if(taskName.contentEquals("image")){
            taskMap.put("isDelay",false );
        }
        return taskMap;


    }
}
