package com.swiftcodes.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftcodes.app.dto.SwiftCodeRequestDTO;
import com.swiftcodes.app.repository.CountryRepository;
import com.swiftcodes.app.repository.SwiftCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SwiftCodeControllerIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SwiftCodeRepository swiftCodeRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        swiftCodeRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    public void addSwiftCode_thenGetSwiftCode() throws Exception {
        SwiftCodeRequestDTO dto = SwiftCodeRequestDTO.builder()
                .swiftCode("ABCDEFGHXXX")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .countryName("Polska")
                .isHeadquarter(true)
                .build();


        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("SWIFT code added successfully")));


        mockMvc.perform(get("/v1/swift-codes/ABCDEFGHXXX")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode", is("ABCDEFGHXXX")))
                .andExpect(jsonPath("$.bankName", is("TEST BANK")))
                .andExpect(jsonPath("$.address", is("TEST ADDRESS")));
    }

    @Test
    public void addSwiftCode_validationError_returnsBadRequest() throws Exception {
        SwiftCodeRequestDTO invalidDto = SwiftCodeRequestDTO.builder()
                .swiftCode("SHORT")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .countryName("Polska")
                .isHeadquarter(true)
                .build();

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("exactly 11 characters")));
    }

    @Test
    public void getSwiftCode_notFound_returns404() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/NONEXISTENT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("SWIFT code not found")));
    }

    @Test
    public void deleteSwiftCode_returnsSuccessMessage() throws Exception {
        SwiftCodeRequestDTO dto = SwiftCodeRequestDTO.builder()
                .swiftCode("ABCDEFGHXXX")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .countryName("Polska")
                .isHeadquarter(true)
                .build();

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("SWIFT code added successfully")));


        mockMvc.perform(delete("/v1/swift-codes/ABCDEFGHXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("SWIFT code deleted successfully")));


        mockMvc.perform(get("/v1/swift-codes/ABCDEFGHXXX")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSwiftCodesByCountry_returnsCodes() throws Exception {
        SwiftCodeRequestDTO dto1 = SwiftCodeRequestDTO.builder()
                .swiftCode("ABCDEFGHXXX")
                .bankName("Bank One")
                .address("Adres One")
                .countryISO2("PL")
                .countryName("Polska")
                .isHeadquarter(true)
                .build();
        SwiftCodeRequestDTO dto2 = SwiftCodeRequestDTO.builder()
                .swiftCode("IJKLMNOPXXX")
                .bankName("Bank Two")
                .address("Adres Two")
                .countryISO2("PL")
                .countryName("Polska")
                .isHeadquarter(true)
                .build();

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("SWIFT code added successfully")));
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("SWIFT code added successfully")));

        mockMvc.perform(get("/v1/swift-codes/country/PL")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryISO2", is("PL")))
                .andExpect(jsonPath("$.swiftCodes", hasSize(2)));
    }

    @Test
    public void uploadCsvFile_validFile_processesSuccessfully() throws Exception {
        String csvContent = "COUNTRY ISO2 CODE,SWIFT CODE,NAME,ADDRESS,COUNTRY NAME\n" +
                "PL,ABCDEFGHXXX,Bank Test,Adres Testowy,Polska\n";
        byte[] content = csvContent.getBytes();

        mockMvc.perform(multipart("/v1/swift-codes/upload-csv")
                        .file("file", content)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedRecords", is(1)))
                .andExpect(jsonPath("$.skippedRecords", is(0)))
                .andExpect(jsonPath("$.headquartersSaved", is(1)))
                .andExpect(jsonPath("$.branchesSaved", is(0)));
    }

    @Test
    public void uploadCsvFile_invalidCSV_returnsBadRequest() throws Exception {
        byte[] content = "dummy content".getBytes();

        mockMvc.perform(multipart("/v1/swift-codes/upload-csv")
                        .file("file", content)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid CSV file format")));
    }
}
