package com.aucy.activityrecognitionsample;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
               ResultCallback<Status> {

  private static final long DETECT_INTERVAL = 0;
  private static final String BROADCAST_ACTION = "com.aucy.activityrecognitionsample.broadcast";
  private static final String DETECTED_ACTIVITIES = "detectedActivities";

  public static Intent newBroadcastIntent(List<DetectedActivity> detectedActivities) {
    final ArrayList<DetectedActivity> activities = new ArrayList<DetectedActivity>(detectedActivities);
    return new Intent(BROADCAST_ACTION).putParcelableArrayListExtra(DETECTED_ACTIVITIES, activities);
  }

  public static IntentFilter newBroadcastIntentFilter() {
    return new IntentFilter(BROADCAST_ACTION);
  }

  private GoogleApiClient mGoogleApiClient;
  private TextView mStatusView;
  private BroadcastReceiver mBroadcastReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mStatusView = (TextView) findViewById(R.id.status);
    mGoogleApiClient =
        new GoogleApiClient.Builder(this).addApi(ActivityRecognition.API)
            .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

    mBroadcastReceiver = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
        List<DetectedActivity>
            detectedActivities =
            intent.getParcelableArrayListExtra(DETECTED_ACTIVITIES);
        StringBuilder builder = new StringBuilder();
        builder.append("result:\n");
        if (detectedActivities != null) {
          for (DetectedActivity detectedActivity : detectedActivities) {
            switch (detectedActivity.getType()) {
              case DetectedActivity.IN_VEHICLE:
                builder.append("IN_VEHICLE");
                break;
              case DetectedActivity.ON_BICYCLE:
                builder.append("ON_BICYCLE");
                break;
              case DetectedActivity.ON_FOOT:
                builder.append("ON_FOOT");
                break;
              case DetectedActivity.RUNNING:
                builder.append("RUNNING");
                break;
              case DetectedActivity.STILL:
                builder.append("STILL");
                break;
              case DetectedActivity.TILTING:
                builder.append("TILTING");
                break;
              case DetectedActivity.WALKING:
                builder.append("WALKING");
                break;
              case DetectedActivity.UNKNOWN:
                builder.append("UNKNOWN");
                break;
              default:
                builder.append("UNEXPECTED");
                break;
            }
            builder.append(": ").append(detectedActivity.getConfidence()).append('\n');
          }
        }
        mStatusView.setText(builder);
      }
    };
  }

  @Override
  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
    mStatusView.setText("connecting");
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerReceiver(mBroadcastReceiver, newBroadcastIntentFilter());
  }

  @Override
  protected void onPause() {
    super.onPause();
    unregisterReceiver(mBroadcastReceiver);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mGoogleApiClient.disconnect();
    mStatusView.setText("disconnected");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onConnected(Bundle bundle) {
    mStatusView.setText("connected");

    final PendingResult<Status>
        statusPendingResult =
        ActivityRecognition.ActivityRecognitionApi
            .requestActivityUpdates(mGoogleApiClient, DETECT_INTERVAL, PendingIntent
                .getService(this, 0, new Intent(this, ActivityDetectionService.class),
                              PendingIntent.FLAG_UPDATE_CURRENT));
    statusPendingResult.setResultCallback(this);
  }

  @Override
  public void onConnectionSuspended(int i) {
    mStatusView.setText("connection suspended");
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    mStatusView.setText("connection failed");
  }

  @Override
  public void onResult(Status status) {
    if (!status.isSuccess()) {
      mStatusView.setText(
          "Activity Recognition failed to start: " + status.getStatusCode() + ", " + status
              .getStatusMessage());
    }
  }
}
