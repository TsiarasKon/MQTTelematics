package com.example.androidterminal;

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
}
