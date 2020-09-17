package org.planetnest.actionaideyewitnessapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import net.alexandroid.shpref.ShPref;
import net.gotev.uploadservice.Logger;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import org.planetnest.actionaideyewitnessapp.Cookies.AddCookiesInterceptor;
import org.planetnest.actionaideyewitnessapp.Cookies.ReceivedCookiesInterceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Banjo Mofesola Paul
 *         Chief Developer, Planet NEST
 *         mofesolapaul@planetnest.org
 *         23/04/2017 20:04
 */

public class App extends Application {
    private final static String TAG = "App";
    private final static boolean LOCAL = false;

    public static String PATH = "";
    public static String SERVER = LOCAL? "http://192.168.43.250/actionaid/eyewitness-app/api.php":"http://01a5b.now.ng/api.php";
    public static String MEDIA_PATH = "";
    public static final int ACTION_TAKE_PICTURE = 1;
    public static final int ACTION_TAKE_VIDEO = 2;
    public static boolean doubleBackToExitPressedOnce = false;
    public static Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();

        // AsyncHttp Setup
        Api.client.addHeader("User-Agent", "De-paule AsyncHttp Client");

        // Preferences
        ShPref.init(this, ShPref.APPLY);

        // setup the broadcast action namespace string which will
        // be used to notify upload status.
        // Gradle automatically generates proper variable as below.
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

        // Set upload service debug log messages level
        Logger.setLogLevel(Logger.LogLevel.DEBUG);

        // OkHttpStack
        UploadService.HTTP_STACK = new OkHttpStack(getOkHttpClient()); // a new client will be automatically created

        // file path
        PATH = Environment.getExternalStorageDirectory() + "/ActionAidEvtCapture";

        // Gson
        gson = new Gson();
    }

    @NonNull
    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)

                // Retrofit/OkHttp
                .addInterceptor(new AddCookiesInterceptor())
                .addInterceptor(new ReceivedCookiesInterceptor())

                // you can add your own request interceptors to add authorization headers.
                // do not modify the body or the http method here, as they are set and managed
                // internally by Upload Service, and tinkering with them will result in strange,
                // erroneous and unpredicted behaviors
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request.Builder request = chain.request().newBuilder();

                        return chain.proceed(request.build());
                    }
                })

                .cache(null)
                .build();
    }

    public static void setLogged(String email, String name, String phone) {
        ShPref.put("LOGGED_email", email);
        ShPref.put("LOGGED_name", name);
        ShPref.put("LOGGED_phone", phone);
    }

    public static String readAssetFile(String filename) {
        BufferedReader reader = null;
        String bulk = "";
        try {
            reader = new BufferedReader(new InputStreamReader(MainActivity.self.getAssets().open(filename), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) bulk += line;
        } catch (IOException e) {
//            Log.e(TAG, e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        return bulk;
    }
}
