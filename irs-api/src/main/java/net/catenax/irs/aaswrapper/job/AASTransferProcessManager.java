//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.component.ProcessingError;
import net.catenax.irs.component.Submodel;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.Endpoint;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import net.catenax.irs.connector.job.TransferProcessManager;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.exceptions.JsonParseException;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.util.JsonUtil;
import org.springframework.web.client.RestClientException;

/**
 * Process manager for AAS Object transfers.
 * Communicates with the AAS Wrapper.
 */
@Slf4j
@SuppressWarnings("PMD.DoNotUseThreads") // We want to use threads at the moment ;-)
public class AASTransferProcessManager implements TransferProcessManager<ItemDataRequest, AASTransferProcess> {

    private final DigitalTwinRegistryFacade registryFacade;

    private final SubmodelFacade submodelFacade;

    private final ExecutorService executor;

    private final BlobPersistence blobStore;

    public AASTransferProcessManager(final DigitalTwinRegistryFacade registryFacade,
            final SubmodelFacade submodelFacade, final ExecutorService executor, final BlobPersistence blobStore) {
        this.registryFacade = registryFacade;
        this.submodelFacade = submodelFacade;
        this.executor = executor;
        this.blobStore = blobStore;
    }

    @Override
    public TransferInitiateResponse initiateRequest(final ItemDataRequest dataRequest,
            final Consumer<String> preExecutionHandler, final Consumer<AASTransferProcess> completionCallback,
            final JobParameter jobData) {

        final String processId = UUID.randomUUID().toString();
        preExecutionHandler.accept(processId);

        executor.execute(getRunnable(dataRequest, completionCallback, processId, jobData));

        return new TransferInitiateResponse(processId, ResponseStatus.OK);
    }

    private Runnable getRunnable(final ItemDataRequest dataRequest,
            final Consumer<AASTransferProcess> transferProcessCompleted, final String processId,
            final JobParameter jobData) {

        return () -> {
            final AASTransferProcess aasTransferProcess = new AASTransferProcess(processId, dataRequest.getDepth());

            final String itemId = dataRequest.getItemId();

            final ItemContainer.ItemContainerBuilder itemContainerBuilder = ItemContainer.builder();

            log.info("Starting processing Digital Twin Registry with itemId {}", itemId);
            try {
                final AssetAdministrationShellDescriptor aasShell = registryFacade.getAAShellDescriptor(itemId,
                        jobData);
                final List<SubmodelDescriptor> aasSubmodelDescriptors = aasShell.getSubmodelDescriptors();

                log.info("Retrieved {} SubmodelDescriptor for itemId {}", aasSubmodelDescriptors.size(), itemId);

                aasShell.findAssemblyPartRelationshipEndpointAddresses().forEach(address -> {
                    try {
                        final AssemblyPartRelationshipDTO submodel = submodelFacade.getSubmodel(address, jobData);
                        processEndpoint(aasTransferProcess, itemContainerBuilder, submodel);
                    } catch (RestClientException | IllegalArgumentException e) {
                        log.info("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.",
                                address);
                        itemContainerBuilder.tombstone(createTombstone(itemId, address, e));
                    } catch (JsonParseException e) {
                        log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
                        itemContainerBuilder.tombstone(createTombstone(itemId, address, e));
                    }
                });
                final List<SubmodelDescriptor> filteredSubmodelDescriptorsByAspectType = aasShell.filterDescriptorsByAspectTypes(
                        jobData.getAspectTypes());

                if (jobData.isCollectAspects()) {
                    log.info("Collecting Submodels.");
                    collectSubmodels(filteredSubmodelDescriptorsByAspectType, itemContainerBuilder, itemId);
                }
                log.debug("Unfiltered SubmodelDescriptor: {}", aasSubmodelDescriptors);
                log.debug("Filtered SubmodelDescriptor: {}", filteredSubmodelDescriptorsByAspectType);

                itemContainerBuilder.shell(
                        aasShell.toBuilder().submodelDescriptors(filteredSubmodelDescriptorsByAspectType).build());
            } catch (RestClientException e) {
                log.info("Shell Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
                itemContainerBuilder.tombstone(createTombstone(itemId, null, e));
            }
            storeItemContainer(processId, itemContainerBuilder.build());

            transferProcessCompleted.accept(aasTransferProcess);
        };
    }

    private void collectSubmodels(final List<SubmodelDescriptor> submodelDescriptors,
            final ItemContainer.ItemContainerBuilder itemContainerBuilder, final String itemId) {
        submodelDescriptors.forEach(submodelDescriptor -> itemContainerBuilder.submodels(
                getSubmodels(submodelDescriptor, itemContainerBuilder, itemId)));
    }

    private List<Submodel> getSubmodels(final SubmodelDescriptor submodelDescriptor,
            final ItemContainer.ItemContainerBuilder itemContainerBuilder, final String itemId) {
        final List<Submodel> submodels = new ArrayList<>();
        submodelDescriptor.getEndpoints().forEach(endpoint -> {
            try {
                submodels.add(createSubmodel(submodelDescriptor, endpoint));
            } catch (JsonParseException e) {
                log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
                itemContainerBuilder.tombstone(
                        createTombstone(itemId, endpoint.getProtocolInformation().getEndpointAddress(), e));
            }
        });
        return submodels;
    }

    private Submodel createSubmodel(final SubmodelDescriptor submodelDescriptor, final Endpoint endpoint) {
        final String payload = requestSubmodelAsString(endpoint);
        return Submodel.builder().identification(submodelDescriptor.getIdentification()).aspectType(
                getAspectType(submodelDescriptor)).payload(payload).build();
    }

    private String getAspectType(final SubmodelDescriptor submodelDescriptor) {
        return submodelDescriptor.getSemanticId()
                                 .getValue()
                                 .stream()
                                 .findFirst()
                                 .orElse(null);
    }

    private String requestSubmodelAsString(final Endpoint endpoint) {
        return submodelFacade.getSubmodelAsString(endpoint.getProtocolInformation().getEndpointAddress());
    }

    private void storeItemContainer(final String processId, final ItemContainer itemContainer) {
        try {
            final JsonUtil jsonUtil = new JsonUtil();
            blobStore.putBlob(processId, jsonUtil.asString(itemContainer).getBytes(StandardCharsets.UTF_8));
        } catch (BlobPersistenceException e) {
            log.error("Unable to store AAS result", e);
        }
    }

    private Tombstone createTombstone(final String itemId, final String address, final Exception exception) {
        final ProcessingError processingError = ProcessingError.builder()
                                                               .withRetryCounter(0)
                                                               .withLastAttempt(ZonedDateTime.now(ZoneOffset.UTC))
                                                               .withErrorDetail(exception.getMessage())
                                                               .build();
        return Tombstone.builder().endpointURL(address).catenaXId(itemId).processingError(processingError).build();
    }

    private void processEndpoint(final AASTransferProcess aasTransferProcess,
            final ItemContainer.ItemContainerBuilder itemContainer, final AssemblyPartRelationshipDTO relationship) {
        log.info("Processing AssemblyPartRelationship with {} children", relationship.getChildParts().size());
        final List<String> childIds = relationship.getChildParts()
                                                  .stream()
                                                  .map(ChildDataDTO::getChildCatenaXId)
                                                  .collect(Collectors.toList());
        aasTransferProcess.addIdsToProcess(childIds);
        // TODO (jkreutzfeld) what do we actually need to store here?
        itemContainer.assemblyPartRelationship(relationship);
    }

}
