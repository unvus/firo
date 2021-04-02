package com.unvus.firo.config;

import com.unvus.firo.module.adapter.s3.S3Adapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix="firo.s3", name = "key")
public class FiroS3Configuration {

    @Bean
    public S3Adapter firoS3Adapter() {
        return new S3Adapter();
    }
}
