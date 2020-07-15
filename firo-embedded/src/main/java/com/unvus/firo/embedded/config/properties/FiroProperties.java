package com.unvus.firo.embedded.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties specific to Firo.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 */

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "unvus.firo")
public class FiroProperties {


    private Directory directory = new Directory();

    private final DatabaseProperties database = new DatabaseProperties();

    @Getter
    @Setter
    public static class Directory {
        private String tmpDir = System.getProperty("java.io.tmpdir");
        private String baseDir = System.getProperty("java.class.path");
    }

}
