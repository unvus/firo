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
@ConfigurationProperties(prefix = "firo")
public class FiroProperties {


    private Directory directory = new Directory();

    private String directUrl;

    private Local local = new Local();

    private Ftp ftp = new Ftp();

    private Sftp sftp = new Sftp();

    private S3 s3 = new S3();

    private final DatabaseProperties database = new DatabaseProperties();

    @Getter
    @Setter
    public static class Directory {
        private String tmpDir = System.getProperty("java.io.tmpdir");
        private String baseDir = System.getProperty("java.io.tmpdir");
    }


    @Getter
    @Setter
    public static class Local {
        private Directory directory = new Directory();
    }

    @Getter
    @Setter
    public static class Ftp {
        private String host;
        private int port = 21;
        private String username;
        private String password;
        private int clientMode = PASSIVE_LOCAL_DATA_CONNECTION_MODE;
        private Directory directory = new Directory();
    }


    @Getter
    @Setter
    public static class Sftp {
        private String host;
        private int port = 21;
        private String username;
        private String password;
        private int clientMode = PASSIVE_LOCAL_DATA_CONNECTION_MODE;
        private Directory directory = new Directory();
    }


    @Getter
    @Setter
    public static class S3 {
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String region;
        private String roleArn;
        private String compression = "Y"; // default: Y

        private Directory directory = new Directory();
    }
}
