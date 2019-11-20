package network;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ESSubscriber implements MqttCallback {

    private final String clientId;
    private final int qos = 2;
    private String topic;
    private MqttClient client;

    public ESSubscriber(String clientId, String topic) throws MqttException {
        this.clientId = clientId;
        this.topic = topic;
        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        this.client = new MqttClient(EdgeServer.getBroker(), clientId, new MemoryPersistence());
        this.client.setCallback(this);
        this.client.connect(conOpt);
        this.client.subscribe(this.topic, qos);
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
        System.out.println(String.format("%s [%s]: %s", EdgeServer.getCurrentTime(), topic, new String(message.getPayload())));
    }
}