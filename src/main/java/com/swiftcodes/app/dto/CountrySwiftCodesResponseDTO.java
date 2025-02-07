package com.swiftcodes.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CountrySwiftCodesResponseDTO {
    @NotNull(message = "Country ISO2 code is required")
    @Size(min = 2, max = 2, message = "Country ISO2 code must be exactly 2 characters")
    private String countryISO2;

    @NotNull(message = "Country name is required")
    @Size(min = 1, max = 255, message = "Country name must be between 1 and 255 characters")
    private String countryName;


    private List<SwiftCodeResponseDTO> swiftCodes;
}
