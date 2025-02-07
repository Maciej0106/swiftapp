package com.swiftcodes.app.service;

import com.swiftcodes.app.dto.SwiftCodeDTO;
import com.swiftcodes.app.dto.SwiftCodeFileResponseDTO;
import com.swiftcodes.app.exception.InvalidDataException;
import com.swiftcodes.app.mapper.SwiftCodeMapper;
import com.swiftcodes.app.model.Country;
import com.swiftcodes.app.model.SwiftCode;
import com.swiftcodes.app.repository.CountryRepository;
import com.swiftcodes.app.repository.SwiftCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwiftCodeFileServiceImpl implements SwiftCodeFileService {

    private final SwiftCodeRepository swiftCodeRepository;
    private final SwiftCodeMapper swiftCodeMapper;
    private final CountryRepository countryRepository;

    @Override
    public SwiftCodeFileResponseDTO parseAndSaveSwiftCodes(MultipartFile file) {
        try {
            return parseAndSaveSwiftCodes(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            log.error("Error processing uploaded CSV file", e);
            throw new InvalidDataException("Could not process uploaded CSV file.");
        }
    }

    @Override
    public SwiftCodeFileResponseDTO parseAndSaveSwiftCodes(InputStream inputStream, String originalFilename) {
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new InvalidDataException("Invalid file format. Please upload a CSV file.");
        }

        List<SwiftCodeDTO> headquarters = new ArrayList<>();
        List<SwiftCodeDTO> branches = new ArrayList<>();
        Set<String> uniqueCountries = new HashSet<>();
        int totalRecords = 0;
        int skippedRecords = 0;

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            // Walidacja wymaganych nagłówków
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            List<String> requiredHeaders = Arrays.asList("COUNTRY ISO2 CODE", "SWIFT CODE", "NAME", "ADDRESS", "COUNTRY NAME");
            for (String header : requiredHeaders) {
                if (!headerMap.containsKey(header)) {
                    throw new InvalidDataException("CSV file is missing required column: " + header);
                }
            }

            for (CSVRecord csvRecord : csvParser) {
                totalRecords++;

                String swiftCode = csvRecord.get("SWIFT CODE").trim();
                String bankName = csvRecord.get("NAME").trim();
                String address = csvRecord.get("ADDRESS").trim();
                String countryISO2 = csvRecord.get("COUNTRY ISO2 CODE").trim().toUpperCase();
                String countryName = csvRecord.get("COUNTRY NAME").trim();

                if (swiftCode.length() != 11) {
                    log.warn("Skipping record {}: SWIFT code '{}' is invalid.", totalRecords, swiftCode);
                    skippedRecords++;
                    continue;
                }

                SwiftCodeDTO swiftCodeDTO = SwiftCodeDTO.builder()
                        .swiftCode(swiftCode)
                        .bankName(bankName.toUpperCase())
                        .address(address.toUpperCase())
                        .countryISO2(countryISO2)
                        .countryName(countryName.toUpperCase())
                        .isHeadquarter(swiftCode.endsWith("XXX"))
                        .build();

                uniqueCountries.add(countryISO2);

                if (swiftCodeDTO.getIsHeadquarter()) {
                    headquarters.add(swiftCodeDTO);
                } else {
                    branches.add(swiftCodeDTO);
                }
            }

            List<Country> countriesToSave = uniqueCountries.stream()
                    .filter(iso -> !countryRepository.existsById(iso))
                    .map(iso -> new Country(iso, iso))
                    .collect(Collectors.toList());
            countryRepository.saveAll(countriesToSave);
            log.info("Saved {} new countries to database.", countriesToSave.size());

            List<SwiftCode> headquartersEntities = headquarters.stream()
                    .map(swiftCodeMapper::toEntity)
                    .collect(Collectors.toList());
            swiftCodeRepository.saveAll(headquartersEntities);
            log.info("Saved {} headquarters to database.", headquartersEntities.size());

            Set<String> existingHeadquarters = swiftCodeRepository.findAllByIsHeadquarterTrue().stream()
                    .map(sc -> calculateBaseSwiftCode(sc.getSwiftCode()))
                    .collect(Collectors.toSet());

            List<SwiftCode> branchesToSave = new ArrayList<>();
            for (SwiftCodeDTO branch : branches) {
                String baseSwiftCode = calculateBaseSwiftCode(branch.getSwiftCode());
                if (existingHeadquarters.contains(baseSwiftCode)) {
                    branch.setBaseSwiftCode(baseSwiftCode);
                    branchesToSave.add(swiftCodeMapper.toEntity(branch));
                } else {
                    log.warn("Skipping branch {}: No matching headquarters found. Expected base_swift_code: {}", branch.getSwiftCode(), baseSwiftCode);
                    skippedRecords++;
                }
            }

            swiftCodeRepository.saveAll(branchesToSave);
            log.info("Saved {} branch offices to database.", branchesToSave.size());

            return new SwiftCodeFileResponseDTO(
                    totalRecords,
                    skippedRecords,
                    countriesToSave.size(),
                    headquartersEntities.size(),
                    branchesToSave.size()
            );

        } catch (IOException e) {
            log.error("Error parsing CSV file", e);
            throw new InvalidDataException("Invalid CSV file format or corrupted file.");
        }
    }

    private String calculateBaseSwiftCode(String swiftCode) {
        if (swiftCode == null || swiftCode.length() < 8) {
            throw new InvalidDataException("Invalid SWIFT code provided for base calculation.");
        }
        return swiftCode.substring(0, 8);
    }
}
