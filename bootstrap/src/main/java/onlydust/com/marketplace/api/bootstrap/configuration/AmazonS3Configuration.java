package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.aws.s3.adapter.AmazonS3Properties;
import onlydust.com.marketplace.api.aws.s3.adapter.AwsS3Adapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static onlydust.com.marketplace.api.aws.s3.adapter.AmazonS3ClientFactory.getAmazonS3Client;


@Configuration
public class AmazonS3Configuration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.aws", ignoreUnknownFields = false)
    public AmazonS3Properties amazonS3Properties() {
        return new AmazonS3Properties();
    }

    @Bean
    public AwsS3Adapter awsS3Adapter(final AmazonS3Properties amazonS3Properties) {
        return new AwsS3Adapter(amazonS3Properties, getAmazonS3Client(amazonS3Properties));
    }
}
