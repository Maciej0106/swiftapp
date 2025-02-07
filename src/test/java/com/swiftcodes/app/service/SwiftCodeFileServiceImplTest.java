package com.swiftcodes.app.service;

import com.swiftcodes.app.dto.SwiftCodeDTO;
import com.swiftcodes.app.dto.SwiftCodeFileResponseDTO;
import com.swiftcodes.app.exception.InvalidDataException;
import com.swiftcodes.app.mapper.SwiftCodeMapper;
import com.swiftcodes.app.model.SwiftCode;
import com.swiftcodes.app.repository.CountryRepository;
import com.swiftcodes.app.repository.SwiftCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SwiftCodeFileServiceImplTest {

    @Mock
    private SwiftCodeRepository swiftCodeRepository;

    @Mock
    private SwiftCodeMapper swiftCodeMapper;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private SwiftCodeFileServiceImpl fileService;

    @BeforeEach
    public void setup() {
        when(countryRepository.existsById("PL")).thenReturn(true);
    }

    @Test
    public void parseAndSaveSwiftCodes_validCSV_returnsSuccessMessage() throws Exception {
        String csvContent = "COUNTRY ISO2 CODE,SWIFT CODE,NAME,ADDRESS,COUNTRY NAME\n" +
                "PL,ABCDEFGHXXX,Bank Test,Adres Testowy,Polska\n" +
                "PL,ABCDEFGH111,Bank Branch,Adres Oddziału,Polska\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));


        when(swiftCodeRepository.existsById("ABCDEFGHXXX")).thenReturn(true);
        when(swiftCodeMapper.toEntity(any(SwiftCodeDTO.class))).thenReturn(new SwiftCode());

        SwiftCode headquarter = new SwiftCode();
        headquarter.setSwiftCode("ABCDEFGHXXX");
        when(swiftCodeRepository.findAllByIsHeadquarterTrue()).thenReturn(Collections.singletonList(headquarter));
        when(swiftCodeRepository.saveAll(any())).thenReturn(null);

        SwiftCodeFileResponseDTO response = fileService.parseAndSaveSwiftCodes(file);


        assertThat(response.getProcessedRecords()).isEqualTo(2);
        assertThat(response.getSkippedRecords()).isEqualTo(0);
        assertThat(response.getHeadquartersSaved()).isEqualTo(1);
        assertThat(response.getBranchesSaved()).isEqualTo(1);
    }

    @Test
    public void parseAndSaveSwiftCodes_mixedRecords_processesOnlyValid() throws Exception {
        String csvContent = "COUNTRY ISO2 CODE,SWIFT CODE,NAME,ADDRESS,COUNTRY NAME\n" +
                "PL,ABCDEFGHXXX,Bank Test,Adres Testowy,Polska\n" +
                "PL,SHORT,Bank Invalid,Adres,Polska\n" +
                "xx,ABCDEFGHIJK,Bank Wrong Country,Adres,Polska\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));


        when(swiftCodeRepository.existsById("ABCDEFGHXXX")).thenReturn(true);
        when(swiftCodeMapper.toEntity(any(SwiftCodeDTO.class))).thenReturn(new SwiftCode());

        SwiftCode headquarter = new SwiftCode();
        headquarter.setSwiftCode("ABCDEFGHXXX");
        when(swiftCodeRepository.findAllByIsHeadquarterTrue()).thenReturn(Collections.singletonList(headquarter));
        when(swiftCodeRepository.saveAll(any())).thenReturn(null);

        SwiftCodeFileResponseDTO response = fileService.parseAndSaveSwiftCodes(file);


        assertThat(response.getProcessedRecords()).isEqualTo(3);
        assertThat(response.getSkippedRecords()).isEqualTo(1);
        assertThat(response.getHeadquartersSaved()).isEqualTo(1);
        assertThat(response.getBranchesSaved()).isEqualTo(1);
    }

    @Test
    public void parseAndSaveSwiftCodes_invalidExtension_throwsInvalidDataException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt",
                "text/plain", "dummy content".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> fileService.parseAndSaveSwiftCodes(file))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Invalid file format");
    }

    @Test
    public void parseAndSaveSwiftCodes_missingRequiredHeaders_throwsInvalidDataException() {
        String csvContent = "COUNTRY ISO2 CODE,SWIFT CODE,NAME\n" +
                "PL,ABCDEFGHXXX,Bank Test\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> fileService.parseAndSaveSwiftCodes(file))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("CSV file is missing required column");
    }
}
