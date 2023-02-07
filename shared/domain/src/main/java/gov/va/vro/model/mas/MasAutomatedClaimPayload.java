package gov.va.vro.model.mas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gov.va.vro.model.event.Auditable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder(toBuilder = true)
@Getter
public class MasAutomatedClaimPayload implements Auditable {

  public static final String BLOOD_PRESSURE_DIAGNOSTIC_CODE = "7101";
  public static final String DISABILITY_ACTION_TYPE_NEW = "NEW";
  public static final String DISABILITY_ACTION_TYPE_INCREASE = "INCREASE";
  public static final String AGENT_ORANGE_FLASH_ID = "266";
  
  private String correlationId;

  @NotBlank(message = "Date of Birth cannot be empty")
  private String dateOfBirth;

  @NotBlank(message = "First Name cannot be empty")
  private String firstName;

  @NotBlank(message = "Last Name cannot be empty")
  private String lastName;

  private String gender;

  @NotNull(message = "Collection ID cannot be empty")
  private Integer collectionId;

  @NotNull @Valid private VeteranIdentifiers veteranIdentifiers;

  @NotNull @Valid private ClaimDetail claimDetail;

  @Setter private String offRampReason;

  private List<String> veteranFlashIds;

  @JsonIgnore
  public String getDiagnosticCode() {
    if (claimDetail == null || claimDetail.getConditions() == null) {
      return null;
    }
    return claimDetail.getConditions().getDiagnosticCode();
  }

  @JsonIgnore
  public String getDisabilityActionType() {
    if (claimDetail == null || claimDetail.getConditions() == null) {
      return null;
    }
    return claimDetail.getConditions().getDisabilityActionType();
  }

  /**
   * Check if it is in scope.
   *
   * @return true or false.
   */
  @JsonIgnore
  public boolean isInScope() {
    return Objects.equals(getDiagnosticCode(), BLOOD_PRESSURE_DIAGNOSTIC_CODE)
        && (Objects.equals(getDisabilityActionType(), DISABILITY_ACTION_TYPE_NEW)
            || Objects.equals(getDisabilityActionType(), DISABILITY_ACTION_TYPE_INCREASE));
  }

  @JsonIgnore
  public Boolean isPresumptive() {
    if (Objects.equals(getDisabilityActionType(), DISABILITY_ACTION_TYPE_NEW)) {
      return (veteranFlashIds != null
          && !Collections.disjoint(
              veteranFlashIds,
              Arrays.asList(MasVeteranFlashProps.getInstance().getAgentOrangeFlashIds())));
    }
    return null;
  }

  @JsonIgnore
  public Integer getClaimId() {
    return claimDetail == null ? null : Integer.parseInt(claimDetail.getBenefitClaimId());
  }

  @JsonIgnore
  public String getVeteranIcn() {
    return veteranIdentifiers == null ? null : veteranIdentifiers.getIcn();
  }

  @JsonIgnore
  @Override
  public String getEventId() {
    return correlationId;
  }

  @Override
  @SneakyThrows
  @JsonIgnore
  public Map<String, String> getDetails() {
    Map<String, String> detailsMap = new HashMap<>();
    detailsMap.put("claimId", Objects.toString(getClaimId()));
    detailsMap.put("collectionId", Objects.toString(getCollectionId()));
    detailsMap.put("diagnosticCode", getDiagnosticCode());
    detailsMap.put("veteranIcn", getVeteranIcn());
    detailsMap.put("disabilityActionType", getDisabilityActionType());
    detailsMap.put(
        "flashIds", getVeteranFlashIds() == null ? null : Objects.toString(getVeteranFlashIds()));
    detailsMap.put("inScope", Objects.toString(isInScope()));
    detailsMap.put("presumptive", Objects.toString(isPresumptive()));
    detailsMap.put(
        "submissionSource", claimDetail == null ? null : claimDetail.getClaimSubmissionSource());
    detailsMap.put(
        "submissionDate",
        claimDetail == null ? null : Objects.toString(claimDetail.getClaimSubmissionDateTime()));
    detailsMap.put("offRampReason", getOffRampReason());
    return detailsMap;
  }

  @JsonIgnore
  @Override
  public String getDisplayName() {
    return "Automated Claim";
  }
}
