package com.example.glucu.Authentication.GetTokens;

import android.content.Context;
import android.os.AsyncTask;

import com.example.glucu.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;


public class GetAccessToken extends AsyncTask<GetAccessTokenParams, Void, String> {

    private String CLIENT_ID;
    private String REDIRECT_URI;
    private String CLIENT_SECRET;
    private String GRANT_TYPE;
    private String TARGET_URL;


    private void readFromKeysJSON(Context context, String GRANT_TYPE, String TARGET_URL) throws JSONException {
        InputStream inputStreamKeys = context.getResources().openRawResource(R.raw.keys);
        String jsonStringKeys = new Scanner(inputStreamKeys).useDelimiter("\\A").next();
        JSONObject objectKeys = (JSONObject) new JSONTokener(jsonStringKeys).nextValue();
        CLIENT_ID = objectKeys.optString("client_id");
        REDIRECT_URI = objectKeys.optString("redirect_uri");
        CLIENT_SECRET = objectKeys.optString("client_secret");
        this.GRANT_TYPE = GRANT_TYPE;
        this.TARGET_URL = objectKeys.optString(TARGET_URL);
        System.out.println("Got everything from keys.json");
    }


    @Override
    protected String doInBackground(GetAccessTokenParams... accessTokenParams) {

        // Get parameters
        Context context = accessTokenParams[0].context;
        String GRANT_TYPE = accessTokenParams[0].GRANT_TYPE;
        String TARGET_URL = accessTokenParams[0].TARGET_URL;
        File filesDir = accessTokenParams[0].filesDir;

        // Create client to be used for both paths
        OkHttpClient client = new OkHttpClient();

        try {
            // Read things relevant to both paths from file
            readFromKeysJSON(context, GRANT_TYPE, TARGET_URL);

            if(GRANT_TYPE.equals("authorization_code")) {
                try {
                    // Get authorization_code stored after user authorization
                    File codesFile = new File(context.getFilesDir(), "codes.json");

                    InputStream inputStreamCodes = new FileInputStream(codesFile);
                    String jsonStringCodes = new Scanner(inputStreamCodes).useDelimiter("\\A").next();
                    JSONObject objectCodes = (JSONObject) new JSONTokener(jsonStringCodes).nextValue();
                    String AUTHORIZATION_CODE = objectCodes.optString("authorization_code");

                    // Build requests
                    RequestBody bodyBuilder = new FormBody.Builder()
                            .add("client_secret", CLIENT_SECRET)
                            .add("client_id", CLIENT_ID)
                            .add("code", AUTHORIZATION_CODE)
                            .add("grant_type", GRANT_TYPE)
                            .add("redirect_uri", REDIRECT_URI)
                            .build();

                    String mediaTypeString = "application/x-www-form-urlencoded";
                    MediaType mediaType = MediaType.parse(mediaTypeString);
                    RequestBody body = RequestBody.create(mediaType, requestbodyToString(bodyBuilder));

                    Request request = new Request.Builder()
                            .url(TARGET_URL)
                            .post(bodyBuilder)
                            .addHeader("content-type", mediaTypeString)
                            .addHeader("cache-control", "no-cache")
                            .build();

                    String targetUrl = request.url().toString() + bodyToString(request); // Log url
                    System.out.println("oof target url: " + targetUrl);

                    // Try making the request
                    try {
                        // Make the request
                        System.out.println("About to start request.");
                        Response response = client.newCall(request).execute();
                        System.out.println("Finished request.");
                        System.out.println("Request: " + targetUrl);
                        // Get response
                        String responseBodyString = response.body().string();
                        System.out.println("Response: " + responseBodyString); // Log response

                        // Put values from response into JSONObject
                        JSONObject responseBodyStringJSON = new JSONObject(responseBodyString);

                        objectCodes.put("access_token", responseBodyStringJSON.optString("access_token"));
                        objectCodes.put("expires_in", responseBodyStringJSON.optString("expires_in"));
                        objectCodes.put("token_type", responseBodyStringJSON.optString("token_type"));
                        objectCodes.put("refresh_token", responseBodyStringJSON.optString("refresh_token"));

                        // Write new JSONObject into file
                        File codes = new File(filesDir, "codes.json");
                        while(!codes.exists()) {
                            try {
                                codes.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        try (FileWriter file = new FileWriter(codes)) {
                            file.write(objectCodes.toString());
                            System.out.println("Successfully Copied JSON Object to File...");
                            System.out.println("\nJSON Object: " + objectCodes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        System.out.println("startBackgroundRefreshTokens being called now");
                        startBackgroundRefreshTokens(context);

                        return responseBodyString;
                    } catch (IOException ex) {
                        System.out.println("Oof. IOException - take a look! " + ex);
                    }

                    return "Error in getting access token.";
                } catch (FileNotFoundException ex) {
                    System.out.println("codes.json file not found: " + ex);
                }
            }

            if(GRANT_TYPE.equals("refresh_token")) {
                try {
                    // Get refresh_token stored after user authorization
                    File codesFile = new File(context.getFilesDir(), "codes.json");

                    InputStream inputStreamCodes = new FileInputStream(codesFile);
                    String jsonStringCodes = new Scanner(inputStreamCodes).useDelimiter("\\A").next();
                    JSONObject objectCodes = (JSONObject) new JSONTokener(jsonStringCodes).nextValue();
                    String REFRESH_TOKEN = objectCodes.optString("refresh_token");

                    // Build requests
                    RequestBody bodyBuilder = new FormBody.Builder()
                            .add("client_secret", CLIENT_SECRET)
                            .add("client_id", CLIENT_ID)
                            .add("refresh_token", REFRESH_TOKEN)
                            .add("grant_type", GRANT_TYPE)
                            .add("redirect_uri", REDIRECT_URI)
                            .build();

                    String mediaTypeString = "application/x-www-form-urlencoded";
                    MediaType mediaType = MediaType.parse(mediaTypeString);
                    RequestBody body = RequestBody.create(mediaType, requestbodyToString(bodyBuilder));

                    Request request = new Request.Builder()
                            .url(TARGET_URL)
                            .post(body)
                            .addHeader("content-type", mediaTypeString)
                            .addHeader("cache-control", "no-cache")
                            .build();

                    // Try making the request
                    try {
                        // Make the request
                        Response response = client.newCall(request).execute();
                        String targetUrl = request.url().toString() + bodyToString(request); // Log url
                        System.out.println("Request: " + targetUrl);
                        // Get response
                        String responseBodyString = response.body().string();
                        System.out.println("Response: " + responseBodyString); // Log response

                        // Put values from response into JSONObject
                        JSONObject responseBodyStringJSON = new JSONObject(responseBodyString);

                        objectCodes.put("access_token", responseBodyStringJSON.optString("access_token"));
                        objectCodes.put("expires_in", responseBodyStringJSON.optString("expires_in"));
                        objectCodes.put("token_type", responseBodyStringJSON.optString("token_type"));
                        objectCodes.put("refresh_token", responseBodyStringJSON.optString("refresh_token"));

                        // Write new JSONObject into file
                        File codes = new File(filesDir, "codes.json");
                        while(!codes.exists()) {
                            try {
                                codes.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        try (FileWriter file = new FileWriter(codes)) {
                            file.write(objectCodes.toString());
                            System.out.println("Successfully Copied JSON Object to File...");
                            System.out.println("\nJSON Object: " + objectCodes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        startBackgroundRefreshTokens(context);
                        return responseBodyString;
                    } catch (IOException ex) {
                        System.out.println("Oof. IOException - take a look! " + ex);
                    }

                    return "Error in getting access token.";
                } catch (FileNotFoundException ex) {
                    System.out.println("codes.json file not found: " + ex);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "Error in getting keys.json";

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

    private static String requestbodyToString(final RequestBody request){
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            copy.writeTo(buffer);
            return buffer.readUtf8();
        }
        catch (final IOException e) {
            return "did not work";
        }
    }

    private void startBackgroundRefreshTokens(Context context) {
        try {

            File codesFile = new File(context.getFilesDir(), "codes.json");

            InputStream inputStreamCodes = new FileInputStream(codesFile);
            String jsonStringCodes = new Scanner(inputStreamCodes).useDelimiter("\\A").next();
            JSONObject objectCodes = (JSONObject) new JSONTokener(jsonStringCodes).nextValue();
            long REFRESH_TOKEN = Long.parseLong(objectCodes.optString("expires_in"));
            System.out.println("REFRESH_TOKEN: " + Long.toString(REFRESH_TOKEN));

            Constraints constraints = new Constraints.Builder().setTriggerContentMaxDelay(REFRESH_TOKEN, TimeUnit.SECONDS).build();
            OneTimeWorkRequest refreshTokens = new OneTimeWorkRequest.Builder(BackgroundRefreshTokens.class)
                    .setInitialDelay(REFRESH_TOKEN - 100, TimeUnit.SECONDS)
                    .setConstraints(constraints)
                    .addTag("GetRefreshToken")
                    .build();

            System.out.println("Built new OneTimeWorkRequest");
            WorkManager.getInstance().enqueue(refreshTokens);

            System.out.println("Started new WorkManager"); // last line seen?
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }


}