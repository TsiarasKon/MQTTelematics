package com.example.androidterminal.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.androidterminal.network.TerminalPublisher;

import org.eclipse.paho.client.mqttv3.MqttException;

public class AsyncConnect extends AsyncTask<Void, Void, TerminalPublisher> {
    private Activity activity;
    private final ProgressBar progressBar;
    private final String ipAddr;
    private final int port;
    private final String clientId;
    private final String topic;
    private Handler handler;
    private Toast connectingToast;

    public AsyncConnect(Activity activity, ProgressBar progressBar, Handler handler, String clientId, String ipAddr, int port, String topic) {
        this.activity = activity;
        this.progressBar = progressBar;
        this.clientId = clientId;
        this.ipAddr = ipAddr;
        this.port = port;
        this.topic = topic;
        this.handler = handler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
        connectingToast = Toast.makeText(activity, "Connecting to Edge Server ...", Toast.LENGTH_LONG);
        connectingToast.show();
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

    @Override
    protected void onPostExecute(TerminalPublisher terminalPublisher) {
        super.onPostExecute(terminalPublisher);
        progressBar.setVisibility(View.INVISIBLE);
        connectingToast.cancel();
        handler.sendEmptyMessage(0);
    }
}
