package eu.linksmart.services.utils.mqtt.broker;

import eu.linksmart.services.utils.mqtt.subscription.MqttMessageObserver;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

/**
 * Created by José Ángel Carvajal on 23.10.2015 a researcher of Fraunhofer FIT.
 */
public interface Broker extends Observer{

    Pattern ipPattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+"), urlPattern = Pattern.compile("\\b(tcp|ws|ssl)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|][:[0-9]+]?");



    boolean isConnected() ;
    void connect() throws Exception ;
    void disconnect() throws Exception ;
    void destroy() throws Exception;
    String getBrokerURL();

    void createClient() throws MqttException;

    //public boolean isWatchdog() ;



    void publish(String topic, byte[] payload, int qos, boolean retained) throws Exception;
    void publish(String topic, byte[] payload) throws Exception ;
    void publish(String topic, String payload) throws Exception;

    String getBrokerName();

    void setBrokerName(String brokerName) throws Exception ;

    String getBrokerPort();

    boolean hasListeners();

    void setBrokerPort(String brokerPort) throws Exception ;
    void setBroker(String brokerName, String brokerPort) throws Exception;

    boolean addListener(String topic, Observer stakeholder);
    boolean addListener(String topic, Observer stakeholder, int QoS);

    void addConnectionListener(Observer listener);

    default boolean addListener(String topic, MqttMessageObserver stakeholder){
      return addListener(topic,(Observer)stakeholder);
    }

    default boolean addListener(String topic, MqttMessageObserver stakeholder, int QoS){
        return addListener(topic,(Observer)stakeholder,QoS);
    }

    boolean removeListener(String topic, Observer stakeholder);
    void removeListener( Observer stakeholder);
    BrokerConfiguration getConfiguration();

    String getAlias();

    @Override
    void update(Observable o, Object arg) ;

    static String getBrokerURL(String brokerName, String brokerPort){

        if (ipPattern.matcher(brokerName).find())
            return "tcp://"+brokerName+":"+brokerPort;
        else
            return "tcp://"+brokerName+":"+brokerPort;
    }
    static String getSecureBrokerURL(String brokerName, String brokerPort){

        if (ipPattern.matcher(brokerName).find())
            return "ssl://"+brokerName+":"+brokerPort;
        else
            return "ssl://"+brokerName+":"+brokerPort;
    }
    static String getBrokerURL(String brokerName, int brokerPort){
        return getBrokerURL(brokerName,String.valueOf(brokerPort));
    }
    static String getSecureBrokerURL(String brokerName, int brokerPort){
        return getSecureBrokerURL(brokerName, String.valueOf(brokerPort));
    }
    static String getBrokerURL(String brokerName, String brokerPort, boolean isSSL_URL){
        if (isSSL_URL)
            return getSecureBrokerURL(brokerName,brokerPort);
        else
            return getBrokerURL(brokerName, brokerPort);
    }
    static boolean isBrokerURL(String string){
        return  urlPattern.matcher(string).find();

    }
}
