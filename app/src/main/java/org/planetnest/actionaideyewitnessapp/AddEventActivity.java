package org.planetnest.actionaideyewitnessapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import net.alexandroid.shpref.ShPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import cz.msebera.android.httpclient.Header;

public class AddEventActivity extends BaseActivity {
    private static final String TAG = "AddEventActivity";
    private ProgressDialog progressDialog;

    @BindView(R.id.spinner_event_type)
    Spinner _spinnerEventType;
    @BindView(R.id.spinner_event_state)
    Spinner _spinnerEventState;
    @BindView(R.id.spinner_event_lga)
    Spinner _spinnerEventLga;
    @BindView(R.id.input_event_title)
    EditText _inputEventTitle;
    @BindView(R.id.input_event_obj)
    EditText _inputEventObj;
    @BindView(R.id.input_event_budget)
    EditText _inputEventBudget;
    @BindView(R.id.input_event_sponsor)
    EditText _inputEventSponsor;
    @BindView(R.id.input_event_community)
    EditText _inputEventCommunity;
    @BindView(R.id.input_event_type)
    EditText _inputEventType;
    @BindView(R.id.txt_event_start)
    TextView _txtEventStart;
    @BindView(R.id.txt_event_end)
    TextView _txtEventEnd;
    @BindView(R.id.btn_event_send)
    Button _btnEventSend;
    @BindView(R.id.main)
    ScrollView _main;
    @BindView(R.id.other_event_type)
    TextInputLayout _otherEventType;
    @BindView(R.id.pane_start_date)
    TextInputLayout _paneStartDate;

    private JSONArray states;
    private Calendar _startDate;
    private String _dateStart = "", _dateEnd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(AddEventActivity.this, R.style.AppTheme_Dark_Dialog);

        // event type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinnerEventType.setAdapter(adapter);

        // state spinner
        final ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        String json = App.readAssetFile("all_nigerian_states.json");

        // lga spinner adapter
        final ArrayAdapter<String> lgaAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        lgaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinnerEventLga.setAdapter(lgaAdapter);

        try {
            states = new JSONArray(json);
            ArrayList<String> statesList = new ArrayList<>();
            for (int i = 0; i < states.length(); i++)
                statesList.add(states.getJSONObject(i).getString("state"));

            _spinnerEventState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    lgaAdapter.clear();
                    try {
                        JSONArray a = states.getJSONObject(position).getJSONArray("lgas");
                        for (int i = 0; i < a.length(); i++) lgaAdapter.add(a.getString(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            stateAdapter.addAll(statesList);
            _spinnerEventState.setAdapter(stateAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean validate() {
        String ttl = _inputEventTitle.getText().toString();
        String obj = _inputEventObj.getText().toString();
        String otherType = _inputEventType.getText().toString();
        boolean isOther = _spinnerEventType.getSelectedItem().toString().equals("Others");

        if (isOther && otherType.isEmpty()) {
            _inputEventType.setError("enter event type");
            _inputEventType.requestFocus();
            fail("Enter event type");
            return false;
        } else {
            _inputEventType.setError(null);
        }

        if (ttl.isEmpty()) {
            _inputEventTitle.setError("enter event title");
            _inputEventTitle.requestFocus();
            fail("Enter event title");
            return false;
        } else {
            _inputEventTitle.setError(null);
        }

        if (obj.isEmpty()) {
            _inputEventObj.setError("enter event objective");
            _inputEventObj.requestFocus();
            fail("Enter event objective");
            return false;
        } else {
            _inputEventObj.setError(null);
        }

        if (_spinnerEventLga.getSelectedItem() == null) {
            _spinnerEventState.requestFocus();
            fail("Select local government");
            return false;
        } else {
            _inputEventObj.setError(null);
        }

        if (_dateStart.isEmpty()) {
            fail("Select start date");
            return false;
        } else {
            _txtEventStart.setError(null);
        }

        if (_dateEnd.isEmpty()) {
            fail("Select end date");
            return false;
        } else {
            _txtEventEnd.setError(null);
        }

        return true;
    }

    public void fail(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    @OnClick(R.id.btn_event_send)
    void doAdd() {
        if (!validate()) return;

        boolean isOther = _spinnerEventType.getSelectedItem().toString().equals("Others");

        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Event...");
        progressDialog.show();

        RequestParams params = new RequestParams();
        params.add("title", _inputEventTitle.getText().toString());
        params.add("type", isOther ? _inputEventType.getText().toString() : _spinnerEventType.getSelectedItem().toString());
        params.add("obj", _inputEventObj.getText().toString());
        params.add("budget", _inputEventBudget.getText().toString());
        params.add("sponsor", _inputEventSponsor.getText().toString());
        params.add("state", _spinnerEventState.getSelectedItem().toString());
        params.add("lga", _spinnerEventLga.getSelectedItem().toString());
        params.add("comm", _inputEventCommunity.getText().toString());
        params.add("start_date", _txtEventStart.getText().toString());
        params.add("end_date", _txtEventEnd.getText().toString());
        params.add("admin", ShPref.getString("admin"));

        Api.post("create-event", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

//                Log.e("MOFESOLA", response.toString());
                try {
                    if (response.getBoolean("status")) {
                        ShPref.put("event_id", response.getInt("event_id"));
                        ShPref.put("event_title", _inputEventTitle.getText().toString());

                        Intent intent = new Intent(AddEventActivity.this, EventAttendeeActivity.class);
                        startActivity(intent);

                        fail("Event created successfully");
                        finish();
                    } else fail(response.getString("errmsg"));
                } catch (JSONException e) {
                    fail("Operation failed");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
//                Log.e("MOFESOLA", responseString);
                fail("Connection failed");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                fail("Connection failed, check network");
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_Sweet_Dialog))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setTitle("Confirm action")
                .setMessage("Are you sure you want to exit event registration?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @OnClick(R.id.txt_event_start)
    void pickStartDate() {
//        Log.e(TAG, "start date pick");
        _startDate = Calendar.getInstance();
        DatePickerDialog datePicker = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                monthOfYear++;
                _startDate.set(Calendar.YEAR, year);
                _startDate.set(Calendar.MONTH, monthOfYear);
                _startDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                _dateStart = String.format("%d-%s-%s", year, prependZero(monthOfYear), prependZero(dayOfMonth));
                _txtEventStart.setText(_dateStart);

                _dateEnd = "";
                _txtEventEnd.setText(_dateEnd);
            }
        }, _startDate.get(Calendar.YEAR), _startDate.get(Calendar.MONTH), _startDate.get(Calendar.DAY_OF_MONTH));
        datePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        datePicker.setMinDate(Calendar.getInstance());
        datePicker.show(getFragmentManager(), "DatepickerDialog");
    }

    @OnClick(R.id.txt_event_end)
    void pickEndDate() {
        if (_startDate == null) {
            fail("Select start date first");
            return;
        }

        Calendar c = Calendar.getInstance();
        DatePickerDialog datePicker = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                monthOfYear++;
                _dateEnd = String.format("%d-%s-%s", year, prependZero(monthOfYear), prependZero(dayOfMonth));
                _txtEventEnd.setText(_dateEnd);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        c = (Calendar) _startDate.clone();
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) - 1);
        datePicker.setMinDate(c);
        datePicker.show(getFragmentManager(), "DatepickerDialog");
    }

    @OnItemSelected(R.id.spinner_event_type)
    void eventTypeSpecified(AdapterView<?> parent, View view, int position, long id) {
        _otherEventType.setVisibility(
                _spinnerEventType.getAdapter().getItem(position).toString().equals("Others") ?
                        View.VISIBLE : View.GONE
        );
    }

    String prependZero(int value) {
        String v = String.valueOf(value);
        return value < 10 ? "0" + v : v;
    }
}
