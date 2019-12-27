package com.example.androidterminal.network;

import android.app.Activity;
import android.util.Log;

import com.example.androidterminal.R;
import com.example.androidterminal.utils.Utilities;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TerminalSubscriber implements MqttCallback {
    private final String ipAddr;
    private final int port;
    private final String clientId;
    private final String topic;
    private final int qos = 2;
    private MqttClient client;

    private Activity activity;
    private GoogleMap mMapPredicted;
    private Marker currMarkPredicted;
    private List<Marker> markersPredicted = new ArrayList<>();

    public TerminalSubscriber(String clientId, String ipAddr, int port, String topic, Activity activity, GoogleMap mMapPredicted) throws MqttException {
        this.clientId = clientId + "_sub";
        this.ipAddr = ipAddr;
        this.port = port;
        this.topic = topic;
        this.activity = activity;
        this.mMapPredicted = mMapPredicted;
        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        this.client = new MqttClient(getBroker(), this.clientId, new MemoryPersistence());
        this.client.setCallback(this);
        this.client.connect(conOpt);
        this.client.subscribe(this.topic, qos);
    }

    private String getBroker() {
        return "tcp://" + ipAddr + ':' + port;
    }

    public void sendMessage(String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        this.client.publish(this.topic, message);
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            System.err.println(clientId + " failed to disconnect");
            e.printStackTrace();
        }
    }

    public void clearMapMarkers() {
        for (Marker m : markersPredicted) {
            m.remove();
        }
        markersPredicted.clear();
        currMarkPredicted = null;
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost because: " + cause);
        System.exit(1);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Override
    public void messageArrived(String topic, final MqttMessage message) {
//        Log.i(clientId, String.format("%s [%s]: %s", Utilities.getCurrentTime(), topic, new String(message.getPayload())));

        activity.runOnUiThread(new Runnable(){
            public void run(){
                String[] msgVals = (new String(message.getPayload())).split(",");
                if (currMarkPredicted != null) currMarkPredicted.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.dot_orange));
                currMarkPredicted = mMapPredicted.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(msgVals[1]), Double.parseDouble(msgVals[2])))
                        .anchor(0.5f, 1)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .title("(" + msgVals[1] + ", " + msgVals[2] + ")")
                        .snippet("RSSI: " + String.format(Locale.US, "%.2f", Double.parseDouble(msgVals[3])) +
                                "  |  Throughput: " + String.format(Locale.US, "%.2f", Double.parseDouble(msgVals[4])))
                );
                currMarkPredicted.showInfoWindow();
                markersPredicted.add(currMarkPredicted);
//                mMapPredicted.animateCamera(CameraUpdateFactory.zoomBy(0.0000001f));

            }
        });
    }
}