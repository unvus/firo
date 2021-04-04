package com.unvus.firo.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.adapter.s3.S3Adapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix="firo.s3", name = "key")
public class FiroS3Configuration {

    private final FiroProperties firoProperties;

    public FiroS3Configuration(FiroProperties firoProperties) {
        this.firoProperties = firoProperties;
    }

    @Bean
    public S3Adapter firoS3Adapter() throws Exception {
        return new S3Adapter(createManager(firoProperties.getS3()), firoProperties.getS3());
    }

    private static TransferManager createManager(FiroProperties.S3 props) throws Exception {
        try {
            if (StringUtils.isNotEmpty(props.getRoleArn())) {
                return createManagerArn(props);
            } else {
                return createManagerDefault(props);
            }
        } catch (Exception e) {
            log.error("AWS S3 커넥션 오류");
            log.error("AWS S3 Properties---");
            log.error("AWS S3 Props.accessKey: {}", props.getAccessKey());
            log.error("AWS S3 Props.secretKey: {}", props.getSecretKey());
            log.error("AWS S3 Props.bucket: {}", props.getBucket());
            log.error("AWS S3 Props.region: {}", props.getRegion());
            log.error("AWS S3 Props.roleArn: {}", props.getRoleArn());
            throw e;
        }
    }

    private static TransferManager createManagerDefault(FiroProperties.S3 props) throws Exception {

        AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(props.getRegion()));
        if (props.getAccessKey() != null && !"".equals(props.getAccessKey()) && props.getSecretKey() != null && !"".equals(props.getSecretKey())) {
            clientBuilder.withCredentials(
                new AWSStaticCredentialsProvider(new BasicAWSCredentials(props.getAccessKey(), props.getSecretKey()))
            );
        }

        TransferManager transferManager = TransferManagerBuilder.standard()
            .withS3Client(
                clientBuilder.build()
            )
            .build();

        return transferManager;
    }


    private static TransferManager createManagerArn(FiroProperties.S3 props) throws Exception {

        // Creating the STS client is part of your trusted code. It has
        // the security credentials you use to obtain temporary security credentials.
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
//            .withCredentials(new ProfileCredentialsProvider())
            .withRegion(props.getRegion())
            .build();

        String randomSessionName = RandomStringUtils.randomAlphabetic(20); // ??
        log.info("random RoleSessionName: {}", randomSessionName);

        // Obtain credentials for the IAM role. Note that you cannot assume the role of an AWS root account;
        // Amazon S3 will deny access. You must use credentials for an IAM user or an IAM role.
        AssumeRoleRequest roleRequest = new AssumeRoleRequest()
            .withRoleArn(props.getRoleArn())
            .withRoleSessionName(randomSessionName);
        AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
        Credentials sessionCredentials = roleResponse.getCredentials();

        // Create a BasicSessionCredentials object that contains the credentials you just retrieved.
        BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
            sessionCredentials.getAccessKeyId(),
            sessionCredentials.getSecretAccessKey(),
            sessionCredentials.getSessionToken());

        log.info("awsCredentials.accessKey: {}", sessionCredentials.getAccessKeyId());
        log.info("awsCredentials.secretAccessKey: {}", sessionCredentials.getSecretAccessKey());
        log.info("awsCredentials.sessionToken: {}", sessionCredentials.getSessionToken());

        AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(props.getRegion()));
        //if (props.getAccessKey() != null && !"".equals(props.getAccessKey()) && props.getSecretKey() != null && !"".equals(props.getSecretKey())) {
        clientBuilder.withCredentials(
            new AWSStaticCredentialsProvider(awsCredentials)
        );
        //}
        // Verify that assuming the role worked and the permissions are set correctly
        // by getting a set of object keys from the bucket.
        TransferManager transferManager = TransferManagerBuilder.standard()
            .withS3Client(
                clientBuilder.build()
            )
            .build();

        return transferManager;
    }
}
