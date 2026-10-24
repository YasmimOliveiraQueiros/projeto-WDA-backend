package com.altis.library.users.models.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProfileContractTest {

    @Test
    void responseUsesStableFrontendPropertyNamesAndNeverExposesPassword() throws Exception {
        UserResponse response = new UserResponse();
        response.setIsAdmin(false);
        response.setAddress("Rua Um");

        JsonNode json = new ObjectMapper().findAndRegisterModules().valueToTree(response);

        assertTrue(json.has("address"));
        assertTrue(json.has("createdAt"));
        assertTrue(json.has("updatedAt"));
        assertTrue(json.has("isAdmin"));
        assertFalse(json.has("adress"));
        assertFalse(json.has("createAt"));
        assertFalse(json.has("updateAt"));
        assertFalse(json.has("admin"));
        assertFalse(json.has("password"));
    }

    @Test
    void profileAndPasswordDtosRejectMissingFieldsAndWeakNewPassword() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            assertFalse(validator.validate(new UserProfileUpdateRequest()).isEmpty());
            assertFalse(validator.validate(new PasswordChangeRequest()).isEmpty());

            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setCurrentPassword("current");
            request.setNewPassword("short");
            request.setConfirmPassword("short");
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> "newPassword".equals(error.getPropertyPath().toString())));
        }
    }

    @Test
    void protectedFieldsAreNotAcceptedByTheProfileUpdateContract() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        String request = """
                {
                  "name": "Updated",
                  "email": "user@example.com",
                  "phone": "555-0100",
                  "address": "New address",
                  "cpf": "00000000000"
                }
                """;

        assertThrows(
                JsonMappingException.class,
                () -> objectMapper.readValue(request, UserProfileUpdateRequest.class)
        );
    }
}
