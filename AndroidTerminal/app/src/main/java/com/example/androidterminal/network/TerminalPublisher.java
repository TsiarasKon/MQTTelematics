package com.example.androidterminal.network;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class TerminalPublisher {
    private final String ipAddr;
    private final int port;
    private final String clientId;
    private final String topic;
    private final int connectionTimeout = 4;
    private final int qos = 2;
    private MqttClient client;

    public TerminalPublisher(String clientId, String ipAddr, int port, String topic) throws MqttException {
        this.clientId = clientId;
        this.ipAddr = ipAddr;
        this.port = port;
        this.topic = topic;
        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        conOpt.setConnectionTimeout(connectionTimeout);
        this.client = new MqttClient(getBroker(), clientId, new MemoryPersistence());
        this.client.connect(conOpt);
        this.client.subscribe(this.topic, qos);
    }

    public String getClientId() {
        return clientId;
    }

    private String getBroker() {
        return "tcp://" + ipAddr + ':' + port;
    }

    public void publishMessage(String messageStr) {
        MqttMessage message = new MqttMessage(messageStr.getBytes());
        message.setQos(qos);
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            System.err.println(clientId + " failed to send MQTT message to topic " + topic);
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            System.err.println(clientId + " failed to disconnect");
            e.printStackTrace();
        }
    }
}
