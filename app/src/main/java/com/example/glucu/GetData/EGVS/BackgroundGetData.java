package com.example.glucu.GetData.EGVS;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.glucu.Database.EGVS;
import com.example.glucu.Database.InsertEGVS;
import com.example.glucu.Database.RetrieveEGVS;
import com.example.glucu.MainActivity;

import java.io.File;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.example.glucu.GetData.EGVS.GetEGVSParams.convertToDate;


public class BackgroundGetData extends Worker {

    Context context;
    static String startYear;
    static String startMonth;
    static String startDay;

    public BackgroundGetData(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        context = getApplicationContext();

        System.out.println("Started BackgroundGetData");
        File dbFile = context.getDatabasePath("EGVS_Database");
        System.out.println("DB exists? " + dbFile.exists());
        boolean dbExists = dbFile.exists();
        System.out.println(dbExists + " should be the same as previous line");

        String startDate = "";
        String endDate;
        String year;
        String month;
        String day;
        String hour;
        String minute;
        String second;
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));

        DecimalFormat mFormat= new DecimalFormat("00");

        if(dbExists) {
            try { System.out.println("STEVE");
                RetrieveEGVS retrieveEGVS = new RetrieveEGVS(context, InsertEGVS.db);
                System.out.println("HARRY");
                retrieveEGVS.start();
                System.out.println("BOB");
                retrieveEGVS.join(0);
                System.out.println("NATHAN!!!");

                List<EGVS> egvsList = retrieveEGVS.egvsList;
                //         List<EGVS> reversedList = Lists.reverse(egvsList);

                Collections.sort(egvsList);
                EGVS latestEGVS = egvsList.get(egvsList.size() - 1);

                cal.setTime(latestEGVS.systemTimeEGVS);
                month = mFormat.format(Double.valueOf(cal.get(Calendar.MONTH)));
                year = mFormat.format(Double.valueOf(cal.get(Calendar.YEAR)));
                day = mFormat.format(Double.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                hour = mFormat.format(Double.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
                minute = mFormat.format(Double.valueOf(cal.get(Calendar.MINUTE))); // TODO: Make this its own class with something like RequestDate.getDate(year, month...)
                second = mFormat.format(Double.valueOf(cal.get(Calendar.SECOND)));
                startDate = convertToDate(year, month, day, hour, minute, second);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } else {
            month = BackgroundGetData.startMonth;
            year = BackgroundGetData.startYear;
            day = BackgroundGetData.startDay;
            hour = "00";
            minute = "00";
            second = "00";
            startDate = convertToDate(year, month, day, hour, minute, second);
        }

        cal = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
        cal.setTime(new Date());
        year = mFormat.format(Double.valueOf(cal.get(Calendar.YEAR)));
        month = mFormat.format(Double.valueOf(cal.get(Calendar.MONTH)));
        day = mFormat.format(Double.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
        hour = mFormat.format(Double.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
        minute = mFormat.format(Double.valueOf(cal.get(Calendar.MINUTE)));
        second = mFormat.format(Double.valueOf(cal.get(Calendar.SECOND)));
        endDate = convertToDate(year, month, day, hour, minute, second);

        GetEGVSParams params = new GetEGVSParams(context, startDate, endDate);

        String getEGVSResponse;
        try {
            getEGVSResponse = new GetEGVS().execute(params).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Result.retry();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.retry();
        }

        StoreEGVS storeEGVS = new StoreEGVS(getEGVSResponse, context);
        storeEGVS.start();

        Constraints constraints = new Constraints.Builder().setTriggerContentMaxDelay(7, TimeUnit.MINUTES).build();
        OneTimeWorkRequest getDataContinuous = new OneTimeWorkRequest.Builder(BackgroundGetData.class)
                .setInitialDelay(3, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag("GetData")
                .build();
        WorkManager.getInstance().enqueue(getDataContinuous);
        System.out.println("Enqueued next one!");

        Context context = MainActivity.context;

        return Result.success();


        // (Returning Result.retry() tells WorkManager to try this task again
        // later; Result.failure() says not to try again.)
    }

   // class SetStartDate {
        public static void setStartDate(int year, int month, int day) throws InterruptedException {
            DecimalFormat mFormat= new DecimalFormat("00");

            BackgroundGetData.startYear = mFormat.format(year);
            BackgroundGetData.startMonth = mFormat.format(month);
            BackgroundGetData.startDay = mFormat.format(day);

            System.out.println("BackgroundGetData setStartDate started.");

            // Schedule future one
            Constraints constraints = new Constraints.Builder().setTriggerContentMaxDelay(30, TimeUnit.SECONDS).build();
            OneTimeWorkRequest getDataContinuous = new OneTimeWorkRequest.Builder(BackgroundGetData.class)
                    .setInitialDelay(0, TimeUnit.SECONDS)
                    .setConstraints(constraints)
                    .addTag("GetData")
                    .build();
            WorkManager.getInstance().enqueue(getDataContinuous);

            System.out.println("New work enqueued to get first supply of data");

        }
  //  }

}
