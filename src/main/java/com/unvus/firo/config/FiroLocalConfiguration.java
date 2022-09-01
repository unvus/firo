package com.unvus.firo.config;

import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.adapter.local.LocalAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FiroLocalConfiguration {

    private final FiroProperties firoProperties;

    public FiroLocalConfiguration(FiroProperties firoProperties) {
        this.firoProperties = firoProperties;
    }

    @Bean
    public LocalAdapter firoLocalAdapter() {
        LocalAdapter adapter = new LocalAdapter();
        adapter.setDirectUrl(firoProperties.getLocal().getDirectUrl());
        return adapter;
    }
}
