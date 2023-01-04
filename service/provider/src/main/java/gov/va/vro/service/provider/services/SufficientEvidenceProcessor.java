package gov.va.vro.service.provider.services;

import gov.va.vro.model.AbdEvidenceWithSummary;
import gov.va.vro.service.provider.mas.MasException;
import gov.va.vro.service.provider.mas.MasProcessingObject;
import gov.va.vro.service.spi.db.SaveToDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SufficientEvidenceProcessor implements Processor {

    private final SaveToDbService saveToDbService;

    @Override
    public void process(Exchange exchange) {
       MasProcessingObject payload = (MasProcessingObject) exchange.getProperty("payload");
       Boolean flag = (Boolean) exchange.getProperty("sufficientForFastTracking");
       String claimSubmissionId = payload.getClaimId();
       saveToDbService.updateSufficientEvidenceFlag(claimSubmissionId, flag);

    }

}
