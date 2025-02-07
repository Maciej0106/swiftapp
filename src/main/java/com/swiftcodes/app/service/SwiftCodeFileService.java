package com.swiftcodes.app.service;

import com.swiftcodes.app.dto.SwiftCodeFileResponseDTO;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface SwiftCodeFileService {


    SwiftCodeFileResponseDTO parseAndSaveSwiftCodes(MultipartFile file);

    SwiftCodeFileResponseDTO parseAndSaveSwiftCodes(InputStream inputStream, String originalFilename);
}

