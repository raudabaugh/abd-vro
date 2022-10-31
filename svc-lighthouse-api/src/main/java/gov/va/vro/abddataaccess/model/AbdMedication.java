package gov.va.vro.abddataaccess.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbdMedication implements Comparable<AbdMedication> {
  private String status;
  private List<String> notes;
  private String description;
  private int refills;
  private Boolean asthmaRelevant;
  private String duration;
  private String authoredOn;
  private List<String> dosageInstructions;
  private String route;

  @Override
  public int compareTo(AbdMedication otherMedication) {
    return StringUtils.compare(authoredOn, otherMedication.authoredOn);
  }
}