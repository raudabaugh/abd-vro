package gov.va.vro.end2end;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.vro.api.responses.MasResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public class VroV2Tests {

  private static final String BASE_URL = "http://localhost:8080/v2";
  private static final String EXAM_ORDERING_URL = BASE_URL + "/examOrderingStatus";
  private static final String AUTOMATED_CLAIM_URL = BASE_URL + "/automatedClaim";

  private static final String JWT_TOKEN =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImMwOTI5NTJlLTM4ZDYtNDNjNi05MzBlLWZmOTNiYTUxYjA4ZiJ9.eyJleHAiOjk5OTk5OTk5OTksImlhdCI6MTY0MTA2Nzk0OSwianRpIjoiNzEwOTAyMGEtMzlkOS00MWE4LThlNzgtNTllZjAwYTlkNDJlIiwiaXNzIjoiaHR0cHM6Ly9zYW5kYm94LWFwaS52YS5nb3YvaW50ZXJuYWwvYXV0aC92Mi92YWxpZGF0aW9uIiwiYXVkIjoibWFzX2RldiIsInN1YiI6IjhjNDkyY2NmLTk0OGYtNDQ1Zi05NmY4LTMxZTdmODU5MDlkMiIsInR5cCI6IkJlYXJlciIsImF6cCI6Im1hc19kZXYiLCJzY29wZSI6Im9wZW5pZCB2cm9fbWFzIiwiY2xpZW50SWQiOiJtYXNfZGV2In0.Qb41CR1JIGGRlryi-XVtqyeNW73cU1YeBVqs9Bps3TA";

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final RestTemplate restTemplate = new RestTemplate();

  @Test
  void testExamOrderingStatus_invalidRequest() {
    var request = getOrderingStatusInvalidRequest();
    var requestEntity = getEntity(request);
    try {
      restTemplate.postForEntity(EXAM_ORDERING_URL, requestEntity, String.class);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertEquals(
          "400 : \"{\"message\":\"collectionStatus: Collection Status is required\"}\"",
          e.getMessage());
    }
  }

  @Test
  void testExamOrderingStatus() {
    var request = getOrderingStatusValidRequest();
    var requestEntity = getEntity(request);
    var response = restTemplate.postForEntity(EXAM_ORDERING_URL, requestEntity, MasResponse.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    var masResponse = response.getBody();
    assertEquals("Received", masResponse.getMessage());
  }

  @Test
  void testAutomatedClaim() {
    var path = "test-mas/claim-350-7101.json";
    var content = resourceToString(path);
    var requestEntity = getEntity(content);
    var response =
        restTemplate.postForEntity(AUTOMATED_CLAIM_URL, requestEntity, MasResponse.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    var masResponse = response.getBody();
    assertEquals("Received", masResponse.getMessage());
  }

  @Test
  void testAutomatedClaim_outOfScope() {
    var path = "test-mas/claim-350-7101-outofscope.json";
    var content = resourceToString(path);
    String url = BASE_URL + "/automatedClaim";
    var requestEntity = getEntity(content);
    var response = restTemplate.postForEntity(url, requestEntity, MasResponse.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    var masResponse = response.getBody();
    assertEquals("Out of scope", masResponse.getMessage());
  }

  @Test
  void testAutomatedClaim_missingAnchor() {
    var path = "test-mas/claim-351-7101-noanchor.json";
    var content = resourceToString(path);
    String url = BASE_URL + "/automatedClaim";
    var requestEntity = getEntity(content);
    var response = restTemplate.postForEntity(url, requestEntity, MasResponse.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    var masResponse = response.getBody();
    assertEquals("Missing anchor", masResponse.getMessage());
  }

  @SneakyThrows
  private String resourceToString(String path) {
    var io = this.getClass().getClassLoader().getResourceAsStream(path);
    try (Reader reader = new InputStreamReader(io)) {
      return FileCopyUtils.copyToString(reader);
    }
  }

  @SneakyThrows
  private String getOrderingStatusInvalidRequest() {
    return objectMapper.writeValueAsString(Map.of("key1", "value1", "key2", "value2"));
  }

  @SneakyThrows
  private String getOrderingStatusValidRequest() {
    var payload =
        Map.of(
            "collectionId",
            "123",
            "collectionStatus",
            "DRAFT",
            "examOrderDateTime",
            "2022-12-08T17:45:61Z",
            "eventId",
            "None");
    return objectMapper.writeValueAsString(payload);
  }

  private HttpEntity<String> getEntity(String content) {
    return new HttpEntity<>(content, getHttpHeaders());
  }

  private static HttpHeaders getHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(JWT_TOKEN);
    return headers;
  }
}