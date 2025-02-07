package com.swiftcodes.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "address", "bankName", "countryISO2", "isHeadquarter", "swiftCode" })
public class SwiftCodeDTO {

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

    private String countryName;

    private Boolean isHeadquarter;

    private List<SwiftCodeDTO> branches = new ArrayList<>();

    @JsonIgnore
    private String baseSwiftCode;
}
