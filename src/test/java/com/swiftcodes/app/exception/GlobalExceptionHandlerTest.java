package com.swiftcodes.app.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExceptionThrowingController.class)
@ContextConfiguration(classes = {ExceptionThrowingController.class, GlobalExceptionHandler.class})
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenResourceNotFoundException_thenReturns404() throws Exception {
        mockMvc.perform(get("/test/notfound")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Resource not found for test")));
    }

    @Test
    public void whenInvalidDataException_thenReturns400() throws Exception {
        mockMvc.perform(get("/test/invaliddata")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid data provided for test")));
    }

    @Test
    public void whenDuplicateEntryException_thenReturns409() throws Exception {
        mockMvc.perform(get("/test/duplicate")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Duplicate entry for test")));
    }
}
