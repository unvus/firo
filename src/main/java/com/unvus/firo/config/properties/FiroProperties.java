package com.unvus.firo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static org.apache.commons.net.ftp.FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE;

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

    private String directUrl;

    private Ftp ftp = new Ftp();

    private final DatabaseProperties database = new DatabaseProperties();

    @Getter
    @Setter
    public static class Directory {
        private String tmpDir = System.getProperty("java.io.tmpdir");
        private String baseDir = System.getProperty("java.io.tmpdir");
    }

    @Getter
    @Setter
    public static class Ftp {
        private String host;
        private int port = 21;
        private String username;
        private String password;
        private int clientMode = PASSIVE_LOCAL_DATA_CONNECTION_MODE;
    }

}
