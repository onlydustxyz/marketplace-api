package onlydust.com.marketplace.api.aws.s3.adapter;

import com.amazonaws.auth.AWSCredentials;
import lombok.Data;

@Data
public class AmazonS3Properties implements AWSCredentials {
    private String imageBucket;
    private String pdfBucket;
    private String region;
    private String accessKey;
    private String secretKey;

    @Override
    public String getAWSAccessKeyId() {
        return accessKey;
    }

    @Override
    public String getAWSSecretKey() {
        return secretKey;
    }
}
