package com.example.glucu;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.work.WorkManager;

import com.example.glucu.Authentication.GetTokens.GetAccessToken;
import com.example.glucu.Authentication.GetTokens.GetAccessTokenParams;
import com.example.glucu.Database.NukeEGVS;
import com.example.glucu.DisplayData.DisplayEGVS;
import com.example.glucu.GetData.EGVS.GetEGVS;
import com.example.glucu.GetData.EGVS.GetEGVSParams;
import com.example.glucu.GetData.EGVS.InputStartDate;
import com.example.glucu.GetData.EGVS.StoreEGVS;
import com.example.glucu.Watch.MainActivityWear;
import com.scichart.charting.visuals.SciChartSurface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static com.example.glucu.Authentication.UserAuthorization.DoUserAuthorization.obtainUserAuthorization;
import static com.example.glucu.GetData.EGVS.GetEGVSParams.convertToDate;


public class MainActivity extends AppCompatActivity {

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("MainActivity onCreate started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

		// scichart license key
        String license = "";
// Use Static method SciChartSurface.setRuntimeLicenseKey()
        try {
            SciChartSurface.setRuntimeLicenseKey(license);
            System.out.println("Scichart license just got gud");
        } catch (Exception e) {
            e.printStackTrace();
        }


        Intent startingIntent = getIntent();
        System.out.println("Got starting intent");
        try { // If there's an extra
            String message = startingIntent.getStringExtra("openStartDateMenu");
            System.out.println("Message sent from starting activity: " + message);
            if (Boolean.parseBoolean(message)) {
                setStartDate();
                System.out.println("setStartDate started from onCreate");
            }
        } catch(NullPointerException e) {
            System.out.println("No message to input start date. That's pretty neat!");
        }

    }

    public void startGetAccessToken(View view) throws JSONException {
        try {
            InputStream inputStreamKeys = this.getResources().openRawResource(R.raw.keys);
            String jsonStringKeys = new Scanner(inputStreamKeys).useDelimiter("\\A").next();
            JSONObject objectKeys = (JSONObject) new JSONTokener(jsonStringKeys).nextValue();
            String TARGET_URL = objectKeys.optString("token_target_url");

            GetAccessTokenParams accessTokenParams = new GetAccessTokenParams(this, "authorization_code", TARGET_URL, this.getFilesDir());

            String accessCodeResponse = new GetAccessToken().execute(accessTokenParams).get();
            System.out.println("Executed AsyncTask to get access token");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    public void startGetRefreshToken(View view) throws JSONException {
        try {
            InputStream inputStreamKeys = this.getResources().openRawResource(R.raw.keys);
            String jsonStringKeys = new Scanner(inputStreamKeys).useDelimiter("\\A").next();
            JSONObject objectKeys = (JSONObject) new JSONTokener(jsonStringKeys).nextValue();
            String TARGET_URL = objectKeys.optString("token_target_url");

            GetAccessTokenParams accessTokenParams = new GetAccessTokenParams(this, "refresh_token", TARGET_URL, this.getFilesDir());

            String accessCodeResponse = new GetAccessToken().execute(accessTokenParams).get();
            System.out.println("Executed AsyncTask to get refresh token");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void startAuthorizeUser(View view) {
        String targetUrl = obtainUserAuthorization(this);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
        startActivity(browserIntent);
    }

    public void stopRequests(View view) {
        System.out.println("stopRequests called");
        WorkManager.getInstance().cancelAllWorkByTag("GetRefreshToken");
    }

    public void startGetEGVS(View view) {
        try {
            String startDate = convertToDate("2019", "03", "29", "08", "00", "00");
            String endDate = convertToDate("2019", "04", "28", "17", "13", "00");

            System.out.println("startDate: " + startDate);
            System.out.println("endDate: " + endDate);

            GetEGVSParams params = new GetEGVSParams(this, startDate, endDate);

            String getEGVSResponse = new GetEGVS().execute(params).get();
            System.out.println(getEGVSResponse);

            StoreEGVS storeEGVS = new StoreEGVS(getEGVSResponse, this);
            storeEGVS.start();

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void startDisplayEGVS(View view) {
        Intent intent = new Intent(this, DisplayEGVS.class);
        startActivity(intent);
    }

    public void deleteFile(View view) {
        File valuesFile = new File(this.getFilesDir(), "values.json");
        valuesFile.delete();
        System.out.println("Deleted file.");
    }

    public void nukeTable(View view) {
        NukeEGVS nukeEGVS = new NukeEGVS(this);
        nukeEGVS.start();

    }

    public void setStartDate() {
        System.out.println("setStartDate in MainActivity started");
        DialogFragment newFragment = new InputStartDate();
        newFragment.show(getSupportFragmentManager(), "datePicker");
        System.out.println("Start time popup is live!");

    }

    public void dbExists(View view) {
        File dbFile = this.getDatabasePath("EGVS_Database");
        System.out.println("DB exists? " + dbFile.exists());
        boolean dbExists = dbFile.exists();
        System.out.println(dbExists + " should be the same as previous line");

    }

    public void startWatchMessagingActivity(View view) {
        Intent intent = new Intent(this, MainActivityWear.class);
        startActivity(intent);
    }


}