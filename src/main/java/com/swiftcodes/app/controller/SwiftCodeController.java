package com.swiftcodes.app.controller;

import com.swiftcodes.app.dto.CountrySwiftCodesResponseDTO;
import com.swiftcodes.app.dto.ResponseMessageDTO;
import com.swiftcodes.app.dto.SwiftCodeRequestDTO;
import com.swiftcodes.app.dto.SwiftCodeResponseDTO;
import com.swiftcodes.app.dto.SwiftCodeFileResponseDTO;
import com.swiftcodes.app.service.SwiftCodeFileService;
import com.swiftcodes.app.service.SwiftCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/swift-codes")
@RequiredArgsConstructor
public class SwiftCodeController {

    private final SwiftCodeService swiftCodeService;
    private final SwiftCodeFileService swiftCodeFileService;


    @GetMapping(value = "/{swiftCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SwiftCodeResponseDTO> getSwiftCode(@PathVariable String swiftCode) {
        log.info("Received request to get details for SWIFT code: {}", swiftCode);
        SwiftCodeResponseDTO dto = swiftCodeService.getSwiftCode(swiftCode);
        return ResponseEntity.ok(dto);
    }


    @GetMapping(value = "/country/{countryISO2}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CountrySwiftCodesResponseDTO> getSwiftCodesByCountry(
            @PathVariable String countryISO2,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get SWIFT codes for country: {}, page: {}, size: {}", countryISO2, page, size);
        Pageable pageable = PageRequest.of(page, size);
        CountrySwiftCodesResponseDTO dto = swiftCodeService.getSwiftCodesByCountry(countryISO2, pageable);
        return ResponseEntity.ok(dto);
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessageDTO> addSwiftCode(@Valid @RequestBody SwiftCodeRequestDTO swiftCodeRequestDTO) {
        log.info("Received request to add new SWIFT code: {}", swiftCodeRequestDTO.getSwiftCode());
        swiftCodeService.addSwiftCode(swiftCodeRequestDTO);
        return ResponseEntity.ok(new ResponseMessageDTO("SWIFT code added successfully"));
    }


    @DeleteMapping(value = "/{swiftCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessageDTO> deleteSwiftCode(@PathVariable String swiftCode) {
        log.info("Received request to delete SWIFT code: {}", swiftCode);
        swiftCodeService.deleteSwiftCode(swiftCode);
        return ResponseEntity.ok(new ResponseMessageDTO("SWIFT code deleted successfully"));
    }


    @PostMapping(value = "/upload-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SwiftCodeFileResponseDTO> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        log.info("Received request to upload a CSV file");
        SwiftCodeFileResponseDTO responseDTO = swiftCodeFileService.parseAndSaveSwiftCodes(file);
        return ResponseEntity.ok(responseDTO);
    }
}
