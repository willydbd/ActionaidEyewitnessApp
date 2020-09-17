package org.planetnest.actionaideyewitnessapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import net.alexandroid.shpref.ShPref;
import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.planetnest.actionaideyewitnessapp.Utils.Uploader;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTouch;

public class VideoActivity extends UploadActivity {

    @BindView(R.id.vid)
    VideoView vid;
    @BindView(R.id.layout_action)
    LinearLayout layout_action;
    @BindView(R.id.desc_pane)
    LinearLayout desc_pane;
    @BindView(R.id.desc_text)
    EditText desc_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_video);

        super.onCreate(savedInstanceState);
        recordVideo();
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
                doUpload("video");
                return true;
            case R.id.action_change:
                recordVideo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Opens camera for video recording
     */
    @OnClick(R.id.layout_action)
    void recordVideo() {
        clearSelection();

        // delete existing image file
        Util.deleteFile(App.MEDIA_PATH);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        try {
            File videoFile = Util.createVideoFile();
            App.MEDIA_PATH = videoFile.getAbsolutePath();
//            Log.e("MOFESOLA-VID", App.MEDIA_PATH);

//            Uri imgUri = Uri.fromFile(videoFile);
            Uri imgUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".org.planetnest.actionaideyewitnessapp.provider", videoFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, App.ACTION_TAKE_VIDEO);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Toast.makeText(this, "Could not start camera", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == App.ACTION_TAKE_VIDEO && resultCode == Activity.RESULT_OK) {
            vid.setVideoPath(App.MEDIA_PATH);
            vid.start();
            galleryAddVid();

            layout_action.setVisibility(View.GONE);
            desc_pane.setVisibility(View.VISIBLE);
        } else {

            Util.deleteFile(App.MEDIA_PATH);
            clearSelection();
            Toast.makeText(this, "Please record a video", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Clears selected media
     */
    private void clearSelection() {
        App.MEDIA_PATH = "";

        layout_action.setVisibility(View.VISIBLE);
        desc_text.setText("");
        desc_pane.setVisibility(View.GONE);
    }

    /**
     * Add video to gallery
     */
    private void galleryAddVid() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(App.MEDIA_PATH);
        Uri contentUri = Uri.fromFile(f);
//        Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".org.planetnest.actionaideyewitnessapp.provider", f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    void doUpload(String url_action) {
        if (App.MEDIA_PATH.isEmpty()) {
            Toast.makeText(this, "Record a video first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (desc_text.getText().toString().isEmpty()) {
            Toast.makeText(this, "Description is mandatory for upload", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String uploadId = new Uploader(this, App.SERVER + "?action=" + url_action)

                    .addFileToUpload(App.MEDIA_PATH, "file")
                    .addParameter("description", desc_text.getText().toString())
                    .setDelegate(uploadStatusDelegate)
                    .startUpload();

            Toast.makeText(this, "Upload started", Toast.LENGTH_SHORT).show();

            // reset stuff
            clearSelection();

        } catch (Exception e) {
//            Log.e("MOFE-UPLOAD", e.getMessage());
            Toast.makeText(this, "Upload failed, try again", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
