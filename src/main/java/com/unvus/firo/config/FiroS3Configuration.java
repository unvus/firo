package com.unvus.firo.config;

import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.adapter.s3.S3Adapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix="firo.s3", name = "accessKey")
public class FiroS3Configuration {

    private final FiroProperties firoProperties;

    public FiroS3Configuration(FiroProperties firoProperties) {
        this.firoProperties = firoProperties;
    }

    @Bean
    public S3Adapter firoS3Adapter() throws Exception {
        return new S3Adapter(firoProperties.getS3());
    }

}
