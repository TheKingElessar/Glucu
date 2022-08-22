package com.example.glucu.Authentication.UserAuthorization;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;

import com.example.glucu.Authentication.GetTokens.GetAccessToken;
import com.example.glucu.Authentication.GetTokens.GetAccessTokenParams;
import com.example.glucu.GetData.EGVS.InputStartDate;
import com.example.glucu.MainActivity;
import com.example.glucu.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class StoreAuthorizationCode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri data = intent.getData();
        String retrievedUri = data.toString();

        String partsOfRetrievedUri[] = retrievedUri.split("=");
        if(partsOfRetrievedUri.length == 2) {
            String authorizationCode = partsOfRetrievedUri[1];
            System.out.println("Authorization code: " + authorizationCode);

            try {
                JSONObject object = new JSONObject();
                object.put("authorization_code", authorizationCode);
                System.out.println("put authcode into keys.json: " + object.optString("authorization_code"));

                File codes = new File(this.getFilesDir(), "codes.json");
                while(!codes.exists()) {
                    try {
                        codes.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try (FileWriter file = new FileWriter(codes)) {
                    file.write(object.toString());
                    System.out.println("Successfully Copied JSON Object to File...");
                    System.out.println("\nJSON Object: " + object);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    InputStream inputStreamKeys = this.getResources().openRawResource(R.raw.keys);
                    String jsonStringKeys = new Scanner(inputStreamKeys).useDelimiter("\\A").next();
                    JSONObject objectKeys = (JSONObject) new JSONTokener(jsonStringKeys).nextValue();
                    String TARGET_URL = objectKeys.optString("token_target_url");

                    GetAccessTokenParams accessTokenParams = new GetAccessTokenParams(this, "authorization_code", TARGET_URL, this.getFilesDir());
                    System.out.println("Formed params");

                    String accessCodeResponse = new GetAccessToken().execute(accessTokenParams).get();
                    System.out.println("Executed AsyncTask after user login");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                System.out.println("Going to main activity");
                Intent goToMainActivity = new Intent(this, MainActivity.class);
                goToMainActivity.putExtra("openStartDateMenu", "true");
                startActivity(goToMainActivity);

                finish();

            } catch (JSONException ex) {
                System.out.println("JSONException. Looks like nothing is working:" + ex);
                finish();
            }

        } else {
            System.out.println("Oops. The retrieved authorization code wasn't what we expected. Oh well - better luck next time?");
        }

    }
}
