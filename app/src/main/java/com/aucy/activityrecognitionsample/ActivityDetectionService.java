package com.aucy.activityrecognitionsample;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;

import java.util.List;

public class ActivityDetectionService extends IntentService {

  protected static final String TAG = "activityDetectionService";

  public ActivityDetectionService() {
    super(TAG);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    final ActivityRecognitionResult
        activityRecognitionResult =
        ActivityRecognitionResult.extractResult(intent);
    if (activityRecognitionResult == null) {
      return;
    }

    //process the result here, pass the data needed to the broadcast
    // e.g. you may want to use activityRecognitionResult.getMostProbableActivity(); instead
    final List<DetectedActivity>
        probableActivities =
        activityRecognitionResult.getProbableActivities();

    sendBroadcast(MainActivity.newBroadcastIntent(probableActivities));
  }
}