package com.unvus.firo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.firo.module.policy.impl.DateDirectoryPathPolicy;
import com.unvus.firo.module.service.FiroRegistry;
import com.unvus.util.JsonUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FiroConfiguration {

    private final ObjectMapper objectMapper;

    private final FiroProperties firoProperties;

    public FiroConfiguration(ObjectMapper objectMapper, FiroProperties firoProperties) {
        this.objectMapper = objectMapper;
        this.firoProperties = firoProperties;
    }

    @Bean
    InitializingBean firoInitializingBean(DirectoryPathPolicy directoryPathPolicy) {
        return () -> {
            if(FiroRegistry.getDefaultDirectoryPathPolicy() == null) {
                FiroRegistry.from(firoProperties, directoryPathPolicy);
            }
        };
    }

    @Bean
    public DirectoryPathPolicy directoryPathPolicy() {
        DateDirectoryPathPolicy policy = new DateDirectoryPathPolicy(
            DateDirectoryPathPolicy.DATE_SUBDIR_TYPE.YYYY_MM,
            firoProperties.getDirectory().getBaseDir(),
            firoProperties.getDirectory().getTmpDir()
        );

        return policy;

    }

    @Bean
    @ConditionalOnMissingBean
    public JsonUtil jsonUtil() {
        JsonUtil jsonUtil = new JsonUtil();
        jsonUtil.setMapper(objectMapper);
        return jsonUtil;
    }
}
