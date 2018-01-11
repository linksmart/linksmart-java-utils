package eu.linksmart.test.services.utils.serialization;

import org.junit.Test;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Created by José Ángel Carvajal on 09.01.2018 a researcher of Fraunhofer FIT.
 */
public class LoggingTest {
    @Test
    public  void test(){
        main(new String[]{});
    }
    public static void main(String[] args){

        Logger logger = LogManager.getLogger(LoggingTest.class);
        if(logger.isInfoEnabled())
            logger.info("info");
        if(logger.isDebugEnabled())
            logger.debug("debug");
        if(logger.isErrorEnabled())
            logger.error("error");
        if(logger.isWarnEnabled())
            logger.warn("warn");
        if(logger.isTraceEnabled())
            logger.trace("trace");
    }
}
