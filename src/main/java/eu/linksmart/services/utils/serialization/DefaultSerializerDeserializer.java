package eu.linksmart.services.utils.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.linksmart.services.utils.configuration.Configurator;
import eu.linksmart.services.utils.constants.Const;
import eu.linksmart.services.utils.function.Utils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by José Ángel Carvajal on 13.06.2018 a researcher of Fraunhofer FIT.
 */
public class DefaultSerializerDeserializer  implements SerializerDeserializer, Deserializer, Serializer {
    static protected Configurator conf = Configurator.getDefaultConfig();


    private ObjectMapper parser = new ObjectMapper();
    public DefaultSerializerDeserializer(){
        parser.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        parser.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if(conf !=null && conf.containsKeyAnywhere(Const.TIME_EPOCH_CONF_PATH))
            parser.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, conf.getBoolean(Const.TIME_EPOCH_CONF_PATH));

        //parser.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //parser.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        parser.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        parser.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        parser.setDateFormat(Utils.getDateFormat());
        parser.setTimeZone(Utils.getTimeZone());
        parser.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    }

    @Override
    public <T> T parse(String string, Class<T> tClass) throws IOException {
        return parser.readValue(string, tClass);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) throws IOException {
        return parser.readValue(bytes, tClass);
    }

    @Override
    public <I,C extends I> boolean defineClassToInterface(Class<I> tInterface,Class<C>... tClass ) {
        Arrays.stream(tClass).forEach(t ->
                parser.registerModule(new SimpleModule(tInterface.getName(), Version.unknownVersion()).addAbstractTypeMapping(tInterface, t))
        );
        return true;
    }

    @Override
    public Object parsePacked(String objectString, TypeReference type) throws IOException, UnsupportedOperationException {
        return parser.readValue(objectString,type );
    }

    @Override
    public Object deserializePacked(byte[] bytes, TypeReference type) throws IOException, UnsupportedOperationException {
        return parser.readValue(bytes,type );
    }

    @Override
    public <T> void addModule(String name, Class<T> tClass, DeserializerMode<T> deserializerMode) {
        parser.registerModule(new SimpleModule(name, Version.unknownVersion()).addDeserializer(tClass, deserializerMode));
    }
    @Override
    public <I,C extends I> void addModule(String name, Class<I> tInterface, Class<C> tClass) {
        parser.registerModule(new SimpleModule(name, Version.unknownVersion()).addAbstractTypeMapping(tInterface,tClass));

    }



    @Override
    public void close() {

    }

    @Override
    public Object getParser() {
        return parser;
    }

    @Override
    public byte[] serialize(Object object) throws IOException {

        try {
            return parser.writeValueAsString(object).getBytes();
        } catch (JsonProcessingException e) {
            throw new IOException(e.getMessage(),e);
        }
    }

    @Override
    public String toString(Object object) throws IOException {
        try {
            return parser.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IOException(e.getMessage(),e);
        }
    }
    @Override
    public <T> void addModule(String name, Class<T> tClass, SerializerMode<T> serializerMode) {

        parser.registerModule(new SimpleModule(name, Version.unknownVersion()).addSerializer(tClass, serializerMode));
    }
}
