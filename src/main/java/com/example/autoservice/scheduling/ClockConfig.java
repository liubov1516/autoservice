package com.example.autoservice.scheduling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

// Годинник як окремий бін, щоб у тестах його можна було підмінити фіксованим
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
