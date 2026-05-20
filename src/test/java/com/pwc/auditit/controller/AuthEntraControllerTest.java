package com.pwc.auditit.controller;

import com.pwc.auditit.dto.response.AuthResponse;
import com.pwc.auditit.dto.response.EntraAuthResponseDto;
import com.pwc.auditit.dto.response.EntraIdTokenResponseDto;
import com.pwc.auditit.dto.response.EntraUserInfoDto;
import com.pwc.auditit.service.EntraIdAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthEntraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EntraIdAuthService entraIdAuthService;

    @Test
    void callbackWithoutCodeRedirectsToFrontendErrorInsteadOfForbidden() throws Exception {
        mockMvc.perform(get("/auth/callback"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("http://localhost:5173/auth/callback?error=authentication_failed&error_description=*"))
                .andExpect(header().string("Location", containsString("Authorization%20code%20is%20missing")));
    }

    @Test
    void callbackWithCodeRedirectsToFrontendWithTokenAndUser() throws Exception {
        EntraUserInfoDto entraUser = EntraUserInfoDto.builder()
                .email("mahdi@example.com")
                .displayName("Mahdi Hammami")
                .givenName("Mahdi")
                .familyName("Hammami")
                .build();
        EntraIdTokenResponseDto tokenResponse = EntraIdTokenResponseDto.builder()
                .accessToken("graph-access-token")
                .refreshToken("graph-refresh-token")
                .expiresIn(3600)
                .build();

        AuthResponse.UserInfo user = AuthResponse.UserInfo.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .email("mahdi@example.com")
                .firstName("Mahdi")
                .lastName("Hammami")
                .fullName("Mahdi Hammami")
                .build();

        when(entraIdAuthService.exchangeCodeForTokenWithResponse("valid-code"))
                .thenReturn(new EntraIdAuthService.EntraExchangeResult(entraUser, tokenResponse));
        when(entraIdAuthService.completeAuthentication(entraUser, tokenResponse)).thenReturn(
                EntraAuthResponseDto.builder()
                        .token("jwt-token")
                        .user(user)
                        .build()
        );

        mockMvc.perform(get("/auth/callback").param("code", "valid-code"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("http://localhost:5173/auth/callback?token=jwt-token")))
                .andExpect(header().string("Location", containsString("user=")))
                .andExpect(header().string("Location", containsString("mahdi@example.com")));
    }
}
