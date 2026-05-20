package com.pwc.auditit.config;

import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.repository.ProfileRepository;
import com.pwc.auditit.security.JwtService;
import com.pwc.auditit.service.MissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private ProfileRepository profileRepository;

    @MockBean
    private MissionService missionService;

    @Test
    void protectedEndpointWithoutAuthorizationHeaderReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/missions"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Missing Authorization header")));
    }

    @Test
    void protectedEndpointWithValidJwtReturnsOk() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        Profile profile = Profile.builder()
                .id(userId)
                .email("mahdi@example.com")
                .fullName("Mahdi Hammami")
                .build();
        String token = jwtService.generateToken(userId, profile.getEmail());

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(missionService.getAllMissions(isNull(), isNull(), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/missions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointFallsBackToTokenEmailWhenJwtSubjectIdDoesNotResolve() throws Exception {
        UUID tokenUserId = UUID.fromString("00000000-0000-0000-0000-000000000011");
        UUID profileUserId = UUID.fromString("00000000-0000-0000-0000-000000000012");
        Profile profile = Profile.builder()
                .id(profileUserId)
                .email("mahdi@example.com")
                .fullName("Mahdi Hammami")
                .build();
        String token = jwtService.generateToken(tokenUserId, profile.getEmail());

        when(profileRepository.findById(tokenUserId)).thenReturn(Optional.empty());
        when(profileRepository.findByEmail(profile.getEmail())).thenReturn(Optional.of(profile));
        when(missionService.getAllMissions(isNull(), isNull(), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/missions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
