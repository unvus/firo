package com.unvus.firo.embedded.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unvus.firo.core.policy.DirectoryPathPolicy;
import com.unvus.firo.core.policy.impl.DateDirectoryPathPolicy;
import com.unvus.firo.embedded.config.properties.FiroProperties;
import com.unvus.util.JsonUtil;
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
    public DirectoryPathPolicy directoryPathPolicy() {
        DateDirectoryPathPolicy policy = new DateDirectoryPathPolicy(
            DateDirectoryPathPolicy.DATE_SUBDIR_TYPE.YYYY_MM,
            firoProperties.getDirectory().getBaseDir(),
            firoProperties.getDirectory().getTmpDir()
        );

        return policy;

    }

    @Bean
    @ConditionalOnMissingBean(JsonUtil.class)
    public JsonUtil jsonUtil() {
        JsonUtil jsonUtil = new JsonUtil();
        jsonUtil.setMapper(objectMapper);
        return jsonUtil;
    }
}
