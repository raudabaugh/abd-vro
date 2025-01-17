package gov.va.vro.mockbipclaims.api;

import gov.va.vro.mockbipclaims.model.mock.request.TempJurisdictionStationRequest;
import gov.va.vro.mockbipclaims.model.mock.response.ContentionUpdatesResponse;
import gov.va.vro.mockbipclaims.model.mock.response.LifecycleUpdatesResponse;
import gov.va.vro.mockbipclaims.model.mock.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This is a helper API to easy updates to claims and contentions. Integration and end-to-end tests
 * can use these end points to detect if updates happened and reset the updates. For now this API
 * assumes same claim cannot be updated at the same time from different tests.
 */
@Tag(name = "Updates")
@RequestMapping("/")
public interface UpdatesApi {

  /** DELETE /updates/{claimId}: Deletes all updates the claim. */
  @Operation(
      operationId = "deleteUpdates",
      summary = "Resets all updates.",
      description = "Resets all updates.",
      responses = {@ApiResponse(responseCode = "204", description = "Reset is successful")})
  @RequestMapping(method = RequestMethod.DELETE, value = "/updates/{claimId}")
  ResponseEntity<Void> deleteUpdates(
      @Parameter(
              name = "claimId",
              description = "The CorpDB BNFT_CLAIM_ID",
              required = true,
              in = ParameterIn.PATH)
          @PathVariable("claimId")
          Long claimId);

  /**
   * GET /updates/{claimId}/lifecycle_status: Retrieves if the claim lifecycle status has updates.
   */
  @Operation(
      operationId = "getLifecycleStatusUpdates",
      summary = "Retrieves if the claim lifecycle status has updates.",
      description = "Retrieves if the claim lifecycle status has updates.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Success.",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = LifecycleUpdatesResponse.class))
            })
      })
  @RequestMapping(
      method = RequestMethod.GET,
      value = "/updates/{claimId}/lifecycle_status",
      produces = {"application/json"})
  ResponseEntity<LifecycleUpdatesResponse> getLifecycleStatusUpdates(
      @Parameter(
              name = "claimId",
              description = "The CorpDB BNFT_CLAIM_ID",
              required = true,
              in = ParameterIn.PATH)
          @PathVariable("claimId")
          Long claimId);

  /**
   * GET /updates/{claimId}/contentions: Retrieves if the claim contentions have updates. updated.
   */
  @Operation(
      operationId = "getContentionsUpdates",
      summary = "Retrieves if claim contentions has updates.",
      description = "Retrieves if claim contentions has updates.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Success.",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = LifecycleUpdatesResponse.class))
            })
      })
  @RequestMapping(
      method = RequestMethod.GET,
      value = "/updates/{claimId}/contentions",
      produces = {"application/json"})
  ResponseEntity<ContentionUpdatesResponse> getContentionsUpdates(
      @Parameter(
              name = "claimId",
              description = "The CorpDB BNFT_CLAIM_ID",
              required = true,
              in = ParameterIn.PATH)
          @PathVariable("claimId")
          Long claimId);

  /**
   * GET /updates/{claimId}/lifecycle_status: Retrieves if the claim lifecycle status has updates.
   */
  @Operation(
      operationId = "postTempJurisdictionStation",
      summary = "Updates the temporary jurisdiction station.",
      description = "Updates the temporary jurisdiction station.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Success.",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = SuccessResponse.class))
            })
      })
  @RequestMapping(
      method = RequestMethod.POST,
      value = "/updates/{claimId}/temp_jurisdiction_station",
      produces = {"application/json"})
  ResponseEntity<SuccessResponse> postTempJurisdictionStation(
      @Parameter(
              name = "claimId",
              description = "The CorpDB BNFT_CLAIM_ID",
              required = true,
              in = ParameterIn.PATH)
          @PathVariable("claimId")
          Long claimId,
      @RequestBody TempJurisdictionStationRequest request);
}
