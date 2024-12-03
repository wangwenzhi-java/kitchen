package com.wwz.kitchen.framework.config;

/**
 * Created by wenzhi.wang.
 * on 2024/11/16.
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QiniuConfig {

    @Value("${qiniu.accessKey}")
    private String accessKey;

    @Value("${qiniu.secretKey}")
    private String secretKey;

    @Value("${qiniu.bucketName}")
    private String bucketName;

    @Value("${qiniu.bucketDomain}")
    private String bucketDomain;

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getBucketDomain() {
        return bucketDomain;
    }
}

