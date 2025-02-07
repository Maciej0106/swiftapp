package com.swiftcodes.app.repository;

import com.swiftcodes.app.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, String> {
}
