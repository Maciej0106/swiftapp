package com.swiftcodes.app.mapper;

import com.swiftcodes.app.dto.SwiftCodeDTO;
import com.swiftcodes.app.dto.SwiftCodeRequestDTO;
import com.swiftcodes.app.dto.SwiftCodeResponseDTO;
import com.swiftcodes.app.model.SwiftCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SwiftCodeMapper {


    @Mapping(source = "countryISO2", target = "countryIso2Code")
    @Mapping(target = "baseSwiftCode", ignore = true)
    SwiftCode toEntity(SwiftCodeRequestDTO swiftCodeRequestDTO);


    @Mapping(source = "countryISO2", target = "countryIso2Code")
    @Mapping(target = "baseSwiftCode", ignore = true)
    SwiftCode toEntity(SwiftCodeDTO swiftCodeDTO);


    @Mapping(source = "countryIso2Code", target = "countryISO2")
    SwiftCodeResponseDTO toResponseDTO(SwiftCode swiftCode);
}
