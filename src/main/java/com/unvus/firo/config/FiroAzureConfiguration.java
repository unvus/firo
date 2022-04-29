package com.unvus.firo.config;

import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.adapter.azure.AzureAdapter;
import com.unvus.firo.module.adapter.s3.S3Adapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "firo.azure", name = "container")
public class FiroAzureConfiguration {

    private final FiroProperties firoProperties;

    public FiroAzureConfiguration(FiroProperties firoProperties) {
        this.firoProperties = firoProperties;
    }

    @Bean
    public AzureAdapter firoAzureAdapter() throws Exception {
        return new AzureAdapter(firoProperties.getAzure());
    }

}
