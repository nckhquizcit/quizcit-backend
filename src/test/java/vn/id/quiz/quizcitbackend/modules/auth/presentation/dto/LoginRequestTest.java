package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void acceptsIdentifierUsernameAndEmailJsonFieldNames() throws Exception {
        LoginRequest identifier = objectMapper.readValue(
                "{\"identifier\":\"student01\",\"password\":\"secret123\"}", LoginRequest.class);
        LoginRequest username = objectMapper.readValue(
                "{\"username\":\"student01\",\"password\":\"secret123\"}", LoginRequest.class);
        LoginRequest email = objectMapper.readValue(
                "{\"email\":\"student01@example.com\",\"password\":\"secret123\"}", LoginRequest.class);

        assertThat(identifier.identifier()).isEqualTo("student01");
        assertThat(username.identifier()).isEqualTo("student01");
        assertThat(email.identifier()).isEqualTo("student01@example.com");
    }
}
