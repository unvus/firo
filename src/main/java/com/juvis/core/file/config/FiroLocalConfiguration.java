package com.juvis.core.file.config;

import com.juvis.core.file.module.adapter.local.LocalAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FiroLocalConfiguration {

    @Bean
    public LocalAdapter firoLocalAdapter() {
        return new LocalAdapter();
    }
}
