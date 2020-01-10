package com.example.androidterminal.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidterminal.network.TerminalPublisher;
import com.example.androidterminal.network.TerminalSubscriber;
import com.google.android.gms.maps.GoogleMap;

import org.eclipse.paho.client.mqttv3.MqttException;

public class AsyncConnect extends AsyncTask<Void, Void, Pair<TerminalPublisher, TerminalSubscriber>> {
    private Activity activity;
    private final ProgressBar progressBar;
    private final String ipAddr;
    private final int port;
    private final String clientId;
    private final String terminal2esTopic;
    private final String es2terminalTopic;
    private Handler handler;
    private Toast connectingToast;

    private GoogleMap mMapPredicted;
    private TextView predictedMapTimerText;

    public AsyncConnect(Activity activity, ProgressBar progressBar, Handler handler, String clientId,
                        String ipAddr, int port, String terminal2esTopic, String es2terminalTopic,
                        GoogleMap mMapPredicted, TextView predictedMapTimerText) {
        this.activity = activity;
        this.progressBar = progressBar;
        this.handler = handler;
        this.clientId = clientId;
        this.ipAddr = ipAddr;
        this.port = port;
        this.terminal2esTopic = terminal2esTopic;
        this.es2terminalTopic = es2terminalTopic;
        this.mMapPredicted = mMapPredicted;
        this.predictedMapTimerText = predictedMapTimerText;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
        connectingToast = Toast.makeText(activity, "Connecting to Edge Server ...", Toast.LENGTH_LONG);
        connectingToast.show();
    }

    @Override
    protected Pair<TerminalPublisher, TerminalSubscriber> doInBackground(Void... voids) {
        try {
            TerminalPublisher esp = new TerminalPublisher(clientId, ipAddr, port, terminal2esTopic);
            TerminalSubscriber ess = new TerminalSubscriber(clientId, ipAddr, port, es2terminalTopic, activity, mMapPredicted, predictedMapTimerText);
            return new Pair<>(esp, ess);
        } catch (MqttException e) {
            e.printStackTrace();
            return new Pair<>(null, null);
        }
    }

    @Override
    protected void onPostExecute(Pair<TerminalPublisher, TerminalSubscriber> pair) {
        super.onPostExecute(pair);
        progressBar.setVisibility(View.INVISIBLE);
        connectingToast.cancel();
        handler.sendEmptyMessage(0);
    }
}
