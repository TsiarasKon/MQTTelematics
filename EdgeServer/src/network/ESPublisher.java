package network;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ESPublisher {

    private final String clientId;
    private final int qos = 2;
    private String topic;
    private MqttClient client;

    public ESPublisher(String clientId, String topic) throws MqttException {
        this.clientId = clientId;
        this.topic = topic;
        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        this.client = new MqttClient(EdgeServer.getBroker(), clientId, new MemoryPersistence());
        this.client.connect(conOpt);
        this.client.subscribe(this.topic, qos);
    }

    public String getClientId() {
        return clientId;
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
