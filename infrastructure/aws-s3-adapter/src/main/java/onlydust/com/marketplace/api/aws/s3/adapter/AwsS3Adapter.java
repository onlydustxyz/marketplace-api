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

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;

@AllArgsConstructor
@Slf4j
public class AwsS3Adapter implements ImageStoragePort {

    private final AmazonS3Properties amazonS3Properties;
    private final AmazonS3 amazonS3;

    private static String getImageFileExtension(byte[] imageBytes) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes));
        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
        if (!imageReaders.hasNext()) {
            throw new OnlydustException(400, "Input stream is not an image", null);
        }
        return imageReaders.next().getFormatName().toLowerCase();
    }

    @Override
    public URL storeImage(InputStream imageInputStream) {
        try {
            final byte[] imageBytes = imageInputStream.readAllBytes();
            final String fileName = String.format("%s.%s", DigestUtils.md5Hex(imageBytes), getImageFileExtension(imageBytes));
            return uploadByteArrayToS3Bucket(imageBytes, amazonS3Properties.getImageBucket(), fileName);
        } catch (IOException e) {
            throw new OnlydustException(400, "Failed to read image input stream", e);
        }
    }

    private URL uploadByteArrayToS3Bucket(final byte[] byteArray, final String bucketName, final String bucketKey) {
        final String base64Md5 = new String(Base64.getEncoder().encode(DigestUtils.md5(byteArray)));
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        try {
            final String md5FromUploadedFile = putObjectToS3andGetContentFileUploadedMd5(bucketName, bucketKey,
                    byteArrayInputStream);
            if (!base64Md5.equals(md5FromUploadedFile)) {
                LOGGER.error("Bucket {} {} md5 content is not equaled to file md5 content", bucketName, bucketKey);
                throw OnlydustException.internalServerError(null);
            }
            return amazonS3.getUrl(bucketName, bucketKey);
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
