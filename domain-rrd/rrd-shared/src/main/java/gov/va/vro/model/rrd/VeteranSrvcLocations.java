package gov.va.vro.model.rrd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VeteranSrvcLocations {

  @Schema(description = "Veteran service location", example = "Vietnam")
  private String location;

  @Schema(
      description = "Source document of information",
      example = "VA 21-3101 Request for Information")
  private String document;

  @Schema(description = "VBMS Receipt Date", example = "1999-12-31")
  private String receiptDate;

  @Schema(description = "Document Page Number", example = "55")
  private String page;

  @Schema(description = "Document Identifier", example = "{BFA4943C-4F56-4AC5-B48F-5FDE469B1226}")
  private String documentId;
}