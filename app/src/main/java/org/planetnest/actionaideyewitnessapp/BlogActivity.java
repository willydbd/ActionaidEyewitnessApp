package org.planetnest.actionaideyewitnessapp;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import net.alexandroid.shpref.ShPref;
import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.planetnest.actionaideyewitnessapp.Utils.Uploader;

import butterknife.BindView;

public class BlogActivity extends UploadActivity {

    @BindView(R.id.issues_identified)
    EditText issues_identified;
    @BindView(R.id.directly_affected)
    EditText directly_affected;
    @BindView(R.id.indirectly_affected)
    EditText indirectly_affected;
    @BindView(R.id.estimated_popu)
    EditText estimated_popu;
    @BindView(R.id.impact_aan)
    EditText impact_aan;
    @BindView(R.id.people_benefit)
    EditText people_benefit;
    @BindView(R.id.expected_output)
    EditText expected_output;
    @BindView(R.id.expected_outcome)
    EditText expected_outcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_blog);

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_upload:
                doUpload("text");
                return true;
            case R.id.action_change:
                clearInputs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearInputs() {
        issues_identified.setText("");
        directly_affected.setText("");
        indirectly_affected.setText("");
        estimated_popu.setText("");
        impact_aan.setText("");
        people_benefit.setText("");
        expected_output.setText("");
        expected_outcome.setText("");
    }

    @Override
    void doUpload(String url_action) {
        if (issues_identified.getText().toString().isEmpty()) {
            Toast.makeText(this, "Describe the issue you identified first", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            String uploadId = new Uploader(this, App.SERVER + "?action=" + url_action)

                    .addParameter("blog_title", issues_identified.getText().toString())
                    .addParameter("Issues Identified", issues_identified.getText().toString())
                    .addParameter("Group of people mostly/directly affected by the identified problem", directly_affected.getText().toString())
                    .addParameter("Group of people indirectly affected", indirectly_affected.getText().toString())
                    .addParameter("Estimated population affected", estimated_popu.getText().toString())
                    .addParameter("What Did/should AAN do", impact_aan.getText().toString())
                    .addParameter("How many people will/has benefit(ed) from AAN intervention", people_benefit.getText().toString())
                    .addParameter("What is/are the outputs/expected output", expected_output.getText().toString())
                    .addParameter("What is/are the outcome(s)/expected outcome", expected_outcome.getText().toString())
                    .setNotificationConfig(new UploadNotificationConfig().setTitle("Sending blog post").setErrorMessage("Error uploading blog post"))
                    .setDelegate(uploadStatusDelegate)
                    .startUpload();

            Toast.makeText(this, "Upload started", Toast.LENGTH_SHORT).show();

            // reset stuff
            clearInputs();

        } catch (Exception e) {
//            Log.e("MOFE-UPLOAD", e.getMessage());
            Toast.makeText(this, "Upload failed, try again", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
