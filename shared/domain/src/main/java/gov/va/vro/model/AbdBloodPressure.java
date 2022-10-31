package gov.va.vro.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbdBloodPressure implements Comparable<AbdBloodPressure> {
  @Schema(description = "The date blood pressure is taken", example = "2020-08-05")
  private String date;

  @Schema(description = "Diastolic measurement")
  private AbdBpMeasurement diastolic;

  @Schema(description = "Systolic measurement")
  private AbdBpMeasurement systolic;

  @Schema(
      description = "Name of the physician who took the measurement",
      example = "DR. THOMAS REYNOLDS PHD")
  private String practitioner;

  @Schema(
      description = "Location where the measurement taken",
      example = "WASHINGTON VA MEDICAL CENTER")
  private String organization;

  @Override
  public int compareTo(AbdBloodPressure otherBp) {
    return StringUtils.compare(date, otherBp.date);
  }
}