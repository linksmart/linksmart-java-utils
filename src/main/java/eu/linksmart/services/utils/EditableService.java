package eu.linksmart.services.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.client.model.Service;

public class EditableService extends Service {
        @JsonProperty("id")
        protected String id;
        @Override
        public String getId() {
            return id;
        }

        public void setId(String id){
            this.id=id;
        }

    }