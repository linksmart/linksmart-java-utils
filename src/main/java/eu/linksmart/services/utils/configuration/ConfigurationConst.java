package eu.linksmart.services.utils.configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by José Ángel Carvajal on 06.08.2015 a researcher of Fraunhofer FIT.
 */
public interface ConfigurationConst {

    Set<String> DEFAULT_CONFIGURATION_FILE = initConst();
    String DEFAULT_DIRECTORY_CONFIGURATION_FILE = "__def__utils__conf__.cfg";
    Character ListDelimiter = ',';

    static Set<String> initConst(){
        Set<String> list = new HashSet<>();
        list.add(DEFAULT_DIRECTORY_CONFIGURATION_FILE);
        return list;
    }
}
