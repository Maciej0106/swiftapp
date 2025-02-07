package com.swiftcodes.app.service;

import com.swiftcodes.app.dto.CountrySwiftCodesResponseDTO;
import com.swiftcodes.app.dto.SwiftCodeRequestDTO;
import com.swiftcodes.app.dto.SwiftCodeResponseDTO;
import com.swiftcodes.app.exception.DatabaseException;
import com.swiftcodes.app.exception.DuplicateEntryException;
import com.swiftcodes.app.exception.InvalidDataException;
import com.swiftcodes.app.exception.ResourceNotFoundException;
import com.swiftcodes.app.mapper.SwiftCodeMapper;
import com.swiftcodes.app.model.Country;
import com.swiftcodes.app.model.SwiftCode;
import com.swiftcodes.app.repository.CountryRepository;
import com.swiftcodes.app.repository.SwiftCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SwiftCodeServiceImpl implements SwiftCodeService {

    private final SwiftCodeRepository swiftCodeRepository;
    private final SwiftCodeMapper swiftCodeMapper;
    private final CountryRepository countryRepository;

    @Override
    @Transactional(readOnly = true)
    public SwiftCodeResponseDTO getSwiftCode(String swiftCode) {
        log.info("Fetching SWIFT code: {}", swiftCode);

        SwiftCode code = swiftCodeRepository.findById(swiftCode)
                .orElseThrow(() -> new ResourceNotFoundException("SWIFT code not found: " + swiftCode));

        SwiftCodeResponseDTO dto = swiftCodeMapper.toResponseDTO(code);
        dto.setCountryISO2(dto.getCountryISO2().toUpperCase());

        countryRepository.findById(dto.getCountryISO2())
                .ifPresent(country -> dto.setCountryName(country.getName().toUpperCase()));

        if (Boolean.TRUE.equals(code.getIsHeadquarter())) {
            List<SwiftCode> branchEntities = swiftCodeRepository.findBranchesByHeadquarter(calculateBaseSwiftCode(code.getSwiftCode()));
            List<SwiftCodeResponseDTO> branches = branchEntities.stream()
                    .filter(branch -> !branch.getSwiftCode().equals(swiftCode))
                    .map(swiftCodeMapper::toResponseDTO)
                    .peek(branchDTO -> {
                        branchDTO.setCountryISO2(branchDTO.getCountryISO2().toUpperCase());
                        branchDTO.setCountryName(null);
                    })
                    .collect(Collectors.toList());
            dto.setBranches(branches);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public CountrySwiftCodesResponseDTO getSwiftCodesByCountry(String countryIso2Code, Pageable pageable) {
        log.info("Fetching SWIFT codes for country: {} with pagination", countryIso2Code);

        if (!countryIso2Code.matches("^[A-Z]{2}$")) {
            log.warn("Invalid country ISO2 format: {}", countryIso2Code);
            throw new InvalidDataException("Country ISO2 code must be exactly 2 uppercase letters.");
        }

        List<SwiftCode> codes = swiftCodeRepository.findByCountryIso2Code(countryIso2Code.toUpperCase(), pageable);

        if (codes.isEmpty()) {
            log.warn("No SWIFT codes found for country: {}", countryIso2Code);
            throw new ResourceNotFoundException("No SWIFT codes found for country: " + countryIso2Code);
        }

        List<SwiftCodeResponseDTO> dtoList = codes.stream()
                .map(swiftCodeMapper::toResponseDTO)
                .peek(dto -> {
                    dto.setSwiftCode(dto.getSwiftCode().toUpperCase());
                    dto.setBankName(dto.getBankName().toUpperCase());
                    dto.setAddress(dto.getAddress().toUpperCase());
                    dto.setCountryISO2(dto.getCountryISO2().toUpperCase());
                    dto.setCountryName(null);
                    dto.setBranches(Collections.emptyList());
                })
                .collect(Collectors.toList());

        Country country = countryRepository.findById(countryIso2Code.toUpperCase())
                .orElse(new Country(
                        countryIso2Code.toUpperCase(),
                        "UNKNOWN"
                ));

        CountrySwiftCodesResponseDTO response = new CountrySwiftCodesResponseDTO();
        response.setCountryISO2(countryIso2Code.toUpperCase());
        response.setCountryName(country.getName().toUpperCase());
        response.setSwiftCodes(dtoList);

        return response;
    }

    @Override
    @Retryable(value = { Exception.class }, maxAttempts = 3)
    public SwiftCodeResponseDTO addSwiftCode(SwiftCodeRequestDTO swiftCodeRequestDTO) {
        log.info("Attempting to add new SWIFT code: {}", swiftCodeRequestDTO.getSwiftCode());

        if (swiftCodeRepository.existsById(swiftCodeRequestDTO.getSwiftCode())) {
            throw new DuplicateEntryException("SWIFT code already exists: " + swiftCodeRequestDTO.getSwiftCode());
        }

        swiftCodeRequestDTO.setCountryISO2(swiftCodeRequestDTO.getCountryISO2().toUpperCase());
        swiftCodeRequestDTO.setBankName(swiftCodeRequestDTO.getBankName().toUpperCase());
        swiftCodeRequestDTO.setAddress(swiftCodeRequestDTO.getAddress().toUpperCase());

        String countryName = Optional.ofNullable(swiftCodeRequestDTO.getCountryName())
                .map(String::toUpperCase)
                .orElseGet(() -> countryRepository.findById(swiftCodeRequestDTO.getCountryISO2())
                        .map(country -> country.getName().toUpperCase())
                        .orElse("UNKNOWN"));

        String baseSwiftCode = calculateBaseSwiftCode(swiftCodeRequestDTO.getSwiftCode());
        if (Boolean.FALSE.equals(swiftCodeRequestDTO.getIsHeadquarter())) {
            boolean headquarterExists = swiftCodeRepository.findAllByIsHeadquarterTrue().stream()
                    .anyMatch(hq -> calculateBaseSwiftCode(hq.getSwiftCode()).equals(baseSwiftCode));
            if (!headquarterExists) {
                throw new InvalidDataException("Branch SWIFT code must have a matching headquarters.");
            }
        }

        countryRepository.findById(swiftCodeRequestDTO.getCountryISO2()).orElseGet(() -> {
            Country newCountry = new Country(swiftCodeRequestDTO.getCountryISO2(), swiftCodeRequestDTO.getCountryName().toUpperCase());
            countryRepository.save(newCountry);
            return newCountry;
        });

        SwiftCode swiftCodeEntity = swiftCodeMapper.toEntity(swiftCodeRequestDTO);
        swiftCodeEntity.setBaseSwiftCode(baseSwiftCode);
        swiftCodeRepository.save(swiftCodeEntity);

        log.info("SWIFT code {} added successfully", swiftCodeEntity.getSwiftCode());
        return swiftCodeMapper.toResponseDTO(swiftCodeEntity);
    }

    @Override
    public SwiftCodeResponseDTO deleteSwiftCode(String swiftCode) {
        log.info("Attempting to delete SWIFT code: {}", swiftCode);

        SwiftCode code = swiftCodeRepository.findById(swiftCode)
                .orElseThrow(() -> {
                    log.warn("Attempted to delete non-existent SWIFT code: {}", swiftCode);
                    return new ResourceNotFoundException("SWIFT code not found: " + swiftCode);
                });

        try {
            SwiftCodeResponseDTO deletedCodeDTO = swiftCodeMapper.toResponseDTO(code);
            swiftCodeRepository.delete(code);
            log.info("SWIFT code {} deleted successfully", swiftCode);
            return deletedCodeDTO;
        } catch (Exception e) {
            log.error("Error deleting SWIFT code: {}", swiftCode, e);
            throw new DatabaseException("Failed to delete SWIFT code due to database error.", e);
        }
    }


    private String calculateBaseSwiftCode(String swiftCode) {
        if (swiftCode == null || swiftCode.length() < 8) {
            throw new InvalidDataException("Invalid SWIFT code provided for base calculation.");
        }
        return swiftCode.substring(0, 8);
    }
}
