package com.backkeesun.inflearnrestapi.account;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class AccountSerializer extends JsonSerializer<Account> {
    @Override
    public void serialize(Account account, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeStartObject();
        //내보낼 정보 작성
        generator.writeNumberField("id",account.getId());
        generator.writeStringField("email",account.getEmail());

        generator.writeEndObject();
    }
}
