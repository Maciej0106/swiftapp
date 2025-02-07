package com.swiftcodes.app.service;

import com.swiftcodes.app.dto.CountrySwiftCodesResponseDTO;
import com.swiftcodes.app.dto.SwiftCodeRequestDTO;
import com.swiftcodes.app.dto.SwiftCodeResponseDTO;
import org.springframework.data.domain.Pageable;

public interface SwiftCodeService {

    SwiftCodeResponseDTO getSwiftCode(String swiftCode);

    CountrySwiftCodesResponseDTO getSwiftCodesByCountry(String countryIso2Code, Pageable pageable);

    SwiftCodeResponseDTO addSwiftCode(SwiftCodeRequestDTO swiftCodeRequestDTO);

    SwiftCodeResponseDTO deleteSwiftCode(String swiftCode);
}
