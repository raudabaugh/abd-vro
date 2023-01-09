package gov.va.vro.service.provider.mas;

import gov.va.vro.model.AbdEvidence;
import gov.va.vro.model.HealthDataAssessment;
import gov.va.vro.model.event.Auditable;
import gov.va.vro.model.mas.MasAutomatedClaimPayload;
import lombok.Data;

@Data
public class MasProcessingObject implements Auditable {

  private MasAutomatedClaimPayload claimPayload;
  private AbdEvidence evidence;
  private HealthDataAssessment healthDataAssessment;
  private boolean isTSOJ = false;

  public int getCollectionId() {
    return claimPayload.getCollectionId();
  }

  public String getClaimId() {
    return claimPayload.getClaimDetail().getBenefitClaimId();
  }

  public String getVeteranIcn() {
    return claimPayload.getVeteranIcn();
  }

  public String getDiagnosticCode() {
    return claimPayload.getDiagnosticCode();
  }

  @Override
  public String getEventId() {
    return claimPayload.getEventId();
  }

  @Override
  public String getDetails() {
    return claimPayload.getDetails();
  }

  public String getDisabilityActionType() {
    return claimPayload.getDisabilityActionType();
  }

  @Override
  public String getDisplayName() {
    return claimPayload.getDisplayName();
  }
}