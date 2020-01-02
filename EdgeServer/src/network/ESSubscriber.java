package network;

import database.DBBridge;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import predictions.Predictor;

public class ESSubscriber implements MqttCallback {

    private final String clientId;
    private final int terminalId;
    private final int qos = 2;
    private String subTopic;
    private String pubTopic;
    private MqttClient client;

    private ESPublisher publisher;
    private Predictor predictor;
    private DBBridge db;
    private boolean firstArrived;

    public ESSubscriber(String clientId, int terminalId, String subTopic, String pubTopic, Predictor predictor) throws MqttException {
        this.clientId = clientId + "_sub";
        this.terminalId = terminalId;
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
        this.db = new DBBridge();
        this.firstArrived = false;
    }

    public void sendMessage(String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        this.client.publish(this.subTopic, message);
    }

    public void disconnect() {
        try {
            client.disconnect();
            db.close();
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
        if (firstArrived) {
            db.updateWithReal(Double.parseDouble(msgVals[0]), Integer.parseInt(msgVals[1]), Double.parseDouble(msgVals[2]),
                    Double.parseDouble(msgVals[3]), Double.parseDouble(msgVals[6]), Double.parseDouble(msgVals[7]));
        } else {
            if (! db.datapointExists(Integer.parseInt(msgVals[1]), Double.parseDouble(msgVals[0])) ) {
                db.insertReal(Double.parseDouble(msgVals[0]), Integer.parseInt(msgVals[1]), Double.parseDouble(msgVals[2]),
                        Double.parseDouble(msgVals[3]), Double.parseDouble(msgVals[6]), Double.parseDouble(msgVals[7]));
                firstArrived = true;
            }
        }
        String predictionStr = (Double.parseDouble(msgVals[0]) + 1) + "," + predictor.makePredictionFor(Double.parseDouble(msgVals[2]),
                Double.parseDouble(msgVals[3]), Double.parseDouble(msgVals[4]), Double.parseDouble(msgVals[5]));
        try {       // publish prediction to terminal
            publisher.publishMessage(predictionStr);
        } catch (MqttException e) {
            System.err.println(clientId + " failed to publish prediction");
            e.printStackTrace();
        }
        System.out.println(String.format("%s [%s] - Sent: %s", EdgeServer.getCurrentTime(), pubTopic, predictionStr));
        String[] predictionVals = predictionStr.split(",");
        if (! db.datapointExists(terminalId, Double.parseDouble(predictionVals[0])) ) {
            db.insertPredicted(Double.parseDouble(predictionVals[0]), terminalId, Double.parseDouble(predictionVals[1]),
                    Double.parseDouble(predictionVals[2]), Double.parseDouble(predictionVals[3]), Double.parseDouble(predictionVals[4]));
        }
    }
}