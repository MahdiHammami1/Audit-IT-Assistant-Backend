package com.pwc.auditit.controller;

import com.pwc.auditit.dto.GenerateCdwFromGraphRequest;
import com.pwc.auditit.dto.GenerateCdwReportResponse;
import com.pwc.auditit.dto.GraphAuthorizationUrlResponse;
import com.pwc.auditit.dto.GraphFileDto;
import com.pwc.auditit.dto.GraphTokenResponse;
import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.exception.MicrosoftGraphException;
import com.pwc.auditit.service.MicrosoftGraphService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/graph")
@RequiredArgsConstructor
public class MicrosoftGraphController {

    private final MicrosoftGraphService microsoftGraphService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/oauth/start-url")
    public ResponseEntity<ApiResponse<GraphAuthorizationUrlResponse>> getAuthorizationUrl(
            @RequestParam(value = "returnPath", required = false) String returnPath
    ) {
        try {
            String state = microsoftGraphService.createAuthorizationStateForCurrentUser(returnPath);
            String authorizationUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/graph/oauth/start")
                    .queryParam("state", state)
                    .build()
                    .encode()
                    .toUriString();
            return ResponseEntity.ok(ApiResponse.ok(
                    "Microsoft Graph authorization URL generated",
                    new GraphAuthorizationUrlResponse(authorizationUrl)
            ));
        } catch (MicrosoftGraphException e) {
            return graphError(e);
        }
    }

    @GetMapping("/oauth/start")
    public ResponseEntity<Void> startOAuth(@RequestParam(value = "state", required = false) String state) {
        try {
            String authorizationUrl = hasText(state)
                    ? microsoftGraphService.buildAuthorizationUrl(state)
                    : microsoftGraphService.buildAuthorizationUrl();
            return redirectTo(authorizationUrl);
        } catch (MicrosoftGraphException e) {
            log.warn("Failed to start Microsoft Graph OAuth: {}", e.getMessage());
            return redirectToFrontend(false, e.getMessage());
        }
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<Void> oauthCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String providerError,
            @RequestParam(value = "error_description", required = false) String providerErrorDescription
    ) {
        if (hasText(providerError) || hasText(providerErrorDescription)) {
            String message = hasText(providerErrorDescription) ? providerErrorDescription : providerError;
            return redirectToFrontend(microsoftGraphService.getReturnPathOrDefault(state), false, "Microsoft Graph OAuth callback failed: " + message);
        }

        if (!hasText(state)) {
            return redirectToFrontend(false, "Microsoft Graph OAuth callback failed: state is missing.");
        }

        try {
            GraphTokenResponse tokenResponse = microsoftGraphService.exchangeCodeForToken(code);
            String returnPath = microsoftGraphService.storeConnection(state, tokenResponse);
            return redirectToFrontend(returnPath, true, null);
        } catch (MicrosoftGraphException e) {
            log.warn("Microsoft Graph OAuth callback failed: {}", e.getMessage());
            return redirectToFrontend(microsoftGraphService.getReturnPathOrDefault(state), false, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Microsoft Graph OAuth callback error", e);
            return redirectToFrontend(microsoftGraphService.getReturnPathOrDefault(state), false, "Microsoft Graph OAuth callback failed.");
        }
    }

    @GetMapping("/files/{itemId}/content")
    public ResponseEntity<byte[]> downloadExcelFile(@PathVariable String itemId) {
        try {
            MicrosoftGraphService.DownloadedGraphFile file = microsoftGraphService.downloadExcelFile(itemId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.contentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(file.fileName())
                            .build()
                            .toString())
                    .body(file.content());
        } catch (MicrosoftGraphException e) {
            log.warn("Microsoft Graph file download failed: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("\"" + e.getMessage().replace("\"", "\\\"") + "\"").getBytes());
        }
    }

    @GetMapping("/files")
    public ResponseEntity<ApiResponse<List<GraphFileDto>>> listExcelFiles() {
        try {
            List<GraphFileDto> files = microsoftGraphService.listExcelFiles();
            return ResponseEntity.ok(ApiResponse.ok("Excel files retrieved from OneDrive", files));
        } catch (MicrosoftGraphException e) {
            return graphError(e);
        }
    }

    @PostMapping("/cdw/generate-from-graph")
    public ResponseEntity<ApiResponse<GenerateCdwReportResponse>> generateFromGraph(
            @Valid @RequestBody GenerateCdwFromGraphRequest request
    ) {
        try {
            GenerateCdwReportResponse response = microsoftGraphService.generateCdwFromGraphFile(
                    request.getItemId(),
                    request.getMissionId(),
                    request.getFormat()
            );
            return ResponseEntity.ok(ApiResponse.ok("CDW report generated successfully from Microsoft Graph file", response));
        } catch (MicrosoftGraphException e) {
            return graphError(e);
        }
    }

    private <T> ResponseEntity<ApiResponse<T>> graphError(MicrosoftGraphException e) {
        log.warn("Microsoft Graph request failed: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.error(e.getMessage()));
    }

    private ResponseEntity<Void> redirectToFrontend(boolean connected, String errorMessage) {
        return redirectToFrontend("/cdw-generation", connected, errorMessage);
    }

    private ResponseEntity<Void> redirectToFrontend(String returnPath, boolean connected, String errorMessage) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(normalizedFrontendUrl() + sanitizeReturnPath(returnPath))
                .queryParam("graphConnected", connected);

        if (hasText(errorMessage)) {
            builder.queryParam("graphError", errorMessage);
        }

        return redirectTo(builder.build().encode().toUriString());
    }

    private ResponseEntity<Void> redirectTo(String url) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    private String normalizedFrontendUrl() {
        return frontendUrl.replaceAll("/+$", "");
    }

    private String sanitizeReturnPath(String returnPath) {
        if (!hasText(returnPath) || !returnPath.startsWith("/") || returnPath.startsWith("//")) {
            return "/cdw-generation";
        }
        return returnPath;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
