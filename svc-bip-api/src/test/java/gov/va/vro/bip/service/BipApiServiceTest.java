package gov.va.vro.bip.service;

import static org.junit.jupiter.api.Assertions.*;

import gov.va.vro.bip.model.BipClaim;
import gov.va.vro.bip.model.BipUpdateClaimResp;
import gov.va.vro.bip.model.ClaimContention;
import gov.va.vro.bip.model.UpdateContentionReq;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Stubber;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class BipApiServiceTest {
  private static final long GOOD_CLAIM_ID = 9666959L;
  private static final long BAD_CLAIM_ID = 9666958L;
  private static final long BAD_JSON_CLAIM_ID = 9123456L;
  private static final long NOT_FOUND_CLAIM_ID = 9234567L;
  private static final long BAD_STATUS_CLAIM_ID = 9345678L;
  private static final long BAD_REST_CLAIM_ID = 9456789L;
  private static final String CONTENTION_RESPONSE_200 =
      "bip-test-data/contention_response_200.json";
  private static final String CONTENTION_RESPONSE_412 =
      "bip-test-data/contention_response_412.json";

  private static final String CLAIM_RESPONSE_404 = "bip-test-data/claim_response_404.json";
  private static final String CLAIM_RESPONSE_200 = "bip-test-data/claim_response_200.json";
  private static final String CLAIM_DETAILS = "/claims/%s";
  private static final String UPDATE_CLAIM_STATUS = "/claims/%s/lifecycle_status";
  private static final String CONTENTION = "/claims/%s/contentions";
  private static final String HTTPS = "https://";
  private static final String CLAIM_URL = "claims.bip.va.gov";
  private static final String CLAIM_SECRET = "thesecretissecurenowthatitislongenough2184vnrwma";
  private static final String CLAIM_USERID = "userid";
  private static final String CLAIM_ISSUER = "issuer";
  private static final String STATION_ID = "280";
  private static final String APP_ID = "bip";

  @InjectMocks private BipApiService service;

  @Mock private RestTemplate restTemplate;

  @Mock private BipApiProps bipApiProps;

  private String getTestData(String dataFile) throws Exception {
    String filename =
        Objects.requireNonNull(getClass().getClassLoader().getResource(dataFile)).getPath();
    Path filePath = Path.of(filename);
    return Files.readString(filePath);
  }

  private void mockBipApiProp() {
    BipApiProps props = new BipApiProps();
    props.setApplicationId(APP_ID);
    props.setApplicationId(CLAIM_ISSUER);
    props.setStationId(STATION_ID);
    props.setClaimClientId(CLAIM_USERID);

    Claims claims = props.toCommonJwtClaims();

    Mockito.doReturn(CLAIM_URL).when(bipApiProps).getClaimBaseUrl();
    Mockito.doReturn(CLAIM_ISSUER).when(bipApiProps).getClaimIssuer();
    Mockito.doReturn(CLAIM_SECRET).when(bipApiProps).getClaimSecret();
    Mockito.doReturn(claims).when(bipApiProps).toCommonJwtClaims();
  }

  @Test
  public void testGetClaimDetails() throws Exception {

    String resp200Body = getTestData(CLAIM_RESPONSE_200);
    String resp404Body = getTestData(CLAIM_RESPONSE_404);

    ResponseEntity<String> resp200 = ResponseEntity.ok(resp200Body);
    ResponseEntity<String> resp404 = ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp404Body);
    ResponseEntity<String> respBadJson =
        ResponseEntity.ok("}" + resp200Body); // add } to valid resp to cause parse error

    String baseUrl = HTTPS + CLAIM_URL;
    String claimUrl = baseUrl + String.format(CLAIM_DETAILS, GOOD_CLAIM_ID);
    String badClaimUrl = baseUrl + String.format(CLAIM_DETAILS, BAD_CLAIM_ID);
    String badJsonClaimUrl = baseUrl + String.format(CLAIM_DETAILS, BAD_JSON_CLAIM_ID);
    String notFoundClaimUrl = baseUrl + String.format(CLAIM_DETAILS, NOT_FOUND_CLAIM_ID);
    String badStatusClaimUrl = baseUrl + String.format(CLAIM_DETAILS, BAD_STATUS_CLAIM_ID);
    String badRestClaimUrl = baseUrl + String.format(CLAIM_DETAILS, BAD_REST_CLAIM_ID);

    mockResponseForUrl(Mockito.doReturn(resp200), claimUrl, HttpMethod.GET);
    mockResponseForUrl(Mockito.doReturn(resp404), badClaimUrl, HttpMethod.GET);
    mockResponseForUrl(Mockito.doReturn(respBadJson), badJsonClaimUrl, HttpMethod.GET);
    mockResponseForUrl(
        Mockito.doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)),
        notFoundClaimUrl,
        HttpMethod.GET);
    mockResponseForUrl(
        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)),
        badStatusClaimUrl,
        HttpMethod.GET);
    mockResponseForUrl(
        Mockito.doThrow(new RestClientException("Mock RestClient exception")),
        badRestClaimUrl,
        HttpMethod.GET);
    mockBipApiProp();
    try {
      BipClaim result = service.getClaimDetails(GOOD_CLAIM_ID).getClaim();
      assertNotNull(result);
    } catch (BipException e) {
      log.error("Positive getClaimDetails test failed.", e);
      fail();
    }

    try {
      BipClaim result = service.getClaimDetails(BAD_CLAIM_ID).getClaim();
      log.error("Negative getClaimDetails test failed. {}", result.getClaimId());
      fail();
    } catch (BipException e) {
      assertSame(HttpStatus.NOT_FOUND, e.getStatus());
    }

    try {
      BipClaim result = service.getClaimDetails(BAD_JSON_CLAIM_ID).getClaim();
      log.error("Negative getClaimDetails test failed. {}", result.getClaimId());
      fail();
    } catch (BipException e) {
      assertSame(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
    }

    try {
      BipClaim result = service.getClaimDetails(NOT_FOUND_CLAIM_ID).getClaim();
      log.error("Negative getClaimDetails test failed. {}", result.getClaimId());
      fail();
    } catch (BipException e) {
      assertSame(HttpStatus.NOT_FOUND, e.getStatus());
    }
    try {
      BipClaim result = service.getClaimDetails(BAD_STATUS_CLAIM_ID).getClaim();
      log.error("Negative getClaimDetails test failed. {}", result.getClaimId());
      fail();
    } catch (BipException e) {
      assertSame(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
    }
    try {
      BipClaim result = service.getClaimDetails(BAD_REST_CLAIM_ID).getClaim();
      log.error("Negative getClaimDetails test failed. {}", result.getClaimId());
      fail();
    } catch (BipException e) {
      assertSame(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
    }
  }

  private void mockResponseForUrl(Stubber response, String claimUrl, HttpMethod httpMethod) {
    response
        .when(restTemplate)
        .exchange(
            ArgumentMatchers.eq(claimUrl),
            ArgumentMatchers.eq(httpMethod),
            ArgumentMatchers.any(HttpEntity.class),
            ArgumentMatchers.eq(String.class));
  }

  @Test
  public void testSetClaimToRfdStatus() {
    String baseUrl = HTTPS + CLAIM_URL;
    String goodUrl = baseUrl + String.format(UPDATE_CLAIM_STATUS, GOOD_CLAIM_ID);
    String badUrl = baseUrl + String.format(UPDATE_CLAIM_STATUS, BAD_CLAIM_ID);
    ResponseEntity<String> resp200 = ResponseEntity.ok("{}");
    ResponseEntity<String> resp500 =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
    mockResponseForUrl(Mockito.doReturn(resp200), goodUrl, HttpMethod.PUT);
    mockResponseForUrl(Mockito.doReturn(resp500), badUrl, HttpMethod.PUT);

    mockBipApiProp();
    try {
      BipUpdateClaimResp result = service.setClaimToRfdStatus(GOOD_CLAIM_ID);
      assertEquals(result.getStatus(), 200);
    } catch (BipException e) {
      log.error("Positive setClaimToRfdStatus test failed.", e);
      fail();
    }

    try {
      BipUpdateClaimResp result = service.setClaimToRfdStatus(BAD_CLAIM_ID);
      log.error("Negative setClaimToRfdStatus test failed.");
      fail();
    } catch (BipException e) {
      assertSame(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
    }
  }

  @Test
  public void testGetClaimContention() throws Exception {
    String resp200Body = getTestData(CONTENTION_RESPONSE_200);
    String resp404Body = getTestData(CLAIM_RESPONSE_404);
    ResponseEntity<String> resp200 = ResponseEntity.ok(resp200Body);
    ResponseEntity<String> resp404 = ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp404Body);

    String baseUrl = HTTPS + CLAIM_URL;
    String goodUrl = baseUrl + String.format(CONTENTION, GOOD_CLAIM_ID);
    String badUrl = baseUrl + String.format(CONTENTION, BAD_CLAIM_ID);

    mockResponseForUrl(Mockito.doReturn(resp200), goodUrl, HttpMethod.GET);
    mockResponseForUrl(Mockito.doReturn(resp404), badUrl, HttpMethod.GET);

    mockBipApiProp();
    try {
      List<ClaimContention> result = service.getClaimContentions(GOOD_CLAIM_ID);
      assertTrue(result.size() > 0);
    } catch (BipException e) {
      log.error("Positive getClaimContentions test failed.", e);
      fail();
    }

    try {
      List<ClaimContention> result = service.getClaimContentions(BAD_CLAIM_ID);
      log.error("Negative getClaimContentions test failed.");
      fail();
    } catch (BipException e) {
      assertSame(HttpStatus.NOT_FOUND, e.getStatus());
    }
  }

  @Test
  public void testUpdateClaimContention() throws Exception {
    String resp200Body = getTestData(CONTENTION_RESPONSE_200);
    String resp412Body = getTestData(CONTENTION_RESPONSE_412);
    ResponseEntity<String> resp200 = ResponseEntity.ok(resp200Body);
    ResponseEntity<String> resp412 =
        ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(resp412Body);

    String baseUrl = HTTPS + CLAIM_URL;
    String goodUrl = baseUrl + String.format(CONTENTION, GOOD_CLAIM_ID);
    String badUrl = baseUrl + String.format(CONTENTION, BAD_CLAIM_ID);

    mockResponseForUrl(Mockito.doReturn(resp200), goodUrl, HttpMethod.PUT);
    mockResponseForUrl(Mockito.doReturn(resp412), badUrl, HttpMethod.PUT);

    mockBipApiProp();
    UpdateContentionReq request = UpdateContentionReq.builder().build();
    try {
      BipUpdateClaimResp result = service.updateClaimContention(GOOD_CLAIM_ID, request);
      assertEquals(200, result.getStatus());
    } catch (BipException e) {
      log.error("Positive updateClaimContention test failed.", e);
      fail();
    }

    try {
      BipUpdateClaimResp result = service.updateClaimContention(BAD_CLAIM_ID, request);
      log.error("Negative updateClaimContention test failed.");
      fail();
    } catch (BipException e) {
      assertSame(HttpStatus.PRECONDITION_FAILED, e.getStatus());
    }
  }
}
