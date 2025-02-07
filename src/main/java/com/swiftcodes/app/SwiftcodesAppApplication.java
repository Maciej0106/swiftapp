package com.swiftcodes.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SwiftcodesAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwiftcodesAppApplication.class, args);
	}
}
