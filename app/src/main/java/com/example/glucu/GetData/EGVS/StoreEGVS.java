package com.example.glucu.GetData.EGVS;

import android.content.Context;
import android.content.Intent;

import com.example.glucu.Database.EGVS;
import com.example.glucu.Database.InsertEGVS;
import com.example.glucu.DisplayData.DisplayEGVS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoreEGVS extends Thread {

    private String jsonString;
    private Context context;

    public StoreEGVS(String jsonString, Context context) {
        this.jsonString = jsonString;
        this.context = context;
    }

    public void run(){
        String date;
        double value;
        Date oldFashionedDateObject;
        Instant pointInTime;
        String trend;
        List<EGVS> egvsList = new ArrayList<>();

        System.out.println("Retrieved data: " + jsonString);
        try {
            JSONObject object = new JSONObject(jsonString);
            JSONArray array = object.getJSONArray("egvs");

            for (int i = 0; i < array.length(); i++) {
                date = array.getJSONObject(i).optString("systemTime");
                date += "Z";
                pointInTime = Instant.parse(date);
                oldFashionedDateObject = Date.from(pointInTime);

                value = Double.parseDouble(array.getJSONObject(i).optString("value"));

                trend = array.getJSONObject(i).optString("trend");

                egvsList.add(new EGVS(oldFashionedDateObject, value, trend));

                if (i < array.length() && i > array.length() - 10) {
                    System.out.println("Time :: value = " + date + " :: " + value);
                }
            }

            InsertEGVS insertEGVS = new InsertEGVS(context, egvsList);
            insertEGVS.start();

            System.out.println("Values just retrieved and saved to database: " + array.length());

            Intent intent = new Intent(context, DisplayEGVS.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Retrieved data isn't in the proper JSON format: " + jsonString);
        }

    }

}
/*
    RetrieveEGVS retrieveEGVS = new RetrieveEGVS(getApplicationContext(), InsertEGVS.db);
                retrieveEGVS.start();
                        retrieveEGVS.join();
                        List<EGVS> egvsList = retrieveEGVS.egvsList;
        List<EGVS> reversedList = Lists.reverse(egvsList);
        Collections.sort(reversedList);
        Date latestEGVSDate = (reversedList.get(reversedList.size() - 1).systemTimeEGVS);
        double latestEGVSValue = (reversedList.get(reversedList.size() - 1).valueEGVS);
        String stringLatestEGVSTrend = (reversedList.get(reversedList.size() - 1).trendEGVS);

        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        String stringLatestEGVSValue = nf.format(latestEGVSValue);
        String stringLatestEGVSDate = Long.toString(latestEGVSDate.getTime());

        message = stringLatestEGVSDate + "/" + stringLatestEGVSValue + "/" + stringLatestEGVSTrend;

        */

/*    // Todo: https://code.tutsplus.com/tutorials/get-wear-os-and-android-talking-exchanging-information-via-the-wearable-data-layer--cms-30986
    String message = intent.getStringExtra("message");
    String[] splitMessage = message.split("/");
    String latestEGVSTime = splitMessage[0];
    String latestEGVSValue = splitMessage[1];
    String latestEGVSTrend = splitMessage[2];
            System.out.println("Retrieved values are now in main activity");

                    String toDisplay = "Latest EGVS Value: " + latestEGVSValue;

                    textView.setText(toDisplay);
                    System.out.println("Latest EGVS value displayed on watch");

// TODO: Do something with these values (display them)
*/