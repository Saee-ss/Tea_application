package com.teastall;

import com.teastall.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MenuService menuService;

    @Override
    public void run(String... args) {
        log.info("Initializing default menu items...");
        menuService.initDefaultMenu();
        log.info("Tea Stall App started! Visit http://localhost:8080");
    }
}
