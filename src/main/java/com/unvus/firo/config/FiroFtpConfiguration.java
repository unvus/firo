package com.unvus.firo.config;

import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.adapter.ftp.FtpAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

@Configuration
@ConditionalOnProperty(prefix = "firo.ftp", name = "host")
public class FiroFtpConfiguration {

    private final FiroProperties firoProperties;

    public FiroFtpConfiguration(FiroProperties firoProperties) {
        this.firoProperties = firoProperties;
    }

    @Bean(autowireCandidate = false)
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

    @Bean(autowireCandidate = false)
    public FtpRemoteFileTemplate firoFtpRemoteFileTemplate() {
        FtpRemoteFileTemplate template = new FtpRemoteFileTemplate(firoFtpSessionFactory());
        return template;
    }

    @Bean
    public FtpAdapter firoFtpAdapter() {
        return new FtpAdapter(firoFtpRemoteFileTemplate(), firoProperties.getFtp().getDirectUrl());
    }
}
