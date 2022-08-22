package com.example.glucu.Authentication.GetTokens;

import android.content.Context;

import java.io.File;

public class GetAccessTokenParams {
    Context context;
    String GRANT_TYPE;
    String TARGET_URL;
    File filesDir;

    public GetAccessTokenParams(Context context, String GRANT_TYPE, String TARGET_URL, File filesDir) {
        this.context = context;
        this.GRANT_TYPE = GRANT_TYPE;
        this.TARGET_URL = TARGET_URL;
        this. filesDir = filesDir;
    }
}
