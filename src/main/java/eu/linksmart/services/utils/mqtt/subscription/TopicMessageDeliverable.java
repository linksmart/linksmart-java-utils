package eu.linksmart.services.utils.mqtt.subscription;

import eu.linksmart.services.utils.configuration.Configurator;
import eu.linksmart.services.utils.constants.Const;
import eu.linksmart.services.utils.mqtt.types.MqttMessage;
import eu.linksmart.services.utils.serialization.DefaultDeserializer;
import eu.linksmart.services.utils.serialization.Deserializer;
import eu.linksmart.testing.tooling.MessageValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Observer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by José Ángel Carvajal on 23.03.2016 a researcher of Fraunhofer FIT.
 */
public class TopicMessageDeliverable implements Runnable{
    private LinkedBlockingQueue<MqttMessage> mqttMessages = new LinkedBlockingQueue<>();
    private LinkedList<MqttMessageObserver> observers = new LinkedList<>();
    protected final String topic;

    //Start of code made for testing performance
    private final boolean VALIDATION_MODE;
    private final Deserializer deserializer;
    private final MessageValidator validator;
    private final Thread thread;
    //End of code made for testing performance

    protected transient Logger loggerService = LogManager.getLogger(TopicMessageDeliverable.class);
    public TopicMessageDeliverable(String topic) {
        this.topic=topic;
        // loggerService.debug("Starting new ");
        thread =new Thread(this);
        thread.start();


        /// Code for validation and test proposes
        if(VALIDATION_MODE = Configurator.getDefaultConfig().containsKeyAnywhere(Const.VALIDATION_DELIVERER)) {
            deserializer = new DefaultDeserializer();
            validator = new MessageValidator(this.getClass(),topic,Configurator.getDefaultConfig().getLong(Const.VALIDATION_LOT_SIZE));
        }else{
            deserializer = null;
            validator = null;
        }

    }

    public synchronized void setIsActive(boolean activeTopic) {
        this.activeTopic = activeTopic;
    }

    private boolean activeTopic = true;

    public synchronized void addObserver(MqttMessageObserver observer){
        if(!observers.contains(observer))
            observers.add(observer);
        else
            loggerService.warn("the same observer was intent to be added in the same Message");
    }

    @Override
    public void run() {
        MqttMessage message;
        boolean active;
        synchronized (this) {
             active = activeTopic;
        }
        while (active) {

            // loggerService.debug(" Started the topic loop");
            try {
                message = mqttMessages.take();
                if(message.getSequence() != -255 && message.getQoS() != -255 ) { // this is not an end message
                    // loggerService.debug("Processing incoming message of topic "+ message.getTopic());
                    synchronized (this) {
                        for (MqttMessageObserver observer : observers)
                            observer.update(message);
                    }
                }

            } catch (Exception e) {
               loggerService.error(e.getMessage(),e);
            }
            synchronized (this) {
                active = activeTopic;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        synchronized (this) {
            activeTopic = false;
        }
    }

    public synchronized void deleteObserver(MqttMessageObserver listener) {
        observers.remove(listener);
    }

    public synchronized int countObservers() {
        return observers.size();
    }

    public synchronized void addMessage(MqttMessage mqttMessage){

        mqttMessages.add(mqttMessage);

       // if(VALIDATION_MODE) toValidation(mqttMessage.getTopic(),mqttMessage.getPayload());
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

    public boolean containsListener(MqttMessageObserver listener) {

        return observers.contains(listener);
    }

    synchronized void destroy(){
        activeTopic = false;
        try{
            MqttMessage endMessage = new MqttMessage();
            endMessage.setQoS(-255);
            endMessage.setSequence(-255);
            mqttMessages.put(endMessage);
        }catch (Exception e){

        }
    }
}
