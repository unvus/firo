package com.unvus.firo.config;

import com.unvus.firo.module.adapter.local.LocalAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FiroLocalConfiguration {

    @Bean
    public LocalAdapter firoLocalAdapter() {
        return new LocalAdapter();
    }
}
