package org.planetnest.actionaideyewitnessapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;

import butterknife.ButterKnife;

/**
 * @author Banjo Mofesola Paul
 *         Chief Developer, Planet NEST
 *         mofesolapaul@planetnest.org
 *         24/04/2017 10:03
 */

public abstract class UploadActivity extends BaseActivity {

    protected UploadStatusDelegate uploadStatusDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.MEDIA_PATH = "";
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Util.setContext(this);
        uploadStatusDelegate = new UploadStatusDelegate() {
            @Override
            public void onProgress(Context context, UploadInfo uploadInfo) {
            }

            @Override
            public void onError(Context context, UploadInfo uploadInfo, Exception exception) {
                try {
                    MainActivity.self.toast("Upload failed");
                } catch (Exception ex) {
                }
            }

            @Override
            public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
//                Log.e("MOFESOLA", serverResponse.getBodyAsString());
                try {
                    if (serverResponse.getHttpCode() == 200) MainActivity.self.toast("Upload completed successfuly");
                    else MainActivity.self.toast("Upload failed mysteriously");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                /*for (String file :
                        uploadInfo.getSuccessfullyUploadedFiles()) {
                    File f = new File(file);
                    if (f.exists()) f.delete();
                }*/
            }

            @Override
            public void onCancelled(Context context, UploadInfo uploadInfo) {
                try {
                    MainActivity.self.toast("Upload canceled");
                } catch (Exception ex) {
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        // no useless files hanging around
        if (!App.MEDIA_PATH.isEmpty()) Util.deleteFile(App.MEDIA_PATH);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // no useless files hanging around
        if (!App.MEDIA_PATH.isEmpty()) Util.deleteFile(App.MEDIA_PATH);
        super.onDestroy();
    }

    /**
     * Performs upload of media
     *
     * @param url_action String Extra GET value specifying desired API action
     */
    abstract void doUpload(String url_action);
}
