package eu.linksmart.services.utils.mqtt.broker;

/**
 * Created by José Ángel Carvajal on 06.08.2015 a researcher of Fraunhofer FIT.
 */
public interface BrokerServiceConst {
    String MULTI_CONNECTION = "connection_brokers_service_connection_multiple";

    String BROKERS_ALIAS = "connection_brokers_aliases";

    String DEFAULT_HOSTNAME= "connection_broker_mqtt_hostname";
    String DEFAULT_PORT= "connection_broker_mqtt_port";
    String DEFAULT_PORT_SECURE= "connection_broker_mqtt_security_port";
    String DEFAULT_CONNECTION_PERSISTENCY= "connection_broker_mqtt_enableFileConnectionPersistency";
    String RECONNECTION_TRY = "connection_broker_mqtt_noReconnectTry";
    String RECONNECTION_MQTT_RETRY_TIME = "connection_broker_mqtt_reconnectWaitingTime";
    String CONNECTION_MQTT_KEEP_ALIVE_TIMEOUT = "connection_broker_mqtt_keepAlive";
    String CONNECTION_MQTT_CONNECTION_TIMEOUT = "connection_broker_mqtt_timeOut";

    String DEFAULT_PUBLISH_QOS = "messaging_client_mqtt_pub_qos";
    String DEFAULT_RETAIN_POLICY= "messaging_client_mqtt_enableRetainPolicy";
    String DEFAULT_SUBSCRIPTION_QoS ="messaging_client_mqtt_sub_qos";
    String CLEAN_SESSION = "messaging_client_mqtt_session_clean_enabled";

    String CA_CERTIFICATE_PATH ="connection_broker_mqtt_security_caCertificatePath";
    String CERTIFICATE_FILE_PATH ="connection_broker_mqtt_security_certificatePath";
    String KEY_FILE_PATH ="connection_broker_mqtt_security_keyPath";
    String CERTIFICATE_BASE_SECURITY ="connection_broker_mqtt_security_certificateBaseSecurityEnabled";
    String CERTIFICATE_PASSWORD = "connection_broker_mqtt_security_certificatePassword";
    String KEY_PASSWORD = "connection_broker_mqtt_security_keyPassword";
    String CA_CERTIFICATE_PASSWORD = "connection_broker_mqtt_security_caCertificatePassword";


    String MAX_IN_FLIGHT = "messaging_client_mqtt_maxInFlightMessages";
    String MQTT_VERSION = "messaging_client_mqtt_version";
    String AUTOMATIC_RECONNECT = "messaging_client_mqtt_automaticReconnect";

    String USER = "messaging_client_mqtt_security_user";
    String PASSWORD = "messaging_client_mqtt_security_password";
    String AUTOBLACKLISTING = "messaging_client_mqtt_autoblacklisting";
}
