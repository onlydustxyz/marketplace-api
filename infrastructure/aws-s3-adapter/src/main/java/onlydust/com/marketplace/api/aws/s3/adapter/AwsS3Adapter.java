package onlydust.com.marketplace.api.aws.s3.adapter;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.port.output.InvoiceStoragePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
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

import static java.lang.String.format;

@AllArgsConstructor
@Slf4j
public class AwsS3Adapter implements ImageStoragePort, InvoiceStoragePort {

    private final AmazonS3Properties amazonS3Properties;
    private final AmazonS3 amazonS3;

    private static String getImageFileExtension(byte[] imageBytes) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes));
        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
        if (!imageReaders.hasNext()) {
            throw OnlyDustException.badRequest("Input stream is not an image", null);
        }
        return imageReaders.next().getFormatName().toLowerCase();
    }

    @Override
    public URL storeImage(InputStream imageInputStream) {
        try {
            final byte[] imageBytes = imageInputStream.readAllBytes();
            final String fileName = format("%s.%s", DigestUtils.md5Hex(imageBytes), getImageFileExtension(imageBytes));
            return uploadByteArrayToS3Bucket(imageBytes, amazonS3Properties.getImageBucket(), fileName);
        } catch (IOException e) {
            throw OnlyDustException.badRequest("Failed to read image input stream", e);
        }
    }

    @Override
    public URL storePdfForName(String name, InputStream inputStream) {
        try {
            final byte[] bytes = inputStream.readAllBytes();
            return uploadByteArrayToS3Bucket(bytes, amazonS3Properties.getImageBucket(), name);
        } catch (IOException e) {
            throw OnlyDustException.badRequest("Failed to read input stream", e);
        }
    }

    private URL uploadByteArrayToS3Bucket(final byte[] byteArray, final String bucketName, final String bucketKey) {
        final String base64Md5 = new String(Base64.getEncoder().encode(DigestUtils.md5(byteArray)));
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        try {
            final String md5FromUploadedFile = putObjectToS3andGetContentFileUploadedMd5(bucketName, bucketKey,
                    byteArrayInputStream);
            if (!base64Md5.equals(md5FromUploadedFile)) {
                throw OnlyDustException.internalServerError(format("Bucket %s %s md5 differs from file md5",
                        bucketName, bucketKey));
            }
            return amazonS3.getUrl(bucketName, bucketKey);
        } catch (SdkClientException sdkClientException) {
            throw OnlyDustException.internalServerError("Failed to upload data to S3", sdkClientException);
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
            throw OnlyDustException.internalServerError(format("Failed to upload %s to S3 bucket %s", bucketKeyId,
                    bucketStorage));
        }
    }
}
