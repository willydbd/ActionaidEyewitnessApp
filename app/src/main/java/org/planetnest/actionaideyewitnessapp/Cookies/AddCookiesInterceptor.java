package org.planetnest.actionaideyewitnessapp.Cookies;

import android.util.Log;

import net.alexandroid.shpref.ShPref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.prefs.Preferences;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This interceptor put all the Cookies in Preferences in the Request.
 * Your implementation on how to get the Preferences MAY VARY.
 * <p>
 * Created by tsuharesu on 4/1/15.
 */
public class AddCookiesInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
//        Log.e("MOFESOLA", "Nigga!");

        Request.Builder builder = chain.request().newBuilder();
        ArrayList<String> preferences = ShPref.getListOfStrings("COOKIES");

        for (String cookie : preferences) {
            builder.addHeader("Cookie", cookie);
//            Log.v("MOFESOLA-OkHttp", "Adding Header: " + cookie); // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
        }

        return chain.proceed(builder.build());
    }
}