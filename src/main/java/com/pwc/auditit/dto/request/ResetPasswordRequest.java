package com.pwc.auditit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^\\d{6}$", message = "Code should be 6 digits")
    private String code;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    private String newPassword;
}

