package com.example.glucu.Authentication.GetTokens;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.glucu.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class BackgroundRefreshTokens extends Worker {

    public BackgroundRefreshTokens(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        try {
            InputStream inputStreamKeys = context.getResources().openRawResource(R.raw.keys);
            String jsonStringKeys = new Scanner(inputStreamKeys).useDelimiter("\\A").next();
            JSONObject objectKeys = (JSONObject) new JSONTokener(jsonStringKeys).nextValue();
            String TARGET_URL = objectKeys.optString("token_target_url");

            GetAccessTokenParams accessTokenParams = new GetAccessTokenParams(context, "refresh_token", TARGET_URL, context.getFilesDir());

            String accessCodeResponse = new GetAccessToken().execute(accessTokenParams).get();
            System.out.println("Executed AsyncTask to get refresh token");

            try {
                File codesFile = new File(context.getFilesDir(), "codes.json");

                InputStream inputStreamCodes = new FileInputStream(codesFile);
                String jsonStringCodes = new Scanner(inputStreamCodes).useDelimiter("\\A").next();
                JSONObject objectCodes = (JSONObject) new JSONTokener(jsonStringCodes).nextValue();
                long REFRESH_TOKEN = Long.parseLong(objectCodes.optString("expires_in"));

                System.out.println("Next refresh between " + Long.toString(REFRESH_TOKEN - 100) + " and " + Long.toString(REFRESH_TOKEN) + " seconds");

                Constraints constraints = new Constraints.Builder().setTriggerContentMaxDelay(REFRESH_TOKEN, TimeUnit.SECONDS).build();
                OneTimeWorkRequest refreshTokens = new OneTimeWorkRequest.Builder(BackgroundRefreshTokens.class)
                        .setInitialDelay(REFRESH_TOKEN - 100, TimeUnit.SECONDS)
                        .setConstraints(constraints)
                        .addTag("GetRefreshToken")
                        .build();
                WorkManager.getInstance().enqueue(refreshTokens);

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }

            return Result.success();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Result.retry();

        // (Returning Result.retry() tells WorkManager to try this task again
        // later; Result.failure() says not to try again.)
    }

}
