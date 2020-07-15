package com.unvus.firo.embedded.config;

import com.unvus.firo.core.policy.DirectoryPathPolicy;
import com.unvus.firo.core.policy.impl.DateDirectoryPathPolicy;
import com.unvus.firo.embedded.config.properties.FiroProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FiroConfiguration {

    private final FiroProperties firoProperties;

    public FiroConfiguration(FiroProperties firoProperties) {
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
}
