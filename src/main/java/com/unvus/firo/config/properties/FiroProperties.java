package com.unvus.firo.config.properties;

import com.unvus.firo.module.adapter.AdapterType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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

    private Local local = new Local(directory);

    private Ftp ftp = new Ftp();

    private Sftp sftp = new Sftp();

    private S3 s3 = new S3();

    private Azure azure = new Azure();

    private final DatabaseProperties database = new DatabaseProperties();

    private Map<AdapterType, AdapterProp> adapterMap = new HashMap();
    public AdapterProp getAdapterProp(AdapterType adapterType) {
        if(adapterMap.isEmpty()) {
            adapterMap.put(AdapterType.LOCAL, local);
            adapterMap.put(AdapterType.FTP, ftp);
            adapterMap.put(AdapterType.SFTP, sftp);
            adapterMap.put(AdapterType.S3, s3);
            adapterMap.put(AdapterType.AZURE, azure);
        }
        return adapterMap.get(adapterType);
    }

    @Getter
    @Setter
    public static class Directory {
        private String tmpDir = System.getProperty("java.io.tmpdir");
        private String baseDir = System.getProperty("java.io.tmpdir");
        private String separator = System.getProperty("file.separator");
    }


    @Getter
    @Setter
    public static class Local implements AdapterProp {
        private String directUrl;

        private Directory directory;

        public Local(Directory directory) {
            this.directory = directory;
        }
    }

    @Getter
    @Setter
    public static class Ftp implements AdapterProp {
        private String host;
        private int port = 21;
        private String username;
        private String password;
        private int clientMode = PASSIVE_LOCAL_DATA_CONNECTION_MODE;
        private Directory directory = new Directory();
        private String directUrl;
    }


    @Getter
    @Setter
    public static class Sftp implements AdapterProp {
        private String host;
        private int port = 21;
        private String username;
        private String password;
        private int clientMode = PASSIVE_LOCAL_DATA_CONNECTION_MODE;
        private Directory directory = new Directory();
        private String directUrl;
    }


    @Getter
    @Setter
    public static class S3 implements AdapterProp {
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String region;
        private String roleArn;
        private String compression = "Y"; // default: Y

        private Directory directory = new Directory();
        private String directUrl;
    }

    @Getter
    @Setter
    public static class Azure implements AdapterProp {
        private String connectionString;
        private String container;

        private Directory directory = new Directory();
        private String directUrl;
    }

    public interface AdapterProp {
        String getDirectUrl();

        Directory getDirectory();
    }
}
