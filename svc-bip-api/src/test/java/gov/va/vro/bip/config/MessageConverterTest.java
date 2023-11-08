package gov.va.vro.bip.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.vro.bip.model.ClaimContention;
import gov.va.vro.bip.model.contentions.GetClaimContentionsResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.util.List;

@Slf4j
public class MessageConverterTest {

  Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

  @Test
  public void test() {
    ObjectMapper mapper = new ObjectMapper();

    GetClaimContentionsResponse resp =
        GetClaimContentionsResponse.builder()
            .statusCode(200)
            .statusMessage("OK")
            .contentions(
                List.of(ClaimContention.builder().build(), ClaimContention.builder().build()))
            .build();
    JsonNode jsonNode = mapper.valueToTree(resp);
    log.info(jsonNode.toPrettyString());

    GetClaimContentionsResponse nope =
        resp.toBuilder()
            .statusCode(400)
            .statusMessage("NOPE")
            .contentions(List.of(ClaimContention.builder().build()))
            .build();
    jsonNode = mapper.valueToTree(nope);
    log.info(jsonNode.toPrettyString());
  }
}