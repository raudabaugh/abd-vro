package gov.va.vro.model.biekafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@Builder(toBuilder = true)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class BieMessagePayload {
  private Integer status;
  private String statusMessage;
  private ContentionEvent eventType;
  private long claimId;
  private long contentionId;
  private String contentionTypeCode;
  private String contentionClassificationName;
  private String diagnosticTypeCode;
  private String actionName;
  private String actionResultName;
  private long notifiedAt;
  private long occurredAt;
}
