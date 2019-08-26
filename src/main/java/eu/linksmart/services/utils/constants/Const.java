package eu.linksmart.services.utils.constants;

import eu.linksmart.services.utils.logging.LoggerServiceConst;
import eu.linksmart.services.utils.mqtt.broker.BrokerServiceConst;

/**
 * Created by José Ángel Carvajal on 06.08.2015 a researcher of Fraunhofer FIT.
 */
public interface Const extends   BrokerServiceConst,LoggerServiceConst {
    String TIME_FORMAT_CONF_PATH = "general_time_timestamp_format";
    String TIME_TIMEZONE_CONF_PATH = "general_time_zone";
    String TIME_EPOCH_CONF_PATH = "general_time_epoch";
    String TIME_ISO_FORMAT_MS_TZ = "yyyy-MM-dd'T'HH:mm:ss.SSZ" ;
    String TIME_ISO_FORMAT_WMS_TZ = "yyyy-MM-dd'T'HH:mm:ssZ" ;
    String TIME_ISO_FORMAT_MS_WTZ = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'" ;
    String TIME_ISO_FORMAT_WMS_WTZ = "yyyy-MM-dd'T'HH:mm:ss'Z'" ;

    String CONFIGURATION_CLASS_FILE = "eu_linksmart_services_Application";

    String VALIDATION_LOT_SIZE = "test_validation_lot_size";
    String VALIDATION_FORWARDING = "test_validation_message_forwarding_enabled";
    String VALIDATION_OBSERVERS = "test_validation_mqtt_observers_enabled";

    String VALIDATION_DELIVERER = "test_validation_message_deliverer";

    String LINKSMART_SERVICE_CATALOG_ENDPOINT = "linksmart_service_catalog_endpoint";

    String LINKSMART_BROKER = "linksmart_broker";
}
