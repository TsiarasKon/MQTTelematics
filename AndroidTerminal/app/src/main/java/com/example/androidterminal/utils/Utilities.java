package com.example.androidterminal.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilities {
    private static final boolean fullDateFormat = false;

    public static String getCurrentTime() {
        String datePattern = fullDateFormat ? "yyyy-MM-dd HH:mm:ss z" : "HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isOnline = (networkInfo != null && networkInfo.isConnected());
//        if (! isOnline)
//            Toast.makeText(context, "No internet Connection", Toast.LENGTH_LONG).show();
        return isOnline;
    }

    public static void toggleButtonActive(Button b) {
        if (b.isClickable()) {
            b.setAlpha(.5f);
            b.setClickable(false);
        } else {
            b.setAlpha(1);
            b.setClickable(true);
        }
    }

    public static void resetItemArrayList(ItemArrayAdapter adapter, String[] dataHeaders) {
        adapter.clear();
        for (String header : dataHeaders) {
            String[] row = {header, "-"};
            adapter.add(row);
        }
    }
}
