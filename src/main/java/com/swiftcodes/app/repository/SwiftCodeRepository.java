package com.swiftcodes.app.repository;

import com.swiftcodes.app.model.SwiftCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SwiftCodeRepository extends JpaRepository<SwiftCode, String> {


    List<SwiftCode> findByCountryIso2Code(String countryIso2Code);


    List<SwiftCode> findByCountryIso2Code(String countryIso2Code, Pageable pageable);


    @Query("SELECT s FROM SwiftCode s WHERE s.isHeadquarter = false AND s.baseSwiftCode = ?1")
    List<SwiftCode> findBranchesByHeadquarter(String baseSwiftCode);


    List<SwiftCode> findAllByIsHeadquarterTrue();
}
