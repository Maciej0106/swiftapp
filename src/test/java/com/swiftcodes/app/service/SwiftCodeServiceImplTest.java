package com.swiftcodes.app.service;

import com.swiftcodes.app.dto.SwiftCodeRequestDTO;
import com.swiftcodes.app.dto.SwiftCodeResponseDTO;
import com.swiftcodes.app.exception.DuplicateEntryException;
import com.swiftcodes.app.exception.ResourceNotFoundException;
import com.swiftcodes.app.mapper.SwiftCodeMapper;
import com.swiftcodes.app.model.Country;
import com.swiftcodes.app.model.SwiftCode;
import com.swiftcodes.app.repository.CountryRepository;
import com.swiftcodes.app.repository.SwiftCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SwiftCodeServiceImplTest {

    @Mock
    private SwiftCodeRepository swiftCodeRepository;

    @Mock
    private SwiftCodeMapper swiftCodeMapper;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private SwiftCodeServiceImpl swiftCodeService;

    private SwiftCode headquarterEntity;
    private SwiftCodeRequestDTO headquarterRequestDTO;

    @BeforeEach
    public void setup() {
        headquarterEntity = new SwiftCode();
        headquarterEntity.setSwiftCode("ABCDEFGHXXX");
        headquarterEntity.setBankName("BANK TEST");
        headquarterEntity.setAddress("ADRES TESTOWY");
        headquarterEntity.setCountryIso2Code("PL");
        headquarterEntity.setIsHeadquarter(true);
        headquarterEntity.setBaseSwiftCode("ABCDEFGHXXX");

        headquarterRequestDTO = SwiftCodeRequestDTO.builder()
                .swiftCode("ABCDEFGHXXX")
                .bankName("BANK TEST")
                .address("ADRES TESTOWY")
                .countryISO2("PL")
                .countryName("POLSKA")
                .isHeadquarter(true)
                .build();
    }

    @Test
    public void getSwiftCode_validHeadquarter_returnsDTOWithBranches() {

        when(swiftCodeRepository.findById("ABCDEFGHXXX")).thenReturn(Optional.of(headquarterEntity));
        when(swiftCodeMapper.toResponseDTO(headquarterEntity)).thenReturn(
                headquarterRequestToResponse(headquarterRequestDTO)
        );


        SwiftCode branchEntity = new SwiftCode();
        branchEntity.setSwiftCode("ABCDEFGH111");
        branchEntity.setBankName("BANK TEST");
        branchEntity.setAddress("ADRES ODDZIAŁU");
        branchEntity.setCountryIso2Code("PL");
        branchEntity.setIsHeadquarter(false);
        branchEntity.setBaseSwiftCode("ABCDEFGHXXX");

        SwiftCodeRequestDTO branchRequestDTO = SwiftCodeRequestDTO.builder()
                .swiftCode("ABCDEFGH111")
                .bankName("BANK TEST")
                .address("ADRES ODDZIAŁU")
                .countryISO2("PL")
                .countryName("POLSKA")
                .isHeadquarter(false)
                .build();

        when(swiftCodeRepository.findBranchesByHeadquarter("ABCDEFGH")).thenReturn(Arrays.asList(branchEntity));
        when(swiftCodeMapper.toResponseDTO(branchEntity)).thenReturn(
                branchRequestToResponse(branchRequestDTO)
        );

        SwiftCodeResponseDTO result = swiftCodeService.getSwiftCode("ABCDEFGHXXX");

        assertThat(result).isNotNull();
        assertThat(result.getSwiftCode()).isEqualTo("ABCDEFGHXXX");
        assertThat(result.getBranches()).hasSize(1);
        assertThat(result.getBranches().get(0).getSwiftCode()).isEqualTo("ABCDEFGH111");
    }

    @Test
    public void addSwiftCode_duplicateEntry_throwsDuplicateEntryException() {
        when(swiftCodeRepository.existsById(headquarterRequestDTO.getSwiftCode())).thenReturn(true);

        assertThatThrownBy(() -> swiftCodeService.addSwiftCode(headquarterRequestDTO))
                .isInstanceOf(DuplicateEntryException.class)
                .hasMessageContaining("SWIFT code already exists");
    }

    @Test
    public void addSwiftCode_lowerCaseCountry_convertsToUpperCase() {
        SwiftCodeRequestDTO dto = SwiftCodeRequestDTO.builder()
                .swiftCode("IJKLMNOPXXX")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("us")  // podajemy małymi literami
                .countryName("United States")
                .isHeadquarter(true)
                .build();

        when(swiftCodeRepository.existsById(dto.getSwiftCode())).thenReturn(false);
        when(countryRepository.existsById("US")).thenReturn(true);

        SwiftCode newEntity = new SwiftCode();
        newEntity.setSwiftCode("IJKLMNOPXXX");
        newEntity.setBankName("TEST BANK");
        newEntity.setAddress("TEST ADDRESS");
        newEntity.setCountryIso2Code("US");
        newEntity.setIsHeadquarter(true);
        newEntity.setBaseSwiftCode("IJKLMNOPXXX");

        when(swiftCodeMapper.toEntity(dto)).thenReturn(newEntity);
        when(swiftCodeRepository.save(newEntity)).thenReturn(newEntity);
        when(swiftCodeMapper.toResponseDTO(newEntity)).thenReturn(dtoToResponse(dto));

        SwiftCodeResponseDTO result = swiftCodeService.addSwiftCode(dto);


        assertThat(result).isNotNull();
        assertThat(result.getCountryISO2()).isEqualTo("US");
    }

    @Test
    public void addSwiftCode_successfulAdd_returnsSwiftCodeDTO() {
        SwiftCodeRequestDTO dto = SwiftCodeRequestDTO.builder()
                .swiftCode("IJKLMNOPXXX")
                .bankName("New Bank")
                .address("New Address")
                .countryISO2("US")
                .countryName("United States")
                .isHeadquarter(true)
                .build();

        when(swiftCodeRepository.existsById(dto.getSwiftCode())).thenReturn(false);
        when(countryRepository.existsById("US")).thenReturn(false);

        SwiftCode newEntity = new SwiftCode();
        newEntity.setSwiftCode("IJKLMNOPXXX");
        newEntity.setBankName("NEW BANK");
        newEntity.setAddress("NEW ADDRESS");
        newEntity.setCountryIso2Code("US");
        newEntity.setIsHeadquarter(true);
        newEntity.setBaseSwiftCode("IJKLMNOPXXX");

        when(swiftCodeMapper.toEntity(dto)).thenReturn(newEntity);
        when(swiftCodeRepository.save(newEntity)).thenReturn(newEntity);
        when(swiftCodeMapper.toResponseDTO(newEntity)).thenReturn(dtoToResponse(dto));

        SwiftCodeResponseDTO result = swiftCodeService.addSwiftCode(dto);

        assertThat(result).isNotNull();
        assertThat(result.getSwiftCode()).isEqualTo("IJKLMNOPXXX");
        assertThat(result.getBankName()).isEqualTo("New Bank");

        verify(countryRepository, times(1)).save(any(Country.class));
    }

    @Test
    public void deleteSwiftCode_validCode_returnsDeletedSwiftCodeDTO() {
        when(swiftCodeRepository.findById("ABCDEFGHXXX")).thenReturn(Optional.of(headquarterEntity));
        doNothing().when(swiftCodeRepository).delete(headquarterEntity);
        when(swiftCodeMapper.toResponseDTO(headquarterEntity)).thenReturn(dtoToResponse(headquarterRequestDTO));

        SwiftCodeResponseDTO result = swiftCodeService.deleteSwiftCode("ABCDEFGHXXX");

        assertThat(result).isNotNull();
        assertThat(result.getSwiftCode()).isEqualTo("ABCDEFGHXXX");
        assertThat(result.getBankName()).isEqualTo("BANK TEST");

        verify(swiftCodeRepository, times(1)).delete(headquarterEntity);
    }

    @Test
    public void deleteSwiftCode_notFound_throwsResourceNotFoundException() {
        when(swiftCodeRepository.findById("ABCDEFGHXXX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> swiftCodeService.deleteSwiftCode("ABCDEFGHXXX"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SWIFT code not found");
    }


    private SwiftCodeResponseDTO headquarterRequestToResponse(SwiftCodeRequestDTO dto) {
        SwiftCodeResponseDTO response = new SwiftCodeResponseDTO();
        response.setSwiftCode(dto.getSwiftCode());
        response.setBankName(dto.getBankName());
        response.setAddress(dto.getAddress());
        response.setCountryISO2(dto.getCountryISO2());
        response.setCountryName(dto.getCountryName());
        response.setIsHeadquarter(dto.getIsHeadquarter());
        response.setBranches(Collections.emptyList());
        return response;
    }

    private SwiftCodeResponseDTO branchRequestToResponse(SwiftCodeRequestDTO dto) {
        SwiftCodeResponseDTO response = new SwiftCodeResponseDTO();
        response.setSwiftCode(dto.getSwiftCode());
        response.setBankName(dto.getBankName());
        response.setAddress(dto.getAddress());
        response.setCountryISO2(dto.getCountryISO2());

        response.setCountryName(null);
        response.setIsHeadquarter(dto.getIsHeadquarter());
        response.setBranches(Collections.emptyList());
        return response;
    }

    private SwiftCodeResponseDTO dtoToResponse(SwiftCodeRequestDTO dto) {
        SwiftCodeResponseDTO response = new SwiftCodeResponseDTO();
        response.setSwiftCode(dto.getSwiftCode());
        response.setBankName(dto.getBankName());
        response.setAddress(dto.getAddress());
        response.setCountryISO2(dto.getCountryISO2());
        response.setCountryName(dto.getCountryName());
        response.setIsHeadquarter(dto.getIsHeadquarter());
        response.setBranches(Collections.emptyList());
        return response;
    }
}
