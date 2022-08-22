package com.example.glucu.Authentication.UserAuthorization;

import android.content.Context;

import com.example.glucu.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

public class DoUserAuthorization {

    public static String obtainUserAuthorization(Context context) {

        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.keys);
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
            JSONObject object = (JSONObject) new JSONTokener(jsonString).nextValue();
            String CLIENT_ID = object.optString("client_id");
            String REDIRECT_URI = object.optString("redirect_uri");
            String RESPONSE_TYPE = object.optString("response_type");
            String SCOPE = object.optString("scope");
            String TARGET_URL = object.optString("authentication_target_url");

            RequestBody formBody = new FormBody.Builder()
                    .add("client_id", CLIENT_ID)
                    .add("redirect_uri", REDIRECT_URI)
                    .add("response_type", RESPONSE_TYPE)
                    .add("scope", SCOPE)
                    .build();

            Request request = new Request.Builder()
                    .url(TARGET_URL)
                    .post(formBody)
                    .build();

            String targetUrl = request.url().toString() + bodyToString(request);
            System.out.println("targetUrl: " + targetUrl);
            return targetUrl;

        } catch (JSONException ex) {
            System.out.println("JSONException. Looks like nothing is working:" + ex);
            return "Something went wrong.";
        }

    }

    private static String bodyToString(final Request request){

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "ERROR method bodyToString(Request request) did not work";
        }
    }

}