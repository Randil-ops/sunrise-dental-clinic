package com.sunrise.dental.config;

import com.sunrise.dental.model.StaffUser;
import com.sunrise.dental.repository.StaffUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final StaffUserRepository staffUserRepository;

    public DataSeeder(StaffUserRepository staffUserRepository) {
        this.staffUserRepository = staffUserRepository;
    }

    @Override
    public void run(String... args) {
        if (staffUserRepository.count() == 0) {
            staffUserRepository.save(new StaffUser("admin", "admin123", "ADMIN"));
            System.out.println("Seeded default staff user -> username: admin, password: admin123");
        }
    }
}
