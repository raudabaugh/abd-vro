package gov.va.vro.bip.config;

import gov.va.vro.bip.service.BipApiProps;
import gov.va.vro.bip.service.ClaimProps;
import gov.va.vro.bip.service.BipException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;

/**
 * Configures BIP API access.
 *
 * @author warren @Date 10/31/22
 */
@Configuration
@Slf4j
@Setter
public class BipApiConfig {

  @Value("${truststore}")
  private String trustStore;

  @Value("${truststore_password}")
  private String password;

  @Value("${keystore}")
  private String keystore;

  @Bean
  public BipApiProps getBipApiProps() {
    return new BipApiProps();
  }

  @Bean
  public ClaimProps getClaimProps() {
    return new ClaimProps();
  }

  private KeyStore getKeyStore(String base64, String password)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    String noSpaceBase64 = base64.replaceAll("\\s+", "");
    byte[] decodedBytes = java.util.Base64.getDecoder().decode(noSpaceBase64);
    InputStream stream = new ByteArrayInputStream(decodedBytes);
    keyStore.load(stream, password.toCharArray());
    return keyStore;
  }

  /**
   * Get Rest template for BIP API connection.
   *
   * @param builder RestTemplateBuilder
   * @return Rest template, request factory
   * @throws BipException failure to create connection
   */
  @Bean(name = "bipCERestTemplate")
  public RestTemplate getHttpsRestTemplate(RestTemplateBuilder builder) throws BipException {
    try {
      if (trustStore.isEmpty() & password.isEmpty()) { // skip if it is test.
        log.info("No valid BIP mTLS setup. Skip related setup.");
        return new RestTemplate();
      }

      log.info("-------load keystore");
      KeyStore keyStoreObj = getKeyStore(keystore, password);
      log.info("-------load truststore");
      KeyStore trustStoreObj = getKeyStore(trustStore, password);

      log.info("------build SSLContext");
      SSLContext sslContext =
          new SSLContextBuilder()
              .loadTrustMaterial(trustStoreObj, null)
              .loadKeyMaterial(keyStoreObj, password.toCharArray())
              .build();

      SSLConnectionSocketFactory sslConFactory = new SSLConnectionSocketFactory(sslContext);

      CloseableHttpClient httpClient =
          HttpClients.custom().setSSLSocketFactory(sslConFactory).build();
      ClientHttpRequestFactory requestFactory =
          new HttpComponentsClientHttpRequestFactory(httpClient);
      return new RestTemplate(requestFactory);
    } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
      log.error("Failed to create SSL context for VA certificate. {}", e.getMessage(), e);
      throw new BipException("Failed to create SSL context.", e);
    } catch (Exception e) {
      log.error("Unexpected error.", e);
      throw new BipException(e.getMessage(), e);
    }
  }
}