package com.swiftcodes.app.validation;

import com.swiftcodes.app.dto.SwiftCodeDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SwiftCodeDTOValidationTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void whenValidDto_thenNoViolations() {
        SwiftCodeDTO dto = SwiftCodeDTO.builder()
                .swiftCode("ABCDEFGHXXX")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .countryName("Polska")
                .isHeadquarter(true)
                .build();

        Set<ConstraintViolation<SwiftCodeDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    public void whenInvalidSwiftCodeLength_thenViolationOccurs() {
        SwiftCodeDTO dto = SwiftCodeDTO.builder()
                .swiftCode("SHORT")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .countryName("Polska")
                .isHeadquarter(true)
                .build();

        Set<ConstraintViolation<SwiftCodeDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("swiftCode") &&
                        v.getMessage().contains("exactly 11 characters"));
    }

    @Test
    public void whenCountryIso2Invalid_thenViolationOccurs() {
        SwiftCodeDTO dto = SwiftCodeDTO.builder()
                .swiftCode("ABCDEFGHXXX")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("pl")
                .countryName("Polska")
                .isHeadquarter(true)
                .build();

        Set<ConstraintViolation<SwiftCodeDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("countryISO2") &&
                        v.getMessage().contains("exactly 2 uppercase letters"));
    }
}
