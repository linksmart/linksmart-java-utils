package eu.linksmart.testing.tooling;

/**
 * Created by José Ángel Carvajal on 29.11.2016 a researcher of Fraunhofer FIT.
 */
public interface Publisher {
    default void publish(String topic, String payload, int qos, boolean retain) throws Exception {
     publish(topic,payload.getBytes(),qos,retain);
    }
    void publish(String topic, byte[] payload, int qos, boolean retain) throws Exception ;

    void  disconnect() ;

    void close();
}
