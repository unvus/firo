package com.unvus.firo.core.adapter.s3;

import com.unvus.firo.core.adapter.AdapterProps;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class S3Props implements AdapterProps {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;
    private String roleArn;
    private String compression = "Y"; // default: Y

    @Override
    public boolean validate() {
        return StringUtils.isNotBlank(bucket) && StringUtils.isNotBlank(region);
    }
}
