package onlydust.com.marketplace.api.aws.s3.adapter;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.port.output.ImageStoragePort;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@AllArgsConstructor
@Slf4j
public class AwsS3Adapter implements ImageStoragePort {

    private final AmazonS3Properties amazonS3Properties;
    private final AmazonS3 amazonS3;

    @Override
    public String storeImage(String fileName, InputStream imageInputStream) {
        return null;
    }

    private void uploadByteArrayToS3Bucket(final byte[] byteArray, final String bucketName, final String bucketKey) {
        final String md5 = new String(Base64.getEncoder().encode(DigestUtils.md5(byteArray)));
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        try {
            final String md5FromUploadedFile = putObjectToS3andGetContentFileUploadedMd5(bucketName, bucketKey,
                    byteArrayInputStream);
            if (!md5.equals(md5FromUploadedFile)) {
                LOGGER.error("Bucket {} {} md5 content is not equaled to file md5 content", bucketName, bucketKey);
                throw OnlydustException.internalServerError(null);
            }
        } catch (SdkClientException sdkClientException) {
            LOGGER.error("A technical exception happened with AWS SDK Client", sdkClientException);
            throw OnlydustException.internalServerError(sdkClientException);
        }
    }


    private String putObjectToS3andGetContentFileUploadedMd5(String bucketStorage, String bucketKeyId,
                                                             ByteArrayInputStream byteArrayInputStream) {
        if (amazonS3.doesBucketExistV2(bucketStorage)) {
            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(byteArrayInputStream.available());
            final PutObjectResult putObjectResult = amazonS3.putObject(bucketStorage, bucketKeyId,
                    byteArrayInputStream, metadata);
            return putObjectResult.getContentMd5();
        } else {
            LOGGER.error("Failed to upload report {} to S3 bucket {}", bucketKeyId, bucketStorage);
            throw OnlydustException.internalServerError(null);
        }
    }
}
