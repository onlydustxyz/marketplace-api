package onlydust.com.marketplace.api.aws.s3.adapter;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public interface AmazonS3ClientFactory {

  static AmazonS3 getAmazonS3Client(AmazonS3Properties amazonS3Properties) {
    return AmazonS3ClientBuilder.standard()
        .withRegion(amazonS3Properties.getRegion())
        .withCredentials(new AWSStaticCredentialsProvider(amazonS3Properties))
        .build();
  }
}
