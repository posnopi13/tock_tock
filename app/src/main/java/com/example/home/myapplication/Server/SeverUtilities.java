package com.example.home.myapplication.Server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


import android.app.Application;
import android.util.Log;
/**
 * Created by HOME on 2015-09-02.
 */
public class SeverUtilities {
    private Common common;
    private static final String TAG = "ServerUtilities";

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * Register this account/device pair within the server.
     */
    public static String check(final String email) {

        String serverUrl = Common.getServerUrl() + "/check";
        Log.i(TAG, "serverUrl : "+serverUrl);
        Map<String, String> params = new HashMap<String, String>();
        params.put(Common.EMAIL, email);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        try {
            return post(serverUrl, params, MAX_ATTEMPTS);
        } catch (IOException e) {
            Log.e("ServerUtilities",e.toString());
        }
        return null;
    }
    public static String register(final String email, final String regId) {
        Log.i(TAG, "registering device (regId = " + regId + ")");
        String serverUrl = Common.getServerUrl() + "/register";
        Log.i(TAG, "serverUrl : "+serverUrl);
        Map<String, String> params = new HashMap<String, String>();
        params.put(Common.EMAIL, email);
        params.put(Common.REDID, regId);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        try {
            return post(serverUrl, params, MAX_ATTEMPTS);
        } catch (IOException e) {
            Log.e("ServerUtilities",e.toString());
        }
        return null;
    }

    /**
     * Unregister this account/device pair within the server.
     */
    public static String unregister(final String email) {
        Log.i(TAG, "unregistering device (email = " + email + ")");
        String serverUrl = Common.getServerUrl() + "/unregister";
        Map<String, String> params = new HashMap<String, String>();
        params.put(Common.EMAIL, email);
        try {
            return post(serverUrl, params, MAX_ATTEMPTS);
        } catch (IOException e) {
            Log.e("ServerUtilities",e.toString());
        }
        return null;
    }

    /**
     * Send a message.
     */
    public static String send(final String msg ,final String take) throws IOException {
        //Log.i(TAG, "sending message (msg = " + msg + ")");
        String serverUrl = Common.getServerUrl() + "/send";
        Map<String, String> params = new HashMap<String, String>();
        //
        params.put(Common._MESSAGE, msg);//메세지
        params.put(Common._FROM, Common.getMyemail());//나
        params.put(Common._TO, take);//상대
        try {
            return post(serverUrl, params, MAX_ATTEMPTS);
        } catch (IOException e) {
            Log.e("ServerUtilities",e.toString());
        }
        return null;
    }

    /**
     * Create a group.
     */
    /*public static String create() {
        //Log.i(TAG, "creating group");
        String serverUrl = Common.getServerUrl() + "/group";
        Map<String, String> params = new HashMap<String, String>();

        try {
            return post(serverUrl, params, MAX_ATTEMPTS);
        } catch (IOException e) {
        }
        return null;
    }*/

    /**
     * Join a group.
     */
   /* public static void join(String to) throws IOException {
        //Log.i(TAG, "joining group");
        String serverUrl = Common.getServerUrl() + "/join";
        Map<String, String> params = new HashMap<String, String>();
        params.put(Common.FROM, Common.getChatId());
        params.put(Common.TO, to);

        post(serverUrl, params, MAX_ATTEMPTS);
    }*/

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     * @return response
     * @throws IOException propagated from POST.
     */
    private static String executePost(String endpoint, Map<String, String> params) throws IOException {
        URL url;
        String TAG_excutePOST = "executePost";
        StringBuffer response = new StringBuffer();
        String myResult;
        Log.i(TAG_excutePOST,"START");
        try {

            url = new URL(endpoint);
            Log.i(TAG_excutePOST,"open url : "+url);
        } catch (MalformedURLException e) {
            Log.i(TAG_excutePOST,"err : "+e.toString());
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Log.i(TAG_excutePOST,"body : "+body);
        HttpURLConnection conn = null;
        try {
            Log.i(TAG_excutePOST,"Http connection");
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(body.toString());
            writer.flush();
            Log.i(TAG_excutePOST, "Http connection : write");
            InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(tmp);
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                builder.append(str);                     // View에 표시하기 위해 라인 구분자 추가
            }
            myResult = builder.toString();
            Log.i(TAG_excutePOST,"myResult : " + myResult);

            if (myResult.equals("200") || myResult.equals("300")) {

            }
            else{
                throw new IOException("Post failed with error code ");
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        Log.i(TAG_excutePOST,"finish");
        return myResult;

    }

    /** Issue a POST with exponential backoff */
    private static String post(String endpoint, Map<String, String> params, int maxAttempts) throws IOException {
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(10);//랜덤값은 0.01초가 최대다
        for (int i = 1; i <= maxAttempts; i++) {
            Log.i(TAG+" post", "Attempt #" + i + " to connect");
            try {
                return executePost(endpoint, params);
            } catch (IOException e) {
                Log.e(TAG, "Failed on attempt " + i + ":" + e);
                if (i == maxAttempts) {
                    throw e;
                }
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                backoff *= 2;
            } catch (IllegalArgumentException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        return null;
    }



}


