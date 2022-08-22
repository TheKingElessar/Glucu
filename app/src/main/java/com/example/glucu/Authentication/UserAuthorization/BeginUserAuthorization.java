package com.example.glucu.Authentication.UserAuthorization;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.view.View;

import com.example.glucu.GetData.EGVS.InputStartDate;
import com.example.glucu.R;

import java.io.IOException;

import static com.example.glucu.Authentication.UserAuthorization.DoUserAuthorization.obtainUserAuthorization;

public class BeginUserAuthorization extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize_user);

    }

    public void startAuthorization(View view) {
        String targetUrl = obtainUserAuthorization(this);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
        startActivity(browserIntent);
    }

}