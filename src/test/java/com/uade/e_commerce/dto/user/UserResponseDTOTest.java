package com.uade.e_commerce.dto.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import com.uade.e_commerce.model.Role;
import com.uade.e_commerce.model.User;

import tools.jackson.databind.json.JsonMapper;

@JsonTest
class UserResponseDTOTest {

    @Autowired
    private JsonMapper jsonMapper;

    private User buildUser(boolean enabled) {
        return new User(1L, "Ada", "Lovelace", "ada@test.com", "hash", "L1",
                LocalDateTime.of(2026, 8, 1, 9, 0), enabled, Role.USER);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toJson(User user) {
        String json = jsonMapper.writeValueAsString(UserResponseDTO.fromEntity(user));
        return jsonMapper.readValue(json, Map.class);
    }

    @Test
    void fromEntity_serializesEnabledFlagAsIsEnabled() {
        Map<String, Object> json = toJson(buildUser(false));

        assertThat(json).containsEntry("isEnabled", false);
        assertThat(json).doesNotContainKey("IsEnabled");
    }
}
