package org.planetnest.actionaideyewitnessapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.alexandroid.shpref.ShPref;
import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.planetnest.actionaideyewitnessapp.Utils.Uploader;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;

public class AudioActivity extends UploadActivity {

    private static final int REST = 557;
    private static final int RECORDING = 320;
    private static final int RECORDED = 59;
    private static final int APP_PERMISSION_RECORD_AUDIO = 507;

    private int state = REST;
    MediaRecorder recorder = new MediaRecorder();
    MediaPlayer player;

    @BindView(R.id.rec_icon)
    ImageView rec_icon;
    @BindView(R.id.rec_info)
    TextView rec_info;
    @BindView(R.id.playback_controls)
    LinearLayout playback_controls;
    @BindView(R.id.play_btn)
    ImageView play_btn;
    @BindView(R.id.stop_btn)
    ImageView stop_btn;
    @BindView(R.id.desc_pane)
    LinearLayout desc_pane;
    @BindView(R.id.desc_text)
    EditText desc_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_audio);

        super.onCreate(savedInstanceState);

        // check permission
        if (hasPermission()) setUpRecorder();
        else requestPermission();
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
                doUpload("audio");
                return true;
            case R.id.action_change:
                startRecording();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks for audio_record permission
     *
     * @return
     */
    private boolean hasPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests audio_record permission
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, APP_PERMISSION_RECORD_AUDIO);
    }

    /**
     * Sets up the media recorder for audio capture
     */
    private void setUpRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    @OnClick(R.id.layout_action)
    void toggleState() {
        switch (state) {
            case REST:
            case RECORDED:
                startRecording();
                break;
            case RECORDING:
                stopRecording();
                break;
        }
    }

    /**
     * Starts recording
     */
    void startRecording() {

        if (!hasPermission()) {
            requestPermission();
            return;
        }

        if (state == RECORDING) {
            Toast.makeText(this, "Cannot refresh recorder, stop recording first", Toast.LENGTH_LONG).show();
            return;
        }

        resetPlayback();
        if (state == RECORDED) Util.deleteFile(App.MEDIA_PATH);

        setUpRecorder();

        try {
            File audioFile = Util.createAudioFile();
            App.MEDIA_PATH = audioFile.getAbsolutePath();
//            Log.e("MOFESOLA-AUD", App.MEDIA_PATH);

//            Uri audUri = Uri.fromFile(audioFile);
            Uri audUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".org.planetnest.actionaideyewitnessapp.provider", audioFile);
            recorder.setOutputFile(App.MEDIA_PATH);

            recorder.prepare();
            recorder.start();

            state = RECORDING;
            rec_icon.setImageResource(R.mipmap.microphone_white_512);
            rec_info.setText("Recording...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops recording
     */
    void stopRecording() {
        recorder.stop();

        state = RECORDED;
        rec_icon.setImageResource(R.mipmap.microphone_pink_512);
        rec_info.setText("Audio recorded");

        playback_controls.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case APP_PERMISSION_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpRecorder();
                    Toast.makeText(this, "Permission granted, you may now record", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "App needs your permission to record audio", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Resets playback controls
     */
    public void resetPlayback() {
        play_btn.setImageResource(R.mipmap.play_white_512);
        stop_btn.setImageResource(R.mipmap.stop_pink_512);
        playback_controls.setVisibility(View.GONE);
    }

    @OnClick(R.id.play_btn)
    void playBtnClick() {
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                play_btn.setImageResource(R.mipmap.play_white_512);
                stop_btn.setImageResource(R.mipmap.stop_pink_512);
            }
        });

        try {
            player.setDataSource(App.MEDIA_PATH);
            player.prepare();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        player.start();
        play_btn.setImageResource(R.mipmap.play_pink_512);
        stop_btn.setImageResource(R.mipmap.stop_white_512);
    }

    @OnClick(R.id.stop_btn)
    void stopBtnClick() {
        if (player == null || !player.isPlaying()) return;

        player.stop();
        player.release();

        play_btn.setImageResource(R.mipmap.play_white_512);
        stop_btn.setImageResource(R.mipmap.stop_pink_512);
    }

    /**
     * Clears selected image
     */
    private void clearSelection() {
        App.MEDIA_PATH = "";
        desc_text.setText("");
        desc_pane.setVisibility(View.GONE);
        resetPlayback();
        state = REST;
        rec_info.setText("Tap to record audio");
    }

    @Override
    void doUpload(String url_action) {
        if (App.MEDIA_PATH.isEmpty()) {
            Toast.makeText(this, "Record audio first", Toast.LENGTH_SHORT).show();
            return;
        }

        // show description pane
        if (desc_pane.getVisibility() != View.VISIBLE) {
            desc_pane.setVisibility(View.VISIBLE);
            playback_controls.setVisibility(View.GONE);
            return;
        }

        // require description text
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
