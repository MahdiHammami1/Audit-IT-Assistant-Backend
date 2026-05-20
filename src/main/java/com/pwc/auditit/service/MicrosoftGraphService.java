package com.pwc.auditit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pwc.auditit.config.GraphProperties;
import com.pwc.auditit.dto.GenerateCdwReportResponse;
import com.pwc.auditit.dto.GraphFileDto;
import com.pwc.auditit.dto.GraphTokenResponse;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.exception.MicrosoftGraphException;
import com.pwc.auditit.repository.ProfileRepository;
import com.pwc.auditit.util.ByteArrayMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MicrosoftGraphService {

    private static final String EXCEL_XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXCEL_XLS_CONTENT_TYPE = "application/vnd.ms-excel";
    private static final long TOKEN_REFRESH_SKEW_SECONDS = 90;

    private final GraphProperties graphProperties;
    private final GraphOAuthStateStore stateStore;
    private final ProfileRepository profileRepository;
    private final CdwReportService cdwReportService;
    private final WebClient.Builder webClientBuilder;

    public String createAuthorizationStateForCurrentUser() {
        return createAuthorizationStateForCurrentUser("/cdw-generation");
    }

    public String createAuthorizationStateForCurrentUser(String returnPath) {
        Profile currentProfile = getCurrentProfile();
        return stateStore.createState(currentProfile.getId(), returnPath);
    }

    public String buildAuthorizationUrl() {
        Profile currentProfile = getCurrentProfile();
        String state = stateStore.createState(currentProfile.getId());
        return buildAuthorizationUrl(state);
    }

    public String buildAuthorizationUrl(String state) {
        ensureGraphConfigured();
        if (state == null || state.isBlank()) {
            throw new MicrosoftGraphException(HttpStatus.BAD_REQUEST, "Microsoft Graph OAuth state is required.");
        }
        stateStore.validateState(state);

        return UriComponentsBuilder.fromUriString(authorizationEndpoint())
                .queryParam("client_id", graphProperties.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", graphProperties.getRedirectUri())
                .queryParam("response_mode", "query")
                .queryParam("scope", delegatedScopes())
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    public String getReturnPathOrDefault(String state) {
        return stateStore.getReturnPathOrDefault(state);
    }

    public GraphTokenResponse exchangeCodeForToken(String code) {
        ensureGraphConfigured();
        if (code == null || code.isBlank()) {
            throw new MicrosoftGraphException(HttpStatus.BAD_REQUEST, "Microsoft Graph OAuth callback failed: authorization code is missing.");
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", graphProperties.getClientId());
        formData.add("client_secret", graphProperties.getClientSecret());
        formData.add("code", code);
        formData.add("redirect_uri", graphProperties.getRedirectUri());
        formData.add("grant_type", "authorization_code");
        formData.add("scope", delegatedScopes());

        GraphTokenResponse tokenResponse = requestToken(formData, "Microsoft Graph OAuth callback failed");
        if (tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isBlank()) {
            throw new MicrosoftGraphException(HttpStatus.BAD_GATEWAY, "Microsoft Graph OAuth callback failed: no access token was returned.");
        }
        return tokenResponse;
    }

    public String storeConnection(String state, GraphTokenResponse tokenResponse) {
        GraphOAuthStateStore.ConsumedGraphOAuthState consumedState = stateStore.consumeState(state);
        Profile profile = profileRepository.findById(consumedState.userId())
                .orElseThrow(() -> new MicrosoftGraphException(HttpStatus.NOT_FOUND, "Authenticated user profile was not found."));
        saveGraphTokens(profile, tokenResponse);
        log.info("Microsoft Graph connected for profile {}", profile.getId());
        return consumedState.returnPath();
    }

    public List<GraphFileDto> listExcelFiles() {
        Profile currentProfile = getCurrentProfile();
        String accessToken = getValidAccessToken(currentProfile);

        List<GraphFileDto> files = new ArrayList<>();
        appendExcelSearchResults(files, graphGet("/me/drive/root/search(q='.xlsx')", accessToken, JsonNode.class, "Unable to list Microsoft Graph Excel files."));
        appendExcelSearchResults(files, graphGet("/me/drive/root/search(q='.xls')", accessToken, JsonNode.class, "Unable to list Microsoft Graph Excel files."));

        if (files.isEmpty()) {
            throw new MicrosoftGraphException(HttpStatus.NOT_FOUND, "No Excel files found in OneDrive.");
        }

        return files;
    }

    public byte[] downloadFile(String itemId) {
        Profile currentProfile = getCurrentProfile();
        String accessToken = getValidAccessToken(currentProfile);
        return downloadFileWithToken(itemId, accessToken);
    }

    public DownloadedGraphFile downloadExcelFile(String itemId) {
        Profile currentProfile = getCurrentProfile();
        String accessToken = getValidAccessToken(currentProfile);
        GraphFileDto graphFile = getDriveItem(itemId, accessToken);

        if (!isExcelFileName(graphFile.getName())) {
            throw new MicrosoftGraphException(HttpStatus.BAD_REQUEST, "Only Excel files (.xlsx, .xls) can be imported.");
        }

        byte[] fileBytes = downloadFileWithToken(itemId, accessToken);
        return new DownloadedGraphFile(graphFile.getName(), resolveExcelContentType(graphFile.getName()), fileBytes);
    }

    public GenerateCdwReportResponse generateCdwFromGraphFile(String itemId, UUID missionId) {
        return generateCdwFromGraphFile(itemId, missionId, "docx");
    }

    public GenerateCdwReportResponse generateCdwFromGraphFile(String itemId, UUID missionId, String format) {
        if (missionId == null) {
            throw new MicrosoftGraphException(HttpStatus.BAD_REQUEST, "missionId is required.");
        }

        Profile currentProfile = getCurrentProfile();
        String accessToken = getValidAccessToken(currentProfile);
        GraphFileDto graphFile = getDriveItem(itemId, accessToken);

        if (!isExcelFileName(graphFile.getName())) {
            throw new MicrosoftGraphException(HttpStatus.BAD_REQUEST, "Only Excel files (.xlsx, .xls) can be used to generate a CDW report.");
        }

        byte[] fileBytes = downloadFileWithToken(itemId, accessToken);
        MultipartFile multipartFile = new ByteArrayMultipartFile(
                "file",
                graphFile.getName(),
                resolveExcelContentType(graphFile.getName()),
                fileBytes
        );

        try {
            GenerateCdwReportResponse response = cdwReportService.generateFromUpload(multipartFile, missionId.toString(), format);
            if (response != null && response.getReportId() != null) {
                response.setDownloadUrl("/api/cdw-reports/download/" + response.getReportId());
            }
            return response;
        } catch (Exception e) {
            throw new MicrosoftGraphException(HttpStatus.INTERNAL_SERVER_ERROR, "CDW generation failed: " + e.getMessage(), e);
        }
    }

    private GraphFileDto getDriveItem(String itemId, String accessToken) {
        if (itemId == null || itemId.isBlank()) {
            throw new MicrosoftGraphException(HttpStatus.BAD_REQUEST, "itemId is required.");
        }

        String encodedItemId = UriUtils.encodePathSegment(itemId, StandardCharsets.UTF_8);
        return graphGet("/me/drive/items/" + encodedItemId, accessToken, GraphFileDto.class, "File not found in Microsoft Graph.");
    }

    private void appendExcelSearchResults(List<GraphFileDto> files, JsonNode response) {
        JsonNode values = response != null ? response.path("value") : null;
        if (values == null || !values.isArray()) {
            return;
        }

        values.forEach(item -> {
            GraphFileDto file = mapDriveItem(item);
            boolean alreadyListed = files.stream().anyMatch(existing -> existing.getId() != null && existing.getId().equals(file.getId()));
            if (!alreadyListed && isExcelFileName(file.getName())) {
                files.add(file);
            }
        });
    }

    private byte[] downloadFileWithToken(String itemId, String accessToken) {
        if (itemId == null || itemId.isBlank()) {
            throw new MicrosoftGraphException(HttpStatus.BAD_REQUEST, "itemId is required.");
        }

        String encodedItemId = UriUtils.encodePathSegment(itemId, StandardCharsets.UTF_8);
        return graphGet(
                "/me/drive/items/" + encodedItemId + "/content",
                accessToken,
                byte[].class,
                "Download failed.",
                redirectingWebClient()
        );
    }

    private <T> T graphGet(String path, String accessToken, Class<T> responseType, String failureMessage) {
        return graphGet(path, accessToken, responseType, failureMessage, webClientBuilder.build());
    }

    private <T> T graphGet(String path, String accessToken, Class<T> responseType, String failureMessage, WebClient webClient) {
        try {
            return webClient
                    .get()
                    .uri(graphUrl(path))
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            throw mapGraphResponseException(e, failureMessage);
        } catch (MicrosoftGraphException e) {
            throw e;
        } catch (Exception e) {
            throw new MicrosoftGraphException(HttpStatus.BAD_GATEWAY, failureMessage + " " + e.getMessage(), e);
        }
    }

    private GraphTokenResponse refreshAccessToken(Profile profile) {
        if (profile.getGraphRefreshToken() == null || profile.getGraphRefreshToken().isBlank()) {
            throw new MicrosoftGraphException(HttpStatus.UNAUTHORIZED, "Microsoft Graph token expired. Please reconnect Microsoft Graph.");
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", graphProperties.getClientId());
        formData.add("client_secret", graphProperties.getClientSecret());
        formData.add("refresh_token", profile.getGraphRefreshToken());
        formData.add("grant_type", "refresh_token");
        formData.add("scope", delegatedScopes());

        GraphTokenResponse tokenResponse = requestToken(formData, "Microsoft Graph token expired. Please reconnect Microsoft Graph.");
        saveGraphTokens(profile, tokenResponse);
        return tokenResponse;
    }

    private GraphTokenResponse requestToken(MultiValueMap<String, String> formData, String failureMessage) {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(tokenEndpoint())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(GraphTokenResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("Microsoft Graph token request failed with status {}", e.getStatusCode().value());
            throw new MicrosoftGraphException(HttpStatus.BAD_GATEWAY, failureMessage + " Token exchange failed.");
        } catch (Exception e) {
            throw new MicrosoftGraphException(HttpStatus.BAD_GATEWAY, failureMessage + " " + e.getMessage(), e);
        }
    }

    private String getValidAccessToken(Profile profile) {
        if (profile.getGraphAccessToken() == null || profile.getGraphAccessToken().isBlank()) {
            throw new MicrosoftGraphException(HttpStatus.CONFLICT, "OneDrive access is not available for this session. Sign in with Microsoft again to grant OneDrive access.");
        }

        Instant expiresAt = profile.getGraphTokenExpiresAt();
        if (expiresAt != null && expiresAt.isAfter(Instant.now().plusSeconds(TOKEN_REFRESH_SKEW_SECONDS))) {
            return profile.getGraphAccessToken();
        }

        GraphTokenResponse refreshedToken = refreshAccessToken(profile);
        return refreshedToken.getAccessToken();
    }

    private void saveGraphTokens(Profile profile, GraphTokenResponse tokenResponse) {
        if (tokenResponse == null || tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isBlank()) {
            throw new MicrosoftGraphException(HttpStatus.BAD_GATEWAY, "Microsoft Graph token exchange failed.");
        }

        long expiresIn = tokenResponse.getExpiresIn() != null ? tokenResponse.getExpiresIn() : 3600;
        profile.setGraphAccessToken(tokenResponse.getAccessToken());
        if (tokenResponse.getRefreshToken() != null && !tokenResponse.getRefreshToken().isBlank()) {
            profile.setGraphRefreshToken(tokenResponse.getRefreshToken());
        }
        profile.setGraphTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
        profile.setGraphConnectedAt(Instant.now());
        profileRepository.save(profile);
    }

    private MicrosoftGraphException mapGraphResponseException(WebClientResponseException e, String failureMessage) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
            return new MicrosoftGraphException(HttpStatus.UNAUTHORIZED, "Microsoft Graph token expired or access was denied. Please reconnect Microsoft Graph.");
        }
        if (status == HttpStatus.NOT_FOUND) {
            return new MicrosoftGraphException(HttpStatus.NOT_FOUND, "File not found in Microsoft Graph.");
        }
        return new MicrosoftGraphException(
                status != null ? status : HttpStatus.BAD_GATEWAY,
                failureMessage + " Microsoft Graph returned status " + e.getStatusCode().value() + "."
        );
    }

    private Profile getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Profile profile) {
            return profile;
        }
        throw new MicrosoftGraphException(HttpStatus.UNAUTHORIZED, "Current platform JWT is required.");
    }

    private GraphFileDto mapDriveItem(JsonNode item) {
        return GraphFileDto.builder()
                .id(textOrNull(item, "id"))
                .name(textOrNull(item, "name"))
                .webUrl(textOrNull(item, "webUrl"))
                .size(item.hasNonNull("size") ? item.get("size").asLong() : null)
                .lastModifiedDateTime(textOrNull(item, "lastModifiedDateTime"))
                .build();
    }

    private String textOrNull(JsonNode node, String fieldName) {
        return node.hasNonNull(fieldName) ? node.get(fieldName).asText() : null;
    }

    private boolean isExcelFileName(String fileName) {
        String normalized = fileName != null ? fileName.toLowerCase() : "";
        return normalized.endsWith(".xlsx") || normalized.endsWith(".xls");
    }

    private String resolveExcelContentType(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".xls")
                ? EXCEL_XLS_CONTENT_TYPE
                : EXCEL_XLSX_CONTENT_TYPE;
    }

    private String graphUrl(String path) {
        String baseUrl = graphProperties.getBaseUrl().replaceAll("/+$", "");
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return baseUrl + normalizedPath;
    }

    private String authorizationEndpoint() {
        return "https://login.microsoftonline.com/" + graphProperties.getTenantId() + "/oauth2/v2.0/authorize";
    }

    private String tokenEndpoint() {
        return "https://login.microsoftonline.com/" + graphProperties.getTenantId() + "/oauth2/v2.0/token";
    }

    private String delegatedScopes() {
        Set<String> scopes = new LinkedHashSet<>();
        if (graphProperties.getDelegatedScopes() != null) {
            Arrays.stream(graphProperties.getDelegatedScopes().split("\\s+"))
                    .filter(scope -> scope != null && !scope.isBlank())
                    .forEach(scopes::add);
        }
        scopes.add("User.Read");
        scopes.add("Files.Read");
        scopes.add("offline_access");
        return String.join(" ", scopes);
    }

    private WebClient redirectingWebClient() {
        return webClientBuilder.clone()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .build();
    }

    private void ensureGraphConfigured() {
        if (!graphProperties.isConfigured()) {
            throw new MicrosoftGraphException(HttpStatus.INTERNAL_SERVER_ERROR, "Microsoft Graph is not configured.");
        }
    }

    public record DownloadedGraphFile(String fileName, String contentType, byte[] content) {
    }
}
