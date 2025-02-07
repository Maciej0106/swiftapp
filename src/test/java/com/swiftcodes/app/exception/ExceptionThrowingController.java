package com.swiftcodes.app.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExceptionThrowingController {

    @GetMapping("/test/notfound")
    public void throwNotFound() {
        throw new ResourceNotFoundException("Resource not found for test");
    }

    @GetMapping("/test/invaliddata")
    public void throwInvalidData() {
        throw new InvalidDataException("Invalid data provided for test");
    }

    @GetMapping("/test/duplicate")
    public void throwDuplicate() {
        throw new DuplicateEntryException("Duplicate entry for test");
    }
}
