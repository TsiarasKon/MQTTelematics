package com.example.androidterminal;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.eclipse.paho.client.mqttv3.MqttException;

public class AsyncConnect extends AsyncTask<Void, Void, TerminalPublisher> {
    Activity activity;
    private final ProgressBar progressBar;
    private final String ipAddr;
    private final int port;
    private final String clientId;
    private final String topic;

    AsyncConnect(Activity activity, ProgressBar progressBar, String clientId, String ipAddr, int port, String topic) {
        this.activity = activity;
        this.progressBar = progressBar;
        this.clientId = clientId;
        this.ipAddr = ipAddr;
        this.port = port;
        this.topic = topic;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        progressBar.getHandler().post(new Runnable() {
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected TerminalPublisher doInBackground(Void... voids) {
        try {
            return new TerminalPublisher(clientId, ipAddr, port, topic);
        } catch (MqttException e) {
            e.printStackTrace();
            return null;
        }
    }

}
