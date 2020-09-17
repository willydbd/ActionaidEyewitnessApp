package org.planetnest.actionaideyewitnessapp;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Banjo Mofesola Paul
 *         Chief Developer, Planet NEST
 *         mofesolapaul@planetnest.org
 *         24/04/2017 08:14
 */

public class Util {

    private static Context context;

    public static void setContext(Context ctx) {
        context = ctx;
    }

    /**
     * Creates an empty image file
     * @return
     * @throws IOException
     */
    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String imageFileName = "Event_capture_app_" + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Creates an empty video file
     * @return
     * @throws IOException
     */
    public static File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String vidFileName = "Event_capture_app_" + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File vid = File.createTempFile(
                vidFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        return vid;
    }

    /**
     * Creates an empty audio file
     * @return
     * @throws IOException
     */
    public static File createAudioFile() throws IOException {
        // Create an image file name
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String audioFileName = "Event_capture_app_" + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File aud = File.createTempFile(
                audioFileName,  /* prefix */
                ".amr",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        return aud;
    }

    /**
     * Deletes specified file
     * @param path
     */
    public static void deleteFile(String path) {
        if (path.isEmpty()) return;

        File f = new File(path);
        if (f.exists()) f.delete();
    }

}
