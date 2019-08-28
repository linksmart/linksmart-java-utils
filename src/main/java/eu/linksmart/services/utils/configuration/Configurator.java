package eu.linksmart.services.utils.configuration;


import eu.linksmart.services.utils.function.Utils;
import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;


import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by José Ángel Carvajal on 06.08.2015 a researcher of Fraunhofer FIT.
 */
public class Configurator extends CombinedConfiguration {

    private static final Configurator def= init();
    private List<String> loadedFiles = new ArrayList<>();
    private ConcurrentMap<String,Object> runtime= new ConcurrentHashMap<>();
    private boolean enableEnvironmentalVariables =false;

    static public synchronized Configurator getDefaultConfig(){

       if( def.loadedFiles.stream().allMatch(ConfigurationConst.DEFAULT_CONFIGURATION_FILE::contains) )
           return def;
        else
            ConfigurationConst.DEFAULT_CONFIGURATION_FILE.stream().filter(Objects::nonNull).forEach(f->Configurator.addConfFile(f));


        return def;
    }

    static public synchronized boolean addConfFile(String filePath) {
     
        ConfigurationConst.DEFAULT_CONFIGURATION_FILE.add(filePath);

        return def.addConfigurationFile(filePath);
    }
    public synchronized boolean addConfigurationFile(String filePath) {
        if(!fileExists(filePath))
            return false;
        if(!loadedFiles.contains(filePath)) {
            this.addConfiguration(new Configurator(filePath));
            loadedFiles.add(filePath);
        }
       return true;
    }

    static protected Configurator init(){
        Configurator configurator = new Configurator();

        ConfigurationConst.DEFAULT_CONFIGURATION_FILE.stream().filter(confFile -> !ConfigurationConst.DEFAULT_DIRECTORY_CONFIGURATION_FILE.equals(confFile)).filter(Objects::nonNull).forEach(confFile -> configurator.append(new Configurator(confFile)));

        return configurator;

    }
    protected Configurator() {
        this(ConfigurationConst.DEFAULT_DIRECTORY_CONFIGURATION_FILE);

    }


    public Configurator(String configurationFile) {
        super();

        String extension;


        if(!fileExists(configurationFile)) {
            System.err.println("File named " + configurationFile + " was not found!'");
            return;
        }
        if(configurationFile !=null){
            int i = configurationFile.lastIndexOf('.');
            if (i > 0) {
                extension = configurationFile.substring(i + 1);
                try {
                    switch (extension) {
                        case "properties":
                        case "cfg":
                            addConfiguration(factoryBuilder(configurationFile).getConfiguration(), configurationFile);
                            break;
                        case "xml":
                            try {
                                addConfiguration(factoryBuilder(configurationFile, XMLPropertiesConfiguration.class).getConfiguration());
                            } catch (Exception e) {
                                addConfiguration(factoryBuilder(configurationFile, XMLConfiguration.class).getConfiguration());
                            }
                            break;
                        default:
                            System.err.println("Not known extension of the configuration file trying to load as property file");
                            addConfiguration(factoryBuilder(configurationFile).getConfiguration());
                            break;
                    }
                    loadedFiles.add(configurationFile);
                } catch (Exception e) {

                    e.printStackTrace();

                }
            }else
                System.err.println("File named " + configurationFile + " doesn't have an extension");
        }
    }



    private static boolean fileExists(String filename){
        File f = new File(filename);
        URL u = Thread.currentThread().getContextClassLoader().getResource(filename);
        return (f.exists() && !f.isDirectory())|| u!=null;
    }

