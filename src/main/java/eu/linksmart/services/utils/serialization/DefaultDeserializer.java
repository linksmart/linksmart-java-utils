package eu.linksmart.services.utils.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.linksmart.services.utils.function.Utils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by José Ángel Carvajal on 01.09.2016 a researcher of Fraunhofer FIT.
 */
public class DefaultDeserializer implements Deserializer{
    protected ObjectMapper mapper = new ObjectMapper();
    public DefaultDeserializer(){
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);

        //mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.setDateFormat(Utils.getDateFormat());
        mapper.setTimeZone(Utils.getTimeZone());
    }

    @Override
    public <T> T parse(String string, Class<T> tClass) throws IOException {
        return mapper.readValue(string, tClass);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) throws IOException {
        return mapper.readValue(bytes, tClass);
    }

    @Override
    public <I,C extends I> boolean defineClassToInterface(Class<I> tInterface,Class<C>... tClass ) {
        Arrays.stream(tClass).forEach(t ->
            mapper.registerModule(new SimpleModule(tInterface.getName(), Version.unknownVersion()).addAbstractTypeMapping(tInterface, t))
        );
        return true;
    }

    @Override
    public <T> List<T> parseArrayOf(String objectString, Class<T> tClass) throws IOException, NotImplementedException {
        return mapper.readValue(objectString,new TypeReference<List<T>>() {} );
    }

    @Override
    public <T> List<T> deserializeArrayOf(byte[] bytes,  Class<T> tClass) throws IOException, NotImplementedException {
        return mapper.readValue(bytes,new TypeReference<List<T>>() {} );
    }

    @Override
    public void addModule(IOModule module) {
        mapper.registerModule((Module) module);
    }

    @Override
    public void close() {

    }
}
