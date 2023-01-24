package gov.va.vro.model.bipevidence;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import javax.validation.Valid;

/** Document types for UI consumption. */
@Schema(name = "documentType", description = "Document types for UI consumption.")
@JsonTypeName("documentType")
public class DocumentType {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("createDateTime")
  private String createDateTime;

  @JsonProperty("modifiedDateTime")
  private String modifiedDateTime;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("isUserUploadable")
  private Boolean isUserUploadable;

  @JsonProperty("documentCategory")
  private DocumentCategory documentCategory;

  public DocumentType id(Long id) {
    this.id = id;
    return this;
  }

  /**
   * Get id.
   *
   * @return id
   */
  @Schema(name = "id", required = false)
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public DocumentType createDateTime(String createDateTime) {
    this.createDateTime = createDateTime;
    return this;
  }

  /**
   * Get createDateTime.
   *
   * @return createDateTime
   */
  @Schema(name = "createDateTime", required = false)
  public String getCreateDateTime() {
    return createDateTime;
  }

  public void setCreateDateTime(String createDateTime) {
    this.createDateTime = createDateTime;
  }

  public DocumentType modifiedDateTime(String modifiedDateTime) {
    this.modifiedDateTime = modifiedDateTime;
    return this;
  }

  /**
   * Get modifiedDateTime.
   *
   * @return modifiedDateTime
   */
  @Schema(name = "modifiedDateTime", required = false)
  public String getModifiedDateTime() {
    return modifiedDateTime;
  }

  public void setModifiedDateTime(String modifiedDateTime) {
    this.modifiedDateTime = modifiedDateTime;
  }

  public DocumentType name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name.
   *
   * @return name
   */
  @Schema(name = "name", required = false)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DocumentType description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description.
   *
   * @return description
   */
  @Schema(name = "description", required = false)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public DocumentType isUserUploadable(Boolean isUserUploadable) {
    this.isUserUploadable = isUserUploadable;
    return this;
  }

  /**
   * Get isUserUploadable.
   *
   * @return isUserUploadable
   */
  @Schema(name = "isUserUploadable", required = false)
  public Boolean getIsUserUploadable() {
    return isUserUploadable;
  }

  public void setIsUserUploadable(Boolean isUserUploadable) {
    this.isUserUploadable = isUserUploadable;
  }

  public DocumentType documentCategory(DocumentCategory documentCategory) {
    this.documentCategory = documentCategory;
    return this;
  }

  /**
   * Get documentCategory.
   *
   * @return documentCategory
   */
  @Valid
  @Schema(name = "documentCategory", required = false)
  public DocumentCategory getDocumentCategory() {
    return documentCategory;
  }

  public void setDocumentCategory(DocumentCategory documentCategory) {
    this.documentCategory = documentCategory;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocumentType documentType = (DocumentType) o;
    return Objects.equals(this.id, documentType.id)
        && Objects.equals(this.createDateTime, documentType.createDateTime)
        && Objects.equals(this.modifiedDateTime, documentType.modifiedDateTime)
        && Objects.equals(this.name, documentType.name)
        && Objects.equals(this.description, documentType.description)
        && Objects.equals(this.isUserUploadable, documentType.isUserUploadable)
        && Objects.equals(this.documentCategory, documentType.documentCategory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        createDateTime,
        modifiedDateTime,
        name,
        description,
        isUserUploadable,
        documentCategory);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DocumentType {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    createDateTime: ").append(toIndentedString(createDateTime)).append("\n");
    sb.append("    modifiedDateTime: ").append(toIndentedString(modifiedDateTime)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    isUserUploadable: ").append(toIndentedString(isUserUploadable)).append("\n");
    sb.append("    documentCategory: ").append(toIndentedString(documentCategory)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}