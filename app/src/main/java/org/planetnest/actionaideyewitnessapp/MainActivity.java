package org.planetnest.actionaideyewitnessapp;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.alexandroid.shpref.ShPref;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";
    private ProgressDialog progressDialog;

    public static MainActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        final boolean firstTime = !ShPref.getBoolean("NOT_FIRST");

        if (firstTime) {
            ShPref.put("NOT_FIRST", true);

            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);

            // set alarm
//            scheduleNotification(getNotification("Thanks for using our app"), 604800);
        } else doLogin();

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
    }

    @OnClick (R.id.camera_btn)
    public void gotoCamera(LinearLayout layout) {
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }

    @OnClick (R.id.video_btn)
    public void gotoVideo(LinearLayout layout) {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }

    @OnClick (R.id.audio_btn)
    public void gotoAudio(LinearLayout layout) {
        Intent intent = new Intent(this, AudioActivity.class);
        startActivity(intent);
    }

    @OnClick (R.id.blog_btn)
    public void gotoBlog(LinearLayout layout) {
        Intent intent = new Intent(this, BlogActivity.class);
        startActivity(intent);
    }

    public void doLogin() {
        final boolean logged = !ShPref.getString("LOGGED_email", "").equals("");

        if (!logged) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.btn_admin_access)
    void adminAccess() {
        final Dialog login = new Dialog(this, R.style.AppTheme_Sweet_Dialog);
        login.setContentView(R.layout.layout_admin_login);
        login.setTitle("Admin access");

        Button btnAdminAuth = (Button) login.findViewById(R.id.btn_admin_auth);
        TextView linkCancelLogin = (TextView) login.findViewById(R.id.link_cancel_login);

        btnAdminAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                v.setEnabled(false);

                final String _email = ((EditText) login.findViewById(R.id.input_admin_email)).getText().toString();
                String _pwd = ((EditText) login.findViewById(R.id.input_admin_pwd)).getText().toString();

                if (_email.isEmpty() || _pwd.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Email and password are mandatory", Toast.LENGTH_LONG).show();
                    v.setEnabled(true);
                    return;
                }

                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Authenticating...");
                progressDialog.show();

                params.add("email", _email);
                params.add("password", _pwd);

                Api.post("admin-auth", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

//                        Log.e("MOFESOLA", response.toString());
                        try {
                            if (response.getBoolean("status")) {
                                ShPref.put("admin", _email);
                                launchAdminSection();
                            }
                            else Toast.makeText(MainActivity.this, response.getString("errmsg"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
                        }

                        dismissDialog();
                        login.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
//                        Log.e("MOFESOLA", responseString);
                        Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_LONG).show();

                        dismissDialog();
                        login.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Toast.makeText(MainActivity.this, "Connection failed, check network", Toast.LENGTH_LONG).show();

                        dismissDialog();
                        login.dismiss();
                    }
                });
            }
        });

        linkCancelLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.dismiss();
            }
        });

        login.show();
    }

    private void launchAdminSection() {
        Toast.makeText(MainActivity.this, "Authentication successful", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
        startActivity(intent);
    }

    private void dismissDialog() {
        progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (App.doubleBackToExitPressedOnce) {
            super.onBackPressed();
        }

        App.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                App.doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void toast(String msg) {
        Toast.makeText(MainActivity.self, msg, Toast.LENGTH_SHORT).show();
    }

    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, futureInMillis, pendingIntent);

//        Log.e("MOFE", "Scheduling alarm");
    }


    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("ActionAid Nigeria");
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher);

//        Log.e("MOFE", "Create notification");

        return builder.build();
    }
}
