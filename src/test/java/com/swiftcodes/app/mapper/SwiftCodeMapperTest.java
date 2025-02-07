package com.swiftcodes.app.mapper;

import com.swiftcodes.app.dto.SwiftCodeDTO;
import com.swiftcodes.app.dto.SwiftCodeResponseDTO;
import com.swiftcodes.app.model.SwiftCode;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.*;

public class SwiftCodeMapperTest {

    private final SwiftCodeMapper mapper = Mappers.getMapper(SwiftCodeMapper.class);

    @Test
    public void whenMappingDtoToEntity_thenAllFieldsAreMapped() {
        SwiftCodeDTO dto = SwiftCodeDTO.builder()
                .swiftCode("ABCDEFGHXXX")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .countryName("Polska")
                .isHeadquarter(true)
                .build();

        SwiftCode entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getSwiftCode()).isEqualTo(dto.getSwiftCode());
        assertThat(entity.getBankName()).isEqualTo(dto.getBankName());
        assertThat(entity.getAddress()).isEqualTo(dto.getAddress());
        assertThat(entity.getCountryIso2Code()).isEqualTo(dto.getCountryISO2());

        assertThat(entity.getBaseSwiftCode()).isNull();
    }

    @Test
    public void whenMappingEntityToDto_thenAllFieldsAreMapped() {
        SwiftCode entity = new SwiftCode();
        entity.setSwiftCode("ABCDEFGHXXX");
        entity.setBankName("Test Bank");
        entity.setAddress("Test Address");
        entity.setCountryIso2Code("PL");
        entity.setIsHeadquarter(true);
        entity.setBaseSwiftCode("ABCDEFGHXXX");

        SwiftCodeResponseDTO dto = mapper.toResponseDTO(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getSwiftCode()).isEqualTo(entity.getSwiftCode());
        assertThat(dto.getBankName()).isEqualTo(entity.getBankName());
        assertThat(dto.getAddress()).isEqualTo(entity.getAddress());
        assertThat(dto.getCountryISO2()).isEqualTo(entity.getCountryIso2Code());

    }
}
