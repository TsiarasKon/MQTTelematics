package com.example.androidterminal.network;

import com.example.androidterminal.utils.Utilities;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class TerminalSubscriber implements MqttCallback {
    private final String ipAddr;
    private final int port;
    private final String clientId;
    private final String topic;
    private final int qos = 2;
    private MqttClient client;

    public TerminalSubscriber(String clientId, String ipAddr, int port, String topic) throws MqttException {
        this.clientId = clientId;
        this.ipAddr = ipAddr;
        this.port = port;
        this.topic = topic;
        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        this.client = new MqttClient(getBroker(), clientId, new MemoryPersistence());
        this.client.setCallback(this);
        this.client.connect(conOpt);
        this.client.subscribe(this.topic, qos);
    }

    public String getClientId() {
        return clientId;
    }

    private String getBroker() {
        return "tcp://" + ipAddr + ':' + port;
    }

    public void sendMessage(String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        this.client.publish(this.topic, message);
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
    public void messageArrived(String topic, MqttMessage message) {
        System.out.println(String.format("%s [%s]: %s", Utilities.getCurrentTime(), topic, new String(message.getPayload())));
    }
}