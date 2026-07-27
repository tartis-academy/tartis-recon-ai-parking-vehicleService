package com.tartis_recon_ai_parking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class VehicleServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test that context loads successfully
    }

    @Test
    void mainMethodRuns() {
        // Test main method execution
        VehicleServiceApplication.main(new String[]{
            "--spring.datasource.url=jdbc:h2:mem:testmain;DB_CLOSE_DELAY=-1",
            "--spring.datasource.driverClassName=org.h2.Driver",
            "--spring.datasource.username=sa",
            "--spring.datasource.password=",
            "--spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
            "--server.port=0" // Prevent port collision
        });
    }
}
