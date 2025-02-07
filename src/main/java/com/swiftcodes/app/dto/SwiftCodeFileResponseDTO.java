package com.swiftcodes.app.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class SwiftCodeFileResponseDTO {

    private final Instant timestamp = Instant.now();
    private int processedRecords;
    private int skippedRecords;
    private int newCountries;
    private int headquartersSaved;
    private int branchesSaved;
}
