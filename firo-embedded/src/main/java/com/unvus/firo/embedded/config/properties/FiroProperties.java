package com.unvus.firo.embedded.config.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * Properties specific to Firo.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 */
@Getter
@Setter
public class FiroProperties {


    private Directory directory = new Directory();

    private final DatabaseProperties database = new DatabaseProperties();

    @Getter
    @Setter
    public static class Directory {
        private String tmpDir;
        private String baseDir;
    }

}
