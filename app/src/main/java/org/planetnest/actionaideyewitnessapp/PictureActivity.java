package org.planetnest.actionaideyewitnessapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.alexandroid.shpref.ShPref;
import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.planetnest.actionaideyewitnessapp.Utils.Uploader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;

public class PictureActivity extends UploadActivity {

    @BindView(R.id.img)
    ImageView img;
    @BindView(R.id.layout_action)
    LinearLayout layout_action;
    @BindView(R.id.desc_pane)
    LinearLayout desc_pane;
    @BindView(R.id.desc_text)
    EditText desc_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_picture);

        super.onCreate(savedInstanceState);
        takePicture();
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
                doUpload("picture");
                return true;
            case R.id.action_change:
                takePicture();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Opens camera for image capture
     */
    @OnClick(R.id.layout_action)
    void takePicture() {
        clearSelection();

        // delete existing image file
        Util.deleteFile(App.MEDIA_PATH);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File imgFile = Util.createImageFile();
            App.MEDIA_PATH = imgFile.getAbsolutePath();
//            Log.e("MOFESOLA-IMG", App.MEDIA_PATH);

//            Uri imgUri = Uri.fromFile(imgFile);
            Uri imgUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".org.planetnest.actionaideyewitnessapp.provider", imgFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
            intent.putExtra("return-data", true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, App.ACTION_TAKE_PICTURE);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Toast.makeText(this, "Could not start camera", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == App.ACTION_TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            setPic();
            //galleryAddPic();

            layout_action.setVisibility(View.GONE);
            desc_pane.setVisibility(View.VISIBLE);
        } else {

            Util.deleteFile(App.MEDIA_PATH);
            clearSelection();
            Toast.makeText(this, "Please capture an image", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Clears selected image
     */
    private void clearSelection() {
        App.MEDIA_PATH = "";
        img.setImageResource(0);
        layout_action.setVisibility(View.VISIBLE);
        desc_text.setText("");
        desc_pane.setVisibility(View.GONE);
    }

    /**
     * Add photo to gallery
     */
    private void galleryAddPic() {
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(App.MEDIA_PATH);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        } catch (Exception ex) {
            Util.deleteFile(App.MEDIA_PATH);
            clearSelection();
        }
    }

    /**
     * Renders captured image
     */
    private void setPic() {
        // Get the dimensions of the View
        int targetW = img.getWidth() | 720;
        int targetH = img.getHeight() | 1024;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(App.MEDIA_PATH, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

//        Log.e("MOFESOLA", String.format("target-height: %d, target-width: %d, photo-height: %d, photo-width: %d", targetH, targetW, photoH, photoW));

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        // bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(App.MEDIA_PATH, bmOptions);

        // attempt save (overwrite)
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(App.MEDIA_PATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        img.setImageBitmap(bitmap);
    }

    @Override
    void doUpload(String url_action) {
        if (App.MEDIA_PATH.isEmpty()) {
            Toast.makeText(this, "Snap a picture first", Toast.LENGTH_SHORT).show();
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
