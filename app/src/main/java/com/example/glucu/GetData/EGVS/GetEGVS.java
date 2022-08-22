package com.example.glucu.GetData.EGVS;

import android.content.Context;
import android.os.AsyncTask;

import com.example.glucu.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;


public class GetEGVS extends AsyncTask<GetEGVSParams, Void, String> {

    @Override
    protected String doInBackground(GetEGVSParams... getEGVSParams) {
        Context context = getEGVSParams[0].context;
        String START_DATE = getEGVSParams[0].START_DATE;
        String END_DATE = getEGVSParams[0].END_DATE;

        System.out.println("Start and end dates: " + START_DATE + " and " + END_DATE);

        try {
            File codesFile = new File(context.getFilesDir(), "codes.json");

            InputStream inputStreamCodes = new FileInputStream(codesFile);
            String jsonStringCodes = new Scanner(inputStreamCodes).useDelimiter("\\A").next();
            JSONObject objectCodes = (JSONObject) new JSONTokener(jsonStringCodes).nextValue();
            String ACCESS_TOKEN = objectCodes.optString("access_token");

            InputStream inputStreamKeys = context.getResources().openRawResource(R.raw.keys);
            String jsonStringKeys = new Scanner(inputStreamKeys).useDelimiter("\\A").next();
            JSONObject objectKeys = (JSONObject) new JSONTokener(jsonStringKeys).nextValue();
            String TARGET_URL = objectKeys.optString("egvs_target_url");

            OkHttpClient client = new OkHttpClient();

            String completeURL = TARGET_URL + "startDate=" + START_DATE + "&" + "endDate=" + END_DATE;

            Request request = new Request.Builder()
                    .url(completeURL)
                    .get()
                    .addHeader("authorization", "Bearer " + ACCESS_TOKEN)
                    .build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();

            return responseString;
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return "Error. :(";
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

}