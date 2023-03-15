package gov.va.vro.end2end.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AutomatedClaimTestSpec {
  private String collectionId;
  private String expectedMessage;
  private String payloadPath;
  private boolean checkSlack;

  public AutomatedClaimTestSpec(String collectionId) {
    this.collectionId = collectionId;
  }
}