package org.planetnest.actionaideyewitnessapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private ProgressDialog progressDialog;

    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_mobile) EditText _mobileText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
    }

    @OnClick(R.id.btn_signup)
    void signup() {
//        Log.d(TAG, "Signup");

        if (!validate()) {
            return;
        }

        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        // String reEnterPassword = _reEnterPasswordText.getText().toString();

        // TODO: Implement your own signup logic here.
        doSignup();
    }


    public void onSignupSuccess() {
        progressDialog.dismiss();
        App.setLogged(_emailText.getText().toString(), _nameText.getText().toString(), _mobileText.getText().toString());
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    public boolean validate() {
        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            onSignupFailed("Name should be at least 3 characters");
            return false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            onSignupFailed("Enter a valid email address");
            return false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()<11 || mobile.length()>14) {
            _mobileText.setError("Enter Valid Mobile Number");
            onSignupFailed("Enter Valid Mobile Number");
            return false;
        } else {
            _mobileText.setError(null);
        }

        return true;
    }

    @OnClick(R.id.link_login)
    void gotoLogin() {
        // Finish the registration screen and return to the Login activity
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private void doSignup() {
        RequestParams params = new RequestParams();
        params.add("name", _nameText.getText().toString());
        params.add("email", _emailText.getText().toString());
        params.add("phone", _mobileText.getText().toString());

        Api.post("signup", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    if (response.getBoolean("status")) onSignupSuccess();
                    else onSignupFailed(response.getString("errmsg"));
                } catch (JSONException e) {
                    onSignupFailed("Sign up failed mysteriously");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
//                Log.e(TAG, responseString);
                onSignupFailed("Connection failed");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onSignupFailed("Connection failed, check network");
            }
        });
    }

    @Override
    public void onBackPressed() {
        gotoLogin();
    }
}