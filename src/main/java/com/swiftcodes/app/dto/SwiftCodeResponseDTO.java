package com.swiftcodes.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "swiftCode", "bankName", "address", "countryISO2", "countryName", "isHeadquarter", "branches" })
public class SwiftCodeResponseDTO {
    private String swiftCode;
    private String bankName;
    private String address;
    private String countryISO2;
    private String countryName;
    private Boolean isHeadquarter;
    private List<SwiftCodeResponseDTO> branches = new ArrayList<>();
}
