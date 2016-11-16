package com.reshmisuresh.jarvis.Services.ActivityRecognition;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.reshmisuresh.jarvis.JarvisApplication;

public class ActivityRecognitionService extends IntentService {
    public JarvisApplication appState;
    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    public ActivityRecognitionService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        appState = (JarvisApplication) getApplicationContext();


        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult
                    .extractResult(intent);

            DetectedActivity detected = result.getMostProbableActivity();

            appState.detectedActivity = detected.getType();
            if (detected.getType() == DetectedActivity.IN_VEHICLE){
                appState.isDriving = true;
            }
        }
    }
}
