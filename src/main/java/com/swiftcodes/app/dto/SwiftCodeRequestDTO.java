package com.swiftcodes.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwiftCodeRequestDTO {
    @NotNull(message = "Swift code is required")
    @Size(min = 11, max = 11, message = "Swift code must be exactly 11 characters long")
    private String swiftCode;

    @NotNull(message = "Bank name is required")
    @Size(min = 1, max = 255, message = "Bank name must be between 1 and 255 characters")
    private String bankName;

    @NotNull(message = "Address is required")
    @Size(min = 1, max = 255, message = "Address must be between 1 and 255 characters")
    private String address;

    @NotNull(message = "Country ISO2 code is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country ISO2 code must be exactly 2 uppercase letters")
    private String countryISO2;

    @NotBlank(message = "Country name is required")
    @Size(min = 1, max = 255, message = "Country name must be between 1 and 255 characters")
    private String countryName;

    @NotNull(message = "isHeadquarter flag is required")
    private Boolean isHeadquarter;
}
