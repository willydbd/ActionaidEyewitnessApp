package org.planetnest.actionaideyewitnessapp.Utils;

import android.content.Context;

import net.alexandroid.shpref.ShPref;
import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.planetnest.actionaideyewitnessapp.BuildConfig;

import java.net.MalformedURLException;

/**
 * @author Banjo Mofesola Paul
 *         Chief Developer, Planet NEST
 *         mofesolapaul@planetnest.org
 *         10/05/2017 19:38
 */

public class Uploader extends MultipartUploadRequest {

    public Uploader(Context context, String serverUrl) throws MalformedURLException, IllegalArgumentException {
        super(context, serverUrl);
    }

    @Override
    public String startUpload() {
        setMethod("POST");
        setUtf8Charset();
        setCustomUserAgent("NESTAgent/" + BuildConfig.VERSION_NAME);
        setUsesFixedLengthStreamingMode(true);

        setNotificationConfig(new UploadNotificationConfig());
        setAutoDeleteFilesAfterSuccessfulUpload(true);
        setMaxRetries(3);
        addParameter("username", ShPref.getString("LOGGED_email"));
        return super.startUpload();
    }
}
