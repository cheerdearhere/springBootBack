package com.backkeesun.inflearnrestapi.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.validation.Errors;

import java.io.IOException;

@JsonComponent
public class ErrorsSerializer extends JsonSerializer<Errors> {//JSON String으로 변환할 대상 지정
    @Override
    public void serialize(Errors errors, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Logger logger = LoggerFactory.getLogger(getClass());
        gen.writeFieldName("errors");//spring boot 2.3부터 Jackson library가 더이상 Array부터 만드는 것을 금지함
        gen.writeStartArray(); // start generating error Array
        errors.getFieldErrors().forEach(e->{
            try{
                gen.writeStartObject(); //start generating Errors Object
                gen.writeStringField("field", e.getField());
                gen.writeStringField("objectName", e.getObjectName());
                gen.writeStringField("code", e.getCode());
                gen.writeStringField("defaultMessage", e.getDefaultMessage());
                Object rejectedValue = e.getRejectedValue();
                if (rejectedValue != null) {
                    gen.writeStringField("rejectedValue", rejectedValue.toString());
                }
                gen.writeEndObject(); // finish generating Errors Object
            }catch (IOException ie){
                logger.error(ie.getMessage());
            }
        });
        errors.getGlobalErrors().forEach(error->{
            try{
                gen.writeStartObject();
                gen.writeStringField("field","GLOBAL");
                gen.writeStringField("objectName",error.getObjectName());
                gen.writeStringField("code",error.getCode());
                gen.writeStringField("defaultMessage",error.getDefaultMessage());
                gen.writeStringField("rejectedValue", "globalError");
                gen.writeEndObject();
            }catch (IOException ie){
                logger.error(ie.getMessage());
            }
        });
        gen.writeEndArray(); // end generating error Array
    }
}
