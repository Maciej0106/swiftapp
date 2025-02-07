package com.swiftcodes.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "swift_codes", indexes = {
        @Index(name = "idx_swift_code", columnList = "swift_code"),
        @Index(name = "idx_country_iso2", columnList = "country_iso2_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwiftCode {

    @Id
    @Column(name = "swift_code", length = 11)
    private String swiftCode;

    @Column(name = "name", nullable = false)
    private String bankName;

    @Column(name = "address")
    private String address;

    @Column(name = "country_iso2_code", nullable = false, length = 2)
    private String countryIso2Code;

    @Column(name = "is_headquarter")
    private Boolean isHeadquarter;


    @Column(name = "base_swift_code", length = 8, insertable = false, updatable = false)
    private String baseSwiftCode;
}
