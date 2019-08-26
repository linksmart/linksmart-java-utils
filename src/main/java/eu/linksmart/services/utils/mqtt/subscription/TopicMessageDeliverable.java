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
    private LinkedList<Observer> observers = new LinkedList<>();
    protected final String topic;

    //Start of code made for testing performance
    private final boolean VALIDATION_MODE;
    private final Deserializer deserializer;
    private final MessageValidator validator;
    //End of code made for testing performance

    protected transient Logger loggerService = LogManager.getLogger(TopicMessageDeliverable.class);
    public TopicMessageDeliverable(String topic) {
        // loggerService.debug("Starting new ");
        Thread thread =new Thread(this);
        thread.start();

        this.topic=topic;

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

    public synchronized void addObserver(Observer observer){
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
                // loggerService.debug("Processing incoming message of topic "+ message.getTopic());
                synchronized (this) {
                    for (Observer observer : observers)
                        observer.update(null, message);
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

    public synchronized void deleteObserver(Observer listener) {
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

    public boolean containsListener(Observer listener) {

        return observers.contains(listener);
    }
}
