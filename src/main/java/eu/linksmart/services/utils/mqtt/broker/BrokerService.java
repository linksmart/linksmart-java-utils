package eu.linksmart.services.utils.mqtt.broker;

import eu.linksmart.services.utils.configuration.Configurator;
import eu.linksmart.services.utils.mqtt.subscription.ForwardingListener;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.*;
import java.util.stream.Collectors;

public class BrokerService implements Observer, Broker {
    protected transient static Logger loggerService = LogManager.getLogger(BrokerService.class);
    // this is the MQTT client to broker in the local broker
    private transient Configurator conf = Configurator.getDefaultConfig();
    protected final transient MqttClient mqttClient;
    transient ForwardingListener listener;
    private transient List<Observer> connectionListener = new ArrayList<>();

    protected ArrayList<String> topics = new ArrayList<>();
    private ArrayList<Integer> qoss = new ArrayList<>();

    private final transient static Object lock = new Object();

    final BrokerConfiguration brokerConf;

    public BrokerService(String alias, UUID ID, String will, String topicWill) throws MqttException {

        brokerConf = new BrokerConfiguration(alias, ID.toString());
        brokerConf.setWill(will);
        brokerConf.setWillTopic(topicWill);
        listener = new ForwardingListener(this, ID);
        listener.setAutoblacklisting(brokerConf.isAutoBlacklisting());
        mqttClient = brokerConf.initClient();
        createClient();
    }

    public boolean isConnected() {

        return mqttClient.isConnected();
    }

    protected synchronized void _connect() throws Exception {

        if (!mqttClient.isConnected()) {
            loggerService.info("MQTT broker UUID:" + brokerConf.getId() + " Alias:" + brokerConf.getAlias() + " with configuration " + brokerConf.toString() + " is connecting...");
            mqttClient.connect(brokerConf.getMqttConnectOptions());

            loggerService.info("MQTT broker UUID:" + brokerConf.getId() + " Alias:" + brokerConf.getAlias() + " is connected");
        }
    }

    protected synchronized void _disconnect() throws Exception {
        loggerService.info("MQTT broker UUID:" + brokerConf.getId() + " Alias:" + brokerConf.getAlias() + " with configuration " + brokerConf.toString() + " is disconnecting...");
        try {

            mqttClient.disconnect();
        } catch (Exception e) {
            loggerService.error(e.getMessage(), e);
            throw e;
        }
        loggerService.info("MQTT broker UUID:" + brokerConf.getId() + " Alias:" + brokerConf.getAlias() + " is disconnected");
    }

    protected void _destroy() throws Exception {

        try {

            if (mqttClient.isConnected())
                _disconnect();

            mqttClient.close();
        } catch (Exception e) {
            loggerService.error(e.getMessage(), e);
            throw e;
        }
    }

    public void connect() throws Exception {
        _connect();
    }

    public void disconnect() throws Exception {

        _disconnect();
    }

    public void destroy() throws Exception {
        _destroy();
    }

    public String getBrokerURL() {
        return brokerConf.getURL();
    }

    public void createClient() throws MqttException {

        mqttClient.setCallback(listener);
        try {
            _connect();
        } catch (MqttException e) {
            throw e;
        } catch (Exception e) {
            loggerService.error(e.getMessage(), e);
        }
    }

    public synchronized void publish(String topic, byte[] payload, int qos, boolean retained) throws Exception {

        if (!mqttClient.isConnected())
            _connect();

        ForwardingListener.addPublishedTopic(topic);

        mqttClient.publish(topic, payload, qos, retained);
    }

    public void publish(String topic, byte[] payload) throws Exception {

        publish(
                topic,
                payload,
                brokerConf.getPubQoS(),
                brokerConf.isRetainPolicy());
    }

    public void publish(String topic, String payload) throws Exception {

        publish(topic, (payload != null) ? payload.getBytes() : null);
    }

    public String getBrokerName() {
        return brokerConf.getHostname();
    }

    public void setBrokerName(String brokerName) throws Exception {
        boolean wasConnected = isConnected();
        if (!this.brokerConf.getHostname().equals(brokerName)) {

            this.brokerConf.setHostname(brokerName);

            restart(wasConnected);
        }
    }

    public String getBrokerPort() {
        return String.valueOf(brokerConf.getPort());
    }

    @Override
    public boolean hasListeners() {
        return listener.hasObservers();
    }

    public void setBrokerPort(String brokerPort) throws Exception {
        boolean wasConnected = isConnected();

        if (!getBrokerPort().equals(brokerPort)) {
            brokerConf.setPort(Integer.valueOf(brokerPort));

            restart(wasConnected);
        }
    }

    public void setBroker(String brokerName, String brokerPort) throws Exception {
        boolean wasConnected = isConnected();
        if (!this.getBrokerName().equals(brokerName) || !this.getBrokerPort().equals(brokerPort)) {

            if (!this.getBrokerPort().equals(brokerPort)) {
                brokerConf.setPort(Integer.valueOf(brokerPort));
            }
            if (!this.getBrokerName().equals(brokerName)) {
                this.brokerConf.setHostname(brokerName);
            }

            restart(wasConnected);
        }
    }

    private void restart(boolean wasConnected) throws Exception {
        try {
            destroy();
        } catch (Exception e) {
            loggerService.error("Error while restarting broker Service:" + e.getMessage(), e);
        }

        createClient();
        if (wasConnected) {
            connect();
        }
    }

    public boolean addListener(String topic, Observer stakeholder) {
        return addListener(topic, stakeholder, brokerConf.getSubQoS());
    }

    public synchronized boolean addListener(String topic, Observer stakeholder, int QoS) {

        try {
            _connect();

            topics.add(topic);
            qoss.add(QoS);

            subscribeAll();

            listener.addObserver(topic, stakeholder);
        } catch (Exception e) {
            loggerService.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public void addConnectionListener(Observer listener) {
        connectionListener.add(listener);
    }

    public synchronized boolean removeListener(String topic, Observer stakeholder) {

        return listener.removeObserver(topic, stakeholder);
    }

    public synchronized void removeListener(Observer stakeholder) {
        for (String topic : topics) {
            listener.removeObserver(topic, stakeholder);
        }
    }

    @Override
    public BrokerConfiguration getConfiguration() {
        return brokerConf;
    }

    private synchronized void subscribeAll() throws MqttException {
        loggerService.info("(re)subscribing to: " + topics.stream().collect(Collectors.joining(",")));
        if (topics.size() > 0) {
            mqttClient.subscribe(topics.toArray(new String[topics.size()]), ArrayUtils.toPrimitive(qoss.toArray(new Integer[qoss.size()])));
        }
    }

    @Override
    public synchronized void update(Observable o, Object arg) {
        boolean wait = false;
        do {
            try {
                Thread.sleep(brokerConf.getReconnectWaitingTime());
                wait = true;
            } catch (InterruptedException ex) {
                //nothing
            }
        } while (!wait);
        loggerService.warn("Disconnection of the client with id: " + brokerConf.getId() + " and alias: " + brokerConf.getAlias() + " with conf: " + brokerConf.toString());
        reconnectingLoop(o, arg);
        resubscribingLoop(o, arg);
        informingLoop(o, arg);

        if (!isConnected())
            System.exit(-1);
    }

    private void reconnectingLoop(Observable o, Object arg) {
        boolean fail = true;
        for (int i = 0; i < brokerConf.getNoTries() && !mqttClient.isConnected(); i++) {
            try {
                if (!mqttClient.isConnected()) {
                    _connect();
                }
                fail = false;
            } catch (Exception e) {
                try {
                    Thread.sleep(brokerConf.getReconnectWaitingTime() * i);
                } catch (InterruptedException ex) {
                    loggerService.error(ex.getMessage(), ex);
                }
                loggerService.error(e.getMessage(), e);
            }
        }
        if (fail && !mqttClient.isConnected()) {
            loggerService.error("System unable to reconnect");
            System.exit(-1);
        }
    }

    private void resubscribingLoop(Observable o, Object arg) {
        boolean fail = !mqttClient.isConnected();
        for (int i = 0; !mqttClient.isConnected() && i < brokerConf.getNoTries(); i++) {
            try {
                loggerService.info("Resubscribing...");
                subscribeAll();
                fail = false;
            } catch (Exception e) {
                try {
                    Thread.sleep(brokerConf.getReconnectWaitingTime() * i);
                } catch (InterruptedException ex) {
                    loggerService.error(ex.getMessage(), ex);
                }
                loggerService.error(e.getMessage(), e);
            }
            if (!mqttClient.isConnected())
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    loggerService.error(ex.getMessage(), ex);
                }
        }
        if (fail) {
            loggerService.error("System unable to resubscribe");
            System.exit(-1);
        }
    }

    private void informingLoop(Observable o, Object arg) {
        boolean fail = true;
        for (int i = 0; i < brokerConf.getNoTries() && mqttClient.isConnected(); i++) {
            try {
                subscribeAll();
                loggerService.info("Informing...");
                if (connectionListener.size() > 1)
                    connectionListener.stream().parallel().forEach(l -> l.update(null, arg));
                else
                    connectionListener.forEach(l -> l.update(null, arg));
                fail = false;
            } catch (Exception e) {
                try {
                    Thread.sleep(brokerConf.getReconnectWaitingTime() * i);
                } catch (InterruptedException ex) {
                    loggerService.error(ex.getMessage(), ex);
                }
                loggerService.error(e.getMessage(), e);
            }
        }
        if (fail) {
            loggerService.error("System unable to resubscribe");
            System.exit(-1);
        }
    }

    @Override
    public String getAlias() {
        return brokerConf.getAlias();
    }

    @Override
    public boolean equals(Object o) {

        if (o == this)
            return true;
        if (o != null && o instanceof BrokerService) {
            BrokerService aux = (BrokerService) o;

            return brokerConf.equals(aux.brokerConf) && topics.equals(aux.topics);
        }
        return false;
    }

    @Override
    public String toString() {

        return "{" +
                "\"brokerConfiguration\":" + brokerConf.toString() + "," +
                "\"subscribedTopics\":[" + topics.stream().map(i -> "\"" + i + "\"").collect(Collectors.joining(",")) + "]" +
                "}";
    }

    @Override
    public int hashCode() {

        return toString().hashCode();
    }
}