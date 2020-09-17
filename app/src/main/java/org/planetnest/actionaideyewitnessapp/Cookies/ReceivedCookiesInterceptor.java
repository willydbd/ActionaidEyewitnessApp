package org.planetnest.actionaideyewitnessapp.Cookies;

import net.alexandroid.shpref.ShPref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.prefs.Preferences;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * This Interceptor add all received Cookies to the app DefaultPreferences.
 * Your implementation on how to save the Cookies on the Preferences MAY VARY.
 * <p>
 * Created by tsuharesu on 4/1/15.
 */
public class ReceivedCookiesInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            ArrayList<String> cookies = new ArrayList<>();

            for (String header : originalResponse.headers("Set-Cookie")) {
                cookies.add(header);
            }

            ShPref.putList("COOKIES", cookies);
        }

        return originalResponse;
    }
}