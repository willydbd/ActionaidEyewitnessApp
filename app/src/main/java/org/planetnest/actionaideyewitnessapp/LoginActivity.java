package org.planetnest.actionaideyewitnessapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ProgressDialog progressDialog;
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_phone) EditText _phoneText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;
    @BindView(R.id.link_reset_password) TextView _resetPasswordLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        _resetPasswordLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Attempt password reset
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        if (!validateEmail()) return;

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Recovering phone number...");
        progressDialog.show();

        RequestParams params = new RequestParams();

        params.add("email", _emailText.getText().toString());

        Api.post("reset-password", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    onLoginFailed(response.getString("data"));
                } catch (JSONException e) {
                    onLoginFailed("Phone number retrieval failed mysteriously");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onLoginFailed("Connection failed");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onLoginFailed("Connection failed, check network");
            }
        });
    }

    public void login() {
        if (!validate()) return;

        _loginButton.setEnabled(false);

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        doLogin();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                // By default we just finish the Activity and log them in automatically
                Toast.makeText(getBaseContext(), "Signup successful", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(JSONObject jObj) {
        progressDialog.dismiss();
        _loginButton.setEnabled(true);

        try {
            App.setLogged(_emailText.getText().toString(), jObj.getString("name"), jObj.getString("phone"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finish();
    }

    public void onLoginFailed(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

        progressDialog.dismiss();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        if (!validateEmail()) return false;

        String phone = _phoneText.getText().toString();
        if (phone.isEmpty()) {
            _phoneText.setError("enter your phone number");
            onLoginFailed("Enter your phone number");
            return false;
        } else {
            _phoneText.setError(null);
        }

        return true;
    }

    private boolean validateEmail() {
        String email = _emailText.getText().toString();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            onLoginFailed("Enter a valid email address");
            return false;
        } else {
            _emailText.setError(null);
            return true;
        }
    }

    private void doLogin() {
        RequestParams params = new RequestParams();

        params.add("email", _emailText.getText().toString());
        params.add("phone", _phoneText.getText().toString());

        Api.post("login", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

//                Log.e("MOFESOLA", response.toString());
                try {
                    if (response.getBoolean("status")) onLoginSuccess(response.getJSONObject("userdata"));
                    else onLoginFailed(response.getString("errmsg"));
                } catch (JSONException e) {
                    onLoginFailed("Login failed mysteriously");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
//                Log.e("MOFESOLA", responseString);
                onLoginFailed("Connection failed");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onLoginFailed("Connection failed, check network");
            }
        });
    }
}