    public Date getDate(String key){

        try {
            return Utils.getDateFormat().parse( getString(key));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void enableEnvironmentalVariables(){
        if(!enableEnvironmentalVariables){
            this.addConfiguration(new EnvironmentConfiguration(), EnvironmentConfiguration.class.getCanonicalName());
            this.loadedFiles.add(EnvironmentConfiguration.class.getCanonicalName());
        }
        enableEnvironmentalVariables = true;
    }

    public boolean isEnvironmentalVariablesEnabled(){
        return enableEnvironmentalVariables;
    }


    static public  FileBasedConfigurationBuilder<? extends FileBasedConfiguration> factoryBuilder(String filename, Class<? extends FileBasedConfiguration> confType){
        return new FileBasedConfigurationBuilder<>(confType)
                        .configure(new Parameters().properties()
                                .setFileName(filename)
                                .setThrowExceptionOnMissing(false)
                                .setListDelimiterHandler(new DefaultListDelimiterHandler(ConfigurationConst.ListDelimiter))
                                .setIncludesAllowed(true)

                        );

    }
    static public  CombinedConfigurationBuilder factoryBuilder2(String filename){
        return new CombinedConfigurationBuilder()
                .configure(new Parameters().properties()
                        .setFileName(filename)
                        .setThrowExceptionOnMissing(true)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(ConfigurationConst.ListDelimiter))
                        .setIncludesAllowed(false));
    }
    static public  FileBasedConfigurationBuilder<PropertiesConfiguration> factoryBuilder(String filename){

        return (FileBasedConfigurationBuilder<PropertiesConfiguration>) factoryBuilder(filename, PropertiesConfiguration.class);
    }
    synchronized private <T> Object getProperty(String key, Class<T> propertyClass){

        if (this.containsKey(key)) {
            if (super.getProperty(key) instanceof Object[]) {
                Object[] aux = (Object[])super.getProperty(key);

                return aux[aux.length>0?aux.length-1:0];

            }else if (super.getProperty(key) instanceof ArrayList ) {

                ArrayList aux = (ArrayList)super.getProperty(key);

                if(List.class.isAssignableFrom(propertyClass)){
                    if(!aux.isEmpty() && !List.class.isAssignableFrom(aux.iterator().next().getClass()))
                        return super.getList(key);

                }

                return aux.get(aux.size()>0?aux.size()-1:0);

            }
            if(List.class.isAssignableFrom(propertyClass) && !(super.getProperty(key) instanceof List)) {

                return super.getList(key);
            }
            return super.get(propertyClass,key);

        }

        return null;
    }
    synchronized private int mostRecentConf(String key){
        for(int i= super.getConfigurationNameList().size()-1; i>=0; i--){
            if(super.getConfiguration(i).containsKey(key))
                return i;
        }
        return -1; // not found; return default conf
    }

    @Override
    public boolean getBoolean(String key) {
        Boolean re= get(key, Boolean.class);
        if(re==null) {
            System.err.println("Key "+key+" has value null o no value. The system then set the value false (null not possible value in boolean)");
            re = false;
        }
        return re;
    }

    @Override
    public byte getByte(String key) {
        return get(key, Byte.class);
    }

    @Override
    public double getDouble(String key) {
        return get(key, Double.class);
    }

    @Override
    public float getFloat(String key) {
        return get(key, Float.class);
    }

    @Override
    public int getInt(String key) {
        return get(key, Integer.class);
    }

    @Override
    public long getLong(String key) {
        return get(key, Long.class);
    }

    @Override
    public short getShort(String key) {
        return get(key, Short.class);
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return get(key, BigDecimal.class);
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return get(key, BigInteger.class);
    }

    @Override
    public String getString(String key) {
        return get(key, String.class);
    }

    @Override
    public String[] getStringArray(String key) {
        int i = mostRecentConf(key);
        if(i<0)
            return null;

        String[] result = this.getConfiguration(i).getStringArray(key);

        // fix the issue that when the array is given by env var the whole is detected as string instead of an array
        if(result.length==1 && result[0].contains(","))
            return result[0].split(",");
        else
            return result;
    }
    public List<String> getStringList(String key) {
        String [] aux = getStringArray(key);
        if( aux != null)
            return Arrays.asList( getStringArray(key));
        return new ArrayList<>();
    }
    private <T>  T get(String key, Class<T> type){
        int i = mostRecentConf(key);

        return (T)runtime.getOrDefault(key,(i>-1?this.getConfiguration(i).get(type,key):null));
    }
    public void setSetting(String key, Object value){
        runtime.put(key, value);
    }

    @Override
    public  List<Object> getList(String key) {
        int i = mostRecentConf(key);

        return i>-1?this.getConfiguration(i).getList(key):null;
    }
    public boolean containsKeyAnywhere(String key){

        return mostRecentConf(key)>-1;
    }



}
