package gov.va.vro.service.provider.bip.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.vro.service.provider.BipApiProps;
import gov.va.vro.service.provider.bip.BipException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * BIP claim API service.
 *
 * @author warren @Date 10/31/22
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BipApiService implements IBipApiService {
  private static final String CLAIM_DETAILS = "/claims/%s";
  private static final String UPDATE_CLAIM_STATUS = "/claims/%s/lifecycle_status";
  private static final String CONTENTION = "/claims/%s/contentions";
  private static final String SPECIAL_ISSUE_TYPES = "/contentions/special_issue_types";

  private static final String HTTPS = "https://";

  @Qualifier("bipCERestTemplate")
  @NonNull
  private final RestTemplate restTemplate;

  private final BipApiProps bipApiProps;

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public BipClaim getClaimDetails(long claimId) throws BipException {
    try {
      String url = HTTPS + bipApiProps.getClaimBaseUrl() + String.format(CLAIM_DETAILS, claimId);
      log.info("call {} to get claim info.", url);
      HttpHeaders headers = getBipHeader();
      HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(headers);
      ResponseEntity<String> bipResponse =
          restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
      if (bipResponse.getStatusCode() == HttpStatus.OK) {
        BipClaimResp result = mapper.readValue(bipResponse.getBody(), BipClaimResp.class);
        return result.getClaim();
      } else {
        log.error(
            "Failed to get claim details for {}. {} \n{}",
            claimId,
            bipResponse.getStatusCode(),
            bipResponse.getBody());
        throw new BipException(bipResponse.getStatusCode(), bipResponse.getBody());
      }
    } catch (JsonProcessingException e) {
      log.error("json processing error", e);
      throw new BipException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (HttpStatusCodeException e) {
      String message = "Failed to get claim info for claim ID " + claimId;
      log.error(message, e);
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new BipException(HttpStatus.BAD_REQUEST, message);
      } else {
        throw new BipException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
      }
    } catch (RestClientException e) {
      log.error("failed to update status to {} for claim {}.", claimId, e);
      throw new BipException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /**
   * Updates claim status.
   *
   * @param claimId claim ID for the claim to be updated.
   * @return an object with status and message.
   * @throws BipException error occurs
   */
  @Override
  public BipUpdateClaimResp setClaimToRfdStatus(long claimId) throws BipException {
    return updateClaimStatus(claimId, ClaimStatus.RFD);
  }

  @Override
  public BipUpdateClaimResp updateClaimStatus(long claimId, ClaimStatus status)
      throws BipException {
    final String description = status.getDescription();
    try {
      String url =
          HTTPS + bipApiProps.getClaimBaseUrl() + String.format(UPDATE_CLAIM_STATUS, claimId);
      log.info("call {} to update claim status to {}.", url, description);

      HttpHeaders headers = getBipHeader();
      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("claimLifecycleStatus", description);
      HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(requestBody, headers);
      ResponseEntity<String> bipResponse =
          restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
      if (bipResponse.getStatusCode() == HttpStatus.OK) {
        return new BipUpdateClaimResp(HttpStatus.OK, bipResponse.getBody());
      } else {
        throw new BipException(bipResponse.getStatusCode(), bipResponse.getBody());
      }
    } catch (RestClientException e) {
      log.error("failed to update status to {} for claim {}.", description, claimId, e);
      throw new BipException(e.getMessage(), e);
    }
  }

  @Override
  public List<ClaimContention> getClaimContentions(long claimId) throws BipException {
    try {
      String url = HTTPS + bipApiProps.getClaimBaseUrl() + String.format(CONTENTION, claimId);
      log.info("Call {} to get claim contention for {}.", url, claimId);
      HttpHeaders headers = getBipHeader();
      HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(headers);
      ResponseEntity<String> bipResponse =
          restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
      if (HttpStatus.OK == bipResponse.getStatusCode()) {
        BipContentionResp resp = mapper.readValue(bipResponse.getBody(), BipContentionResp.class);
        return resp.getContentions();
      } else if (HttpStatus.NO_CONTENT == bipResponse.getStatusCode()) {
        return new ArrayList<>();
      } else {
        log.error(
            "getClaimContentions returned {} for {}. {}",
            bipResponse.getStatusCode(),
            claimId,
            bipResponse.getBody());
        throw new BipException(bipResponse.getStatusCode(), bipResponse.getBody());
      }
    } catch (RestClientException | JsonProcessingException e) {
      log.error("failed to getClaimContentions for claim {}.", claimId, e);
      throw new BipException(e.getMessage(), e);
    }
  }

  @Override
  public BipUpdateClaimResp updateClaimContention(long claimId, UpdateContentionReq contention)
      throws BipException {
    try {
      String url = HTTPS + bipApiProps.getClaimBaseUrl() + String.format(CONTENTION, claimId);
      log.info("Call {} to update contention for {}.", url, claimId);
      HttpHeaders headers = getBipHeader();
      String updtContention = mapper.writeValueAsString(contention);
      HttpEntity<String> httpEntity = new HttpEntity<>(updtContention, headers);
      ResponseEntity<String> bipResponse =
          restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
      if (bipResponse.getStatusCode() == HttpStatus.OK) {
        return new BipUpdateClaimResp(HttpStatus.OK, bipResponse.getBody());
      } else {
        throw new BipException(bipResponse.getStatusCode(), bipResponse.getBody());
      }
    } catch (RestClientException | JsonProcessingException e) {
      log.error("failed to getClaimContentions for claim {}.", claimId, e);
      throw new BipException(e.getMessage(), e);
    }
  }

  @Override
  public boolean verifySpecialIssueTypes() {
    String url = HTTPS + bipApiProps.getClaimBaseUrl() + SPECIAL_ISSUE_TYPES;
    log.info("Call {} to get special_issue_types", url);

    HttpHeaders headers = getBipHeader();
    HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

    return response.getStatusCode() == HttpStatus.OK && !response.getBody().isEmpty();
  }

  private HttpHeaders getBipHeader() throws BipException {
    try {
      HttpHeaders bipHttpHeaders = new HttpHeaders();
      bipHttpHeaders.setContentType(MediaType.APPLICATION_JSON);

      String jwt = createJwt();
      bipHttpHeaders.add("Authorization", "Bearer " + jwt);
      return bipHttpHeaders;
    } catch (Exception e) {
      log.error("Failed to build BIP HTTP Headers.", e);
      throw new BipException(e.getMessage(), e);
    }
  }

  private String createJwt() throws BipException {
    Claims claims = bipApiProps.toCommonJwtClaims();
    Map<String, Object> headerType = new HashMap<>();
    headerType.put("typ", Header.JWT_TYPE);

    claims.put("iss", bipApiProps.getClaimIssuer());
    byte[] signSecretBytes = bipApiProps.getClaimSecret().getBytes(StandardCharsets.UTF_8);
    Key signingKey = new SecretKeySpec(signSecretBytes, SignatureAlgorithm.HS256.getJcaName());
    return Jwts.builder()
        .setSubject("Claim")
        .setIssuedAt(Calendar.getInstance().getTime())
        .setExpiration(claims.getExpiration())
        .setClaims(claims)
        .signWith(SignatureAlgorithm.HS256, signingKey)
        .setHeaderParams(headerType)
        .compact();
  }
}
