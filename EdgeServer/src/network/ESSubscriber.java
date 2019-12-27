package network;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import sumo_data.Predictor;

public class ESSubscriber implements MqttCallback {

    private final String clientId;
    private final int qos = 2;
    private String subTopic;
    private String pubTopic;
    private MqttClient client;

    private ESPublisher publisher;

    private Predictor predictor;

    public ESSubscriber(String clientId, String subTopic, String pubTopic, Predictor predictor) throws MqttException {
        this.clientId = clientId + "_sub";
        this.subTopic = subTopic;
        this.pubTopic = pubTopic;
        this.publisher = new ESPublisher(clientId + "_pub", pubTopic);
        this.predictor = predictor;
        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        this.client = new MqttClient(EdgeServer.getBroker(), clientId, new MemoryPersistence());
        this.client.setCallback(this);
        this.client.connect(conOpt);
        this.client.subscribe(this.subTopic, qos);
    }

    public void sendMessage(String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        this.client.publish(this.subTopic, message);
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            System.err.println(clientId + " failed to disconnect");
            e.printStackTrace();
        }
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
    public void messageArrived(String subTopic, MqttMessage message) {
        String msgResult = new String(message.getPayload());
        if (msgResult.charAt(0) == '$') {
            System.out.println(String.format("%s [%s]: %s", EdgeServer.getCurrentTime(), subTopic, msgResult.substring(1)));
            return;
        }
        System.out.println(String.format("%s [%s] - Received: %s", EdgeServer.getCurrentTime(), subTopic, msgResult));
        String[] msgVals = msgResult.split(",");
        // TODO: insert real in db
        String predictionStr = (Double.parseDouble(msgVals[0]) + 1) + "," + predictor.makePredictionFor(Double.parseDouble(msgVals[2]),
                Double.parseDouble(msgVals[3]), Double.parseDouble(msgVals[4]), Double.parseDouble(msgVals[5]));
        try {       // publish prediction to terminal
            publisher.publishMessage(predictionStr);
        } catch (MqttException e) {
            System.err.println(clientId + " failed to publish prediction");
            e.printStackTrace();
        }
        System.out.println(String.format("%s [%s] - Sent: %s", EdgeServer.getCurrentTime(), pubTopic, predictionStr));
        // TODO: insert predicted in db
    }
}