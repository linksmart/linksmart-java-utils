package eu.linksmart.services.utils.mqtt.subscription;


import eu.linksmart.services.utils.configuration.Configurator;
import eu.linksmart.services.utils.constants.Const;
import eu.linksmart.services.utils.mqtt.broker.BrokerService;
import eu.linksmart.services.utils.mqtt.types.MqttMessage;
import eu.linksmart.services.utils.mqtt.types.Topic;
import eu.linksmart.services.utils.serialization.DefaultDeserializer;
import eu.linksmart.services.utils.serialization.Deserializer;
import eu.linksmart.testing.tooling.MessageValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;


import java.io.IOException;
import java.util.*;

/**
 * Created by José Ángel Carvajal on 06.08.2015 a researcher of Fraunhofer FIT.
 */
public  class ForwardingListener implements MqttCallback {
    private final UUID originProtocol;
    private Observer  connectionListener = null;
    private static Logger LOG = LogManager.getLogger(BrokerService.class);

    private long sequence ;
    private Map<Topic, TopicMessageDeliverable> observables;
    private Map<String,TopicMessageDeliverable> compiledTopic = new Hashtable<>();

    private final  Object muxMessageDelivererSet = new Object();
    private Set<Topic> messageDelivererSet = new HashSet<>();
    // set of topics where we publish
    private static Set<String> publishedTopics = new HashSet<>();
    // if true ignores messages sent by the same broker
    private boolean autoblacklisting = true;

    //Start of code made for testing performance
    private final boolean VALIDATION_MODE;
    private final Deserializer deserializer;
    private final MessageValidator validator;
    //End of code made for testing performance

    public ForwardingListener( Observer connectionListener, UUID originProtocol) {
        this.originProtocol = originProtocol;
        this.connectionListener = connectionListener;

        /// Code for validation and test proposes
        if(VALIDATION_MODE = Configurator.getDefaultConfig().containsKeyAnywhere(Const.VALIDATION_FORWARDING)) {
            deserializer = new DefaultDeserializer();
            validator = new MessageValidator(this.getClass(),"0",Configurator.getDefaultConfig().getLong(Const.VALIDATION_LOT_SIZE));
        }else{
            deserializer = null;
            validator = null;
        }

        observables = new Hashtable<>();
    }


    protected void initObserver(String listening, Observer mqttEventsListener){
        observables = new Hashtable<>();
        observables.put(new Topic(listening), new TopicMessageDeliverable(listening));
    }
    public void addObserver(String topic, Observer listener){
        Topic t = new Topic(topic);
        if(!observables.containsKey(t))
            observables.put(t, new TopicMessageDeliverable(topic));

        observables.get(t).addObserver(listener);
        synchronized (muxMessageDelivererSet) {
            messageDelivererSet = observables.keySet();
        }

    }

    public static void addPublishedTopic(String topic){
       // if(messageDelivererSet.stream().anyMatch(t -> t.equals(topic)))
            publishedTopics.add(topic);
    }
    public void removePublishedTopic(String topic){
        publishedTopics.remove(topic);
    }

    public boolean isAutoblacklisting() {
        return autoblacklisting;
    }

    public void setAutoblacklisting(boolean autoblacklisting) {
        this.autoblacklisting = autoblacklisting;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean removeObserver(String topic, Observer listener){
        if(observables.containsKey(topic) && observables.get(topic).containsListener(listener))
            observables.get(topic).deleteObserver(listener);
        else
            return false;
        if(observables.get(topic).countObservers()==0)
            observables.remove(topic);


        synchronized (muxMessageDelivererSet) {
            messageDelivererSet = observables.keySet();
        }
        return true;
    }
    public Set<Topic> getListeningTopics(){
        return observables.keySet();
    }
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean isObserversEmpty(String topic){
        return observables.containsKey(topic);
    }



    @Override
    public void connectionLost(Throwable throwable) {
        LOG.warn("Connection lost: "+throwable.getMessage(),throwable);
        connectionListener.update(null, this);

    }


    private synchronized long getMessageIdentifier(){
        sequence = (sequence + 1) % Long.MAX_VALUE;
        return sequence;
    }

    @Override
    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage) {
        // LOG.debug("Message arrived in listener:" + topic);
        if(autoblacklisting && publishedTopics.contains(topic)) {// if this topic had been used to published
            // publishedTopics.remove(topic);
            return;
        }

        if(VALIDATION_MODE) toValidation(topic,mqttMessage.getPayload());

        boolean processed= false;
        if(!compiledTopic.containsKey(topic)){
            for(Topic t: messageDelivererSet)
                if(t.equals(topic)) {

                    compiledTopic.put(topic, observables.get(t));
                    compiledTopic.get(topic).addMessage(new MqttMessage(topic, mqttMessage.getPayload(), mqttMessage.getQos(), mqttMessage.isRetained(), getMessageIdentifier(), originProtocol));

                    processed = true;
                    break;
                }
        } else if(compiledTopic.containsKey(topic)) {
            // observables.get(t).notifyObservers(new MqttMessage(topic, mqttMessage.getPayload(), mqttMessage.getQos(), mqttMessage.isRetained(), getMessageIdentifier(), originProtocol));
            compiledTopic.get(topic).addMessage(new MqttMessage(topic, mqttMessage.getPayload(), mqttMessage.getQos(), mqttMessage.isRetained(), getMessageIdentifier(), originProtocol));

            processed = true;
        }

        if(!processed)
            LOG.warn("A message arrived and no one listening to it");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        LOG.debug("delivery complete in listener");

    }


    /// for validation and evaluation propose
    private void toValidation(String topic, byte[] payload){
        if (VALIDATION_MODE)
            try {
                validator.addMessage(topic,(int)deserializer.deserialize(payload, Hashtable.class).get("ResultValue"));
            } catch (IOException e) {
                e.printStackTrace();
            }

    }


    public boolean hasObservers() {
        return !observables.isEmpty();
    }
}
