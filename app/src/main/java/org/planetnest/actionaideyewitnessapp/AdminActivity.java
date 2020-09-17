package org.planetnest.actionaideyewitnessapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.alexandroid.shpref.ShPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cz.msebera.android.httpclient.Header;

public class AdminActivity extends AppCompatActivity {
    final String TAG = "AdminActivity";
    private ProgressDialog progressDialog;

    @BindView(R.id.spinner_current_events)
    Spinner _spinnerCurrentEvents;
    @BindView(R.id.btn_add_attendance)
    Button _btnAddAttendance;
    @BindView(R.id.txt_event_title)
    TextView _txtEventTitle;
    @BindView(R.id.layout_attendance)
    LinearLayout _layoutAttendance;
    @BindView(R.id.layout_admin_menu)
    LinearLayout _layoutAdminMenu;
    @BindView(R.id.pane_attendee_name)
    TextInputLayout _paneAttendeeName;
    @BindView(R.id.label_attendance_name)
    EditText _labelAttendanceName;
    @BindView(R.id.input_attendance_phone)
    EditText _inputAttendancePhone;

    private boolean _init = false;
    private boolean _attendance = false;
    private boolean _recordExists = false;
    private ArrayAdapter<CurrentEvent> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ButterKnife.bind(this);

        adapter = new ArrayAdapter<CurrentEvent>(AdminActivity.this, android.R.layout.simple_spinner_item);

        progressDialog = new ProgressDialog(AdminActivity.this, R.style.AppTheme_Dark_Dialog);
        refreshEvents();
    }

    @OnClick(R.id.btn_refresh_events)
    void refreshEvents() {
        if (!_init) showProgressDialog("Loading current events...");

        Api.post("get-current-events", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

//                Log.e("MOFESOLA", response.toString());
                try {
                    if (response.getBoolean("status")) {
                        CurrentEvent[] events = App.gson.fromJson(response.getJSONArray("data").toString(), CurrentEvent[].class);

                        if (events.length == 0) {
                            disableRefreshBtn();
                            toast("There are no current events");
                        } else {
                            _btnAddAttendance.setEnabled(true);
                        }

                        adapter = new ArrayAdapter<CurrentEvent>(AdminActivity.this, android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.addAll(events);
                        _spinnerCurrentEvents.setAdapter(adapter);

                        if (!_init) {
                            _init = true;
                            dismissDialog();
                        }
                    } else {
                        if (!_init) disableRefreshBtn();
                        toast("Unable to fetch events, please refresh");
                    }
                } catch (JSONException e) {
                    if (!_init) disableRefreshBtn();
                    toast("Unable to fetch events, please refresh");
                }

                dismissDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
//                Log.e("MOFESOLA", responseString);
                toast("Connection failed");

                dismissDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                toast("Connection failed, check network");

                dismissDialog();
            }
        });
    }

    @OnClick(R.id.btn_add_attendance)
    void addAttendance() {
        int index = _spinnerCurrentEvents.getSelectedItemPosition();
        if (index >= 0) {
            CurrentEvent event = adapter.getItem(index);

            ShPref.put("event_id", event.id);
            _txtEventTitle.setText(event.title);
            _layoutAdminMenu.setVisibility(View.GONE);
            _layoutAttendance.setVisibility(View.VISIBLE);
            _attendance = true;
        } else toast("Select an event first");
    }

    @OnClick(R.id.btn_register_attendees)
    void registerAttendees() {
        int index = _spinnerCurrentEvents.getSelectedItemPosition();
        if (index >= 0) {
            CurrentEvent event = adapter.getItem(index);

            ShPref.put("event_id", event.id);
            ShPref.put("event_title", event.title);

            launch(EventAttendeeActivity.class);
        } else toast("Select an event first");
    }

    @OnClick(R.id.btn_create_event)
    void createEvent() {
        launch(AddEventActivity.class);
    }

    void launch(Class who) {
        Intent intent = new Intent(this, who);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (_attendance) dismissAttendance();
        else {
            if (App.doubleBackToExitPressedOnce) {
                App.doubleBackToExitPressedOnce = false;
                super.onBackPressed();
            }

            App.doubleBackToExitPressedOnce = true;
            toast("Press back again to exit");

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    App.doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @OnClick(R.id.txt_go_back)
    void dismissAttendance() {
        _layoutAttendance.setVisibility(View.GONE);
        _layoutAdminMenu.setVisibility(View.VISIBLE);
        _attendance = false;
    }

    private void dismissDialog() {
        progressDialog.dismiss();
    }

    void disableRefreshBtn() {
        _btnAddAttendance.setEnabled(false);
    }

    void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @OnTextChanged(R.id.input_attendance_phone)
    void phoneChanged() {
        _recordExists = false;
        _paneAttendeeName.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_pull_records)
    void pullRecords() {
        if (_inputAttendancePhone.getText().toString().isEmpty()) {
            toast("No phone number entered yet");
            return;
        }

        _recordExists = false;
        _paneAttendeeName.setVisibility(View.GONE);

        showProgressDialog("Pulling records...");

        RequestParams params = new RequestParams();
        params.put("event_id", Integer.toString(ShPref.getInt("event_id")));
        params.put("phone", _inputAttendancePhone.getText().toString());

        Api.post("pull-records", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

//                Log.e("MOFESOLA", response.toString());
                try {
                    if (response.getBoolean("status")) {
                        _recordExists = true;
                        _paneAttendeeName.setVisibility(View.VISIBLE);
                        _labelAttendanceName.setText(response.getString("data"));
                    } else toast("Records not found!");
                } catch (JSONException e) {
                    toast("Records not found, please retry");
                }

                dismissDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
//                Log.e("MOFESOLA", responseString);
                toast("Connection failed");

                dismissDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                toast("Connection failed, check network");

                dismissDialog();
            }
        });
    }

    @OnClick(R.id.btn_record_attendance)
    void recordAttendance() {
        if (!_recordExists) toast("Use an already registered phone number");
        else {
            showProgressDialog("Recording attendance...");

            RequestParams params = new RequestParams();
            params.put("event_id", Integer.toString(ShPref.getInt("event_id")));
            params.put("phone", _inputAttendancePhone.getText().toString());

            Api.post("mark-attendance", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);

//                    Log.e("MOFESOLA", response.toString());
                    try {
                        if (response.getBoolean("status")) {
                            _recordExists = false;
                            _paneAttendeeName.setVisibility(View.GONE);
                            _inputAttendancePhone.setText("");
                            toast("Register updated");
                        } else toast("Attendance update failed");
                    } catch (JSONException e) {
                        toast("Attendance update failed, please retry");
                    }

                    dismissDialog();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
//                    Log.e("MOFESOLA", responseString);
                    toast("Connection failed");

                    dismissDialog();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    toast("Connection failed, check network");

                    dismissDialog();
                }
            });
        }
    }

    void showProgressDialog(String msg) {
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    private class CurrentEvent {
        public int id;
        public String title = "";

        public CurrentEvent() {
        }

        public CurrentEvent(int id, String title) {
            this.id = id;
            this.title = title;
        }

        @Override
        public String toString() {
            return this.title;
        }
    }
}
