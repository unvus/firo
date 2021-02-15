package com.unvus.firo.config;

import com.unvus.firo.config.properties.FiroProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

@Configuration
@ConditionalOnProperty(prefix="unvus.firo.ftp", name = "host")
public class FiroFtpConfiguration {

    private final FiroProperties firoProperties;

    public FiroFtpConfiguration(FiroProperties firoProperties) {
        this.firoProperties = firoProperties;
    }

    @Bean
    public DefaultFtpSessionFactory firoFtpSessionFactory() {
        FiroProperties.Ftp ftp = firoProperties.getFtp();
        DefaultFtpSessionFactory defaultFtpSessionFactory = new DefaultFtpSessionFactory();
        defaultFtpSessionFactory.setHost(ftp.getHost());
        defaultFtpSessionFactory.setPort(ftp.getPort());
        defaultFtpSessionFactory.setUsername(ftp.getUsername());
        defaultFtpSessionFactory.setPassword(ftp.getPassword());
        defaultFtpSessionFactory.setClientMode(ftp.getClientMode());
        return defaultFtpSessionFactory;
    }

    @Bean
    public FtpRemoteFileTemplate firoFtpRemoteFileTemplate(DefaultFtpSessionFactory dsf) {
        FtpRemoteFileTemplate template = new FtpRemoteFileTemplate(dsf);
        return template;
    }
}
