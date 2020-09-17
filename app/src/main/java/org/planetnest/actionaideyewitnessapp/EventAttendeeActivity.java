package org.planetnest.actionaideyewitnessapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.alexandroid.shpref.ShPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class EventAttendeeActivity extends BaseActivity {
    private static final String TAG = "EventAttendeeActivity";
    private ProgressDialog progressDialog;

    @BindView(R.id.main)
    ScrollView _main;
    @BindView(R.id.text_event_title)
    TextView _textEventTitle;
    @BindView(R.id.text_attendee_count)
    TextView _textAttendeeCount;
    @BindView(R.id.text_event_notif)
    TextView _textEventNotif;

    @BindView(R.id.input_person_name)
    EditText _inputPersonName;
    @BindView(R.id.input_person_email)
    EditText _inputPersonEmail;
    @BindView(R.id.input_person_phone)
    EditText _inputPersonPhone;
    @BindView(R.id.input_person_org)
    EditText _inputPersonOrg;
    @BindView(R.id.input_person_address)
    EditText _inputPersonAddress;

    @BindView(R.id.spinner_person_gender)
    Spinner _spinnerPersonGender;
    @BindView(R.id.spinner_person_age)
    Spinner _spinnerPersonAge;
    @BindView(R.id.spinner_person_state)
    Spinner _spinnerPersonState;

    @BindView(R.id.btn_attendee_add)
    Button _btnAttendeeAdd;
    @BindView(R.id.btn_attendees_upload)
    Button _btnAttendeesUpload;
    @BindView(R.id.btn_attendee_next)
    Button _btnAttendeeNext;
    @BindView(R.id.btn_attendee_prev)
    Button _btnAttendeePrev;
    @BindView(R.id.btn_attendee_update)
    Button _btnAttendeeUpdate;
    @BindView(R.id.btn_attendee_delete)
    Button _btnAttendeeDelete;

    @BindView(R.id.layout_update_delete)
    LinearLayout _layoutUpdateDelete;

    private List<Attendee> attendees;
    private int attendeeCount = 0;
    private int attendeeIndex = -1;

    private List<String> statesList;
    private List<String> genderList;
    private List<String> agesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_attendee);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(EventAttendeeActivity.this, R.style.AppTheme_Dark_Dialog);
        attendees = new ArrayList<Attendee>();

        // gender spinner
        genderList = Arrays.asList(getResources().getStringArray(R.array.genders));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.genders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinnerPersonGender.setAdapter(adapter);

        // age bracket spinner
        agesList = Arrays.asList(getResources().getStringArray(R.array.ages));
        ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(this, R.array.ages, android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinnerPersonAge.setAdapter(ageAdapter);

        // state spinner
        final ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        try {
            String json = App.readAssetFile("all_nigerian_states.json");
            JSONArray states = null;

            states = new JSONArray(json);
            statesList = new ArrayList<>();

            for (int i = 0; i < states.length(); i++) {
                stateAdapter.add(states.getJSONObject(i).getString("state"));
                statesList.add(states.getJSONObject(i).getString("state"));
            }

            _spinnerPersonState.setAdapter(stateAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // show event title
        _textEventTitle.setText(ShPref.getString("event_title"));

        // disable the upload button
        _btnAttendeesUpload.setVisibility(View.GONE);

        /*_main.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e(TAG, "Main touched");
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                return true;
            }
        });*/
    }

    public boolean validate() {
        String name = _inputPersonName.getText().toString();
        String email = _inputPersonEmail.getText().toString();
        String phone = _inputPersonPhone.getText().toString();
        String org = _inputPersonOrg.getText().toString();
        String address = _inputPersonAddress.getText().toString();

        if (name.isEmpty()) {
            _inputPersonName.setError("enter attendee name");
            _inputPersonName.requestFocus();
            addFail("Enter attendee name");
            return false;
        } else {
            _inputPersonName.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _inputPersonEmail.setError("enter valid email address");
            _inputPersonEmail.requestFocus();
            addFail("Enter valid email address");
            return false;
        } else {
            _inputPersonEmail.setError(null);
        }

        if (phone.isEmpty()) {
            _inputPersonPhone.setError("enter attendee phone number");
            _inputPersonPhone.requestFocus();
            addFail("Enter attendee phone number");
            return false;
        } else {
            _inputPersonPhone.setError(null);
        }

        if (org.isEmpty()) {
            _inputPersonOrg.setError("enter attendee organization");
            _inputPersonOrg.requestFocus();
            addFail("Enter attendee organization");
            return false;
        } else {
            _inputPersonOrg.setError(null);
        }

        if (address.isEmpty()) {
            _inputPersonAddress.setError("enter attendee address");
            _inputPersonAddress.requestFocus();
            addFail("Enter attendee address");
            return false;
        } else {
            _inputPersonAddress.setError(null);
        }

        return true;
    }

    @OnClick(R.id.btn_attendee_add)
    void doAdd() {
        if (!validate()) return;
        boolean isNew = _btnAttendeeAdd.getText().toString().equalsIgnoreCase("New");

        if (!isNew) {
            attendees.add(
                    new Attendee(
                            _inputPersonName.getText().toString(),
                            _inputPersonEmail.getText().toString(),
                            _inputPersonPhone.getText().toString(),
                            _inputPersonOrg.getText().toString(),
                            _inputPersonAddress.getText().toString(),
                            _spinnerPersonGender.getSelectedItem().toString(),
                            _spinnerPersonAge.getSelectedItem().toString(),
                            _spinnerPersonState.getSelectedItem().toString()
                    )
            );

            _textAttendeeCount.setText(String.valueOf(++attendeeCount));

            if (_btnAttendeesUpload.getVisibility() == View.GONE)
                _btnAttendeesUpload.setVisibility(View.VISIBLE);
            _btnAttendeesUpload.setText(String.format("Upload All (%d)", attendeeCount));

            addFail("Attendee added");
            clearInputs();
            attendeeAdded();
        } else freshRecord();
    }

    void addFail(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
    }

    public boolean exists(String[] arr, int index) {
        try {
            String s = arr[index];
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private class Attendee {
        String name;
        String email;
        String phone;
        String org;
        String address;
        String gender;
        String age;
        String state;

        Attendee(String... data) {
            this.name = exists(data, 0) ? data[0] : "";
            this.email = exists(data, 1) ? data[1] : "";
            this.phone = exists(data, 2) ? data[2] : "";
            this.org = exists(data, 3) ? data[3] : "";
            this.address = exists(data, 4) ? data[4] : "";
            this.gender = exists(data, 5) ? data[5] : "";
            this.age = exists(data, 6) ? data[6] : "";
            this.state = exists(data, 7) ? data[7] : "";
        }
    }

    void clearInputs() {
        _inputPersonName.setText("");
        _inputPersonEmail.setText("");
        _inputPersonPhone.setText("");
        _inputPersonOrg.setText("");
        _inputPersonAddress.setText("");
    }

    void reset() {
        attendees.clear();
        attendeeCount = 0;
        _textAttendeeCount.setText(String.valueOf(0));
        _btnAttendeesUpload.setVisibility(View.GONE);
        _btnAttendeeNext.setVisibility(View.GONE);
        _btnAttendeePrev.setVisibility(View.GONE);
        _layoutUpdateDelete.setVisibility(View.GONE);
        _btnAttendeeAdd.setText("Add");

    }

    @OnClick(R.id.btn_attendees_upload)
    void doUpload() {
        if (attendeeCount < 1) {
            addFail("Add an attendee first");
            return;
        }

        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        String json = new Gson().toJson(attendees);

        RequestParams params = new RequestParams();
        params.add("event_id", Integer.toString(ShPref.getInt("event_id")));
        params.add("admin", ShPref.getString("admin"));
        params.add("data", json);

        Api.post("add-attendees", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

//                Log.e("MOFESOLA", response.toString());
                try {
                    if (response.getBoolean("status")) {
                        addFail("Records uploaded successfully");
                        clearInputs();
                        reset();
                    } else addFail(response.getString("errmsg"));
                } catch (JSONException e) {
                    addFail("Operation failed");
                }

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
//                Log.e("MOFESOLA", responseString);
                addFail("Connection failed");
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                addFail("Connection failed, check network");
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_Sweet_Dialog))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setTitle("Dismiss event")
                .setMessage("Exiting will clear all unuploaded data, are you sure you want to exit the page?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @OnClick(R.id.btn_attendee_next)
    void goNext() {
        if (attendeeIndex != -1 && attendeeIndex < attendees.size() - 1) {
            attendeeIndex++;
            loadAttendeeInfo();
        }
    }

    @OnClick(R.id.btn_attendee_prev)
    void goPrev() {
        if (attendees.size() > 0 && (attendeeIndex != 0)) {
            attendeeIndex = (attendeeIndex == -1 ? attendees.size() : attendeeIndex) - 1;
            loadAttendeeInfo();
        }
    }

    private void attendeeAdded() {
        _btnAttendeeNext.setVisibility(View.GONE);
        _btnAttendeePrev.setVisibility(View.VISIBLE);
    }

    private void loadAttendeeInfo() {
        Attendee att = attendees.get(attendeeIndex);
        _inputPersonName.setText(att.name);
        _inputPersonEmail.setText(att.email);
        _inputPersonPhone.setText(att.phone);
        _inputPersonOrg.setText(att.org);
        _inputPersonAddress.setText(att.address);
        _spinnerPersonGender.setSelection(genderList.indexOf(att.gender));
        _spinnerPersonAge.setSelection(agesList.indexOf(att.age));
        _spinnerPersonState.setSelection(statesList.indexOf(att.state));

        if (attendeeIndex <= 0) _btnAttendeePrev.setVisibility(View.GONE);
        else _btnAttendeePrev.setVisibility(View.VISIBLE);

        if (attendeeIndex != -1) _btnAttendeeAdd.setText("New");
        if (attendeeIndex != attendees.size() - 1) _btnAttendeeNext.setVisibility(View.VISIBLE);
        else _btnAttendeeNext.setVisibility(View.GONE);

        _layoutUpdateDelete.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_attendee_delete)
    void deleteAttendee() {
        attendees.remove(attendeeIndex);
        _textAttendeeCount.setText(String.valueOf(--attendeeCount));
        freshRecord();
    }

    @OnClick(R.id.btn_attendee_update)
    void updateAttendee() {
        Attendee att = attendees.get(attendeeIndex);
        att.name = _inputPersonName.getText().toString();
        att.email= _inputPersonEmail.getText().toString();
        att.phone = _inputPersonPhone.getText().toString();
        att.org = _inputPersonOrg.getText().toString();
        att.address = _inputPersonAddress.getText().toString();
        att.gender = _spinnerPersonGender.getSelectedItem().toString();
        att.age = _spinnerPersonAge.getSelectedItem().toString();
        att.state = _spinnerPersonState.getSelectedItem().toString();
        addFail("Attendee info updated");
    }

    private void freshRecord() {
        clearInputs();
        attendeeIndex = -1;
        if (attendees.size() > 0) _btnAttendeePrev.setVisibility(View.VISIBLE);
        _btnAttendeeNext.setVisibility(View.GONE);
        _btnAttendeeAdd.setText("Add");
        _layoutUpdateDelete.setVisibility(View.GONE);
        if (attendeeCount == 0) _btnAttendeesUpload.setVisibility(View.GONE);
    }
}
