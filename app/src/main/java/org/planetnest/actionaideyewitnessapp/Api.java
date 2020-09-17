package org.planetnest.actionaideyewitnessapp;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * @author Banjo Mofesola Paul
 *         Chief Developer, Planet NEST
 *         mofesolapaul@planetnest.org
 *         30/04/2017 17:58
 */

public class Api {
    private static String BASE_URL = App.SERVER;
    public static AsyncHttpClient client = new AsyncHttpClient();

    public static void post(String action, RequestParams params, AsyncHttpResponseHandler handler) {
        client.post(getAbsoluteUrl(action), params, handler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + "?action=" + relativeUrl;
    }
}
