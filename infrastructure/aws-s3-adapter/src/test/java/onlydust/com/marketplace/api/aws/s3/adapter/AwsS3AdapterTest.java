package onlydust.com.marketplace.api.aws.s3.adapter;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AwsS3AdapterTest {


    private final Faker faker = new Faker();

    @Test
    void should_save_image_to_s3_bucket_storage() throws OnlydustException, MalformedURLException {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AwsS3Adapter AwsS3Adapter =
                new AwsS3Adapter(amazonS3Properties, amazonS3);
        final byte[] bytes = fakeImage();
        final PutObjectResult putObjectResultMock = mock(PutObjectResult.class);
        final String expectedFileName = "79fb1604b4c2cd782205f37490162bc8.jpeg";

        // When
        when(amazonS3.doesBucketExistV2(eq(amazonS3Properties.getImageBucket()))).thenReturn(true);
        when(amazonS3.putObject(eq(amazonS3Properties.getImageBucket()), eq(expectedFileName), any(), any())).thenReturn(putObjectResultMock);
        when(putObjectResultMock.getContentMd5()).thenReturn(new String(Base64.getEncoder().encode(DigestUtils.md5(bytes))));
        when(amazonS3.getUrl(eq(amazonS3Properties.getImageBucket()), eq(expectedFileName))).thenReturn(new URL("https://my-s3-bucket.com/my-file.jpeg"));
        final URL imageUrl = AwsS3Adapter.storeImage(new ByteArrayInputStream(bytes));

        // Then
        verify(amazonS3, times(1)).putObject(any(), any(), any(), any());
    }


    @Test
    void should_raise_an_exception_when_client_raise_a_runtime_while_saving() {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AwsS3Adapter AwsS3Adapter =
                new AwsS3Adapter(amazonS3Properties, amazonS3);
        final byte[] bytes = fakeImage();

        // When
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getImageBucket())).thenReturn(true);
        when(amazonS3.putObject(anyString(), anyString(), any(), any())).thenThrow(new SdkClientException(faker.name().firstName()));

        Assertions.assertThatThrownBy(() -> {
            AwsS3Adapter.storeImage(new ByteArrayInputStream(bytes));
        }).isInstanceOf(OnlydustException.class);
    }

    @Test
    void should_raise_an_exception_for_not_equaled_md5_while_saving() {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AwsS3Adapter AwsS3Adapter =
                new AwsS3Adapter(amazonS3Properties, amazonS3);
        final byte[] bytes = fakeImage();
        final PutObjectResult putObjectResultMock = mock(PutObjectResult.class);

        // When
        when(amazonS3.putObject(anyString(), anyString(), any(), any())).thenReturn(putObjectResultMock);
        when(putObjectResultMock.getContentMd5()).thenReturn(faker.ancient().hero());
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getImageBucket())).thenReturn(true);
        OnlydustException exception = null;
        try {
            AwsS3Adapter.storeImage(new ByteArrayInputStream(bytes));
        } catch (OnlydustException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(500);
        assertThat(exception.getMessage()).isEqualTo("INTERNAL_SERVER_ERROR");
    }


    @Test
    void should_raise_an_exception_for_not_existing_bucket_while_saving() {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AwsS3Adapter AwsS3Adapter =
                new AwsS3Adapter(amazonS3Properties, amazonS3);
        final byte[] bytes = fakeImage();

        // When
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getImageBucket())).thenReturn(false);
        OnlydustException exception = null;
        try {
            AwsS3Adapter.storeImage(new ByteArrayInputStream(bytes));
        } catch (OnlydustException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(500);
        assertThat(exception.getMessage()).isEqualTo("INTERNAL_SERVER_ERROR");
    }


    private AmazonS3Properties buildAmazonS3PropertiesStub() {
        final AmazonS3Properties amazonS3Properties = new AmazonS3Properties();
        amazonS3Properties.setImageBucket(faker.name().name());
        return amazonS3Properties;
    }

    @SneakyThrows
    private byte[] fakeImage() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("fixtures/some_picture.jpg")) {
            assert inputStream != null;
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}