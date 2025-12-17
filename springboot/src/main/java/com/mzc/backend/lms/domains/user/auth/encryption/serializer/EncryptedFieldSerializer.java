package com.mzc.backend.lms.domains.user.auth.encryption.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 암호화된 필드를 복호화하여 직렬화하는 Jackson Serializer
 */
@Slf4j
@Component
public class EncryptedFieldSerializer extends JsonSerializer<String> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService encryptionService) {
        EncryptedFieldSerializer.encryptionService = encryptionService;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeString(value);
            return;
        }

        try {
            String decrypted = encryptionService.decryptPersonalInfo(value);
            gen.writeString(decrypted);
        } catch (Exception e) {
            log.warn("복호화 실패, 원본 반환: {}", e.getMessage());
            gen.writeString(value);
        }
    }
}