package it.wldt.adapter.coap.physical.utils;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.*;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class CoapTestShadowingFunction extends ShadowingFunction {
    private static final Logger logger = LoggerFactory.getLogger(CoapTestShadowingFunction.class);

    public CoapTestShadowingFunction() {
        super("coap-test-shadowing-function");
    }

    @Override
    protected void onCreate() {
        logger.debug("Shadowing - OnStart");
    }

    @Override
    protected void onStart() {
        logger.debug("Shadowing - OnStart");
    }

    @Override
    protected void onStop() {
        logger.debug("Shadowing - OnStop");
    }

    @Override
    protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        logger.debug("Shadowing - onDtBound");
        startShadowing(adaptersPhysicalAssetDescriptionMap);
        try {
            this.observePhysicalAssetProperties(adaptersPhysicalAssetDescriptionMap.values()
                    .stream()
                    .flatMap(pad -> pad.getProperties().stream())
                    .collect(Collectors.toList()));
            //observes all the available events
            this.observePhysicalAssetEvents(adaptersPhysicalAssetDescriptionMap.values()
                    .stream()
                    .flatMap(pad -> pad.getEvents().stream())
                    .collect(Collectors.toList()));
            this.observeDigitalActionEvents();
        } catch (EventBusException | ModelException e) {
            e.printStackTrace();
        }
    }

    private void startShadowing(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        adaptersPhysicalAssetDescriptionMap.forEach((id, pad) -> {
            pad.getProperties()
                    .forEach(p -> {
                        try {
                            this.digitalTwinStateManager.startStateTransaction();
                            this.digitalTwinStateManager.createProperty(new DigitalTwinStateProperty<>(p.getKey(), p.getInitialValue()));
                            this.digitalTwinStateManager.commitStateTransaction();
                        } catch (WldtDigitalTwinStateException e) {
                            e.printStackTrace();
                        }
                    });
            pad.getActions().forEach(a -> {
                try {
                    this.digitalTwinStateManager.startStateTransaction();
                    this.digitalTwinStateManager.enableAction(new DigitalTwinStateAction(a.getKey(), a.getType(), a.getContentType()));
                    this.digitalTwinStateManager.commitStateTransaction();
                } catch (WldtDigitalTwinStateException e) {
                    e.printStackTrace();
                }
            });
            pad.getEvents().forEach(e -> {
                try {
                    this.digitalTwinStateManager.startStateTransaction();
                    this.digitalTwinStateManager.registerEvent(new DigitalTwinStateEvent(e.getKey(), e.getType()));
                    this.digitalTwinStateManager.commitStateTransaction();
                } catch (WldtDigitalTwinStateException ex) {
                    ex.printStackTrace();
                }
            });
            notifyShadowingSync();
        });
    }

    @Override
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> map, String errorMessage) {
        logger.debug("Shadowing - onDTUnBound - error: {} ", errorMessage);
    }

    @Override
    protected void onPhysicalAdapterBidingUpdate(String adapterId, PhysicalAssetDescription adapterPhysicalAssetDescription) {
        logger.info("Shadowing - onPABindingUpdate - updated Adapter: {}, new PAD: {}", adapterId, adapterPhysicalAssetDescription);
    }

    @Override
    protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage) {
        logger.info("Shadowing - onPAPropertyVariation - property event: {} ", physicalPropertyEventMessage);
        //Update Digital Twin Status
        try {
            this.digitalTwinStateManager.startStateTransaction();
            this.digitalTwinStateManager.updateProperty(
                    new DigitalTwinStateProperty<>(
                            physicalPropertyEventMessage.getPhysicalPropertyId(),
                            physicalPropertyEventMessage.getBody()));
            this.digitalTwinStateManager.commitStateTransaction();
        } catch (WldtDigitalTwinStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        logger.info("Shadowing - onPhysicalAssetEventNotification - received Event:{}", physicalAssetEventWldtEvent);
        try {
            this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(
                    physicalAssetEventWldtEvent.getPhysicalEventKey(),
                    (String) physicalAssetEventWldtEvent.getBody(),
                    physicalAssetEventWldtEvent.getCreationTimestamp()));
        } catch (WldtDigitalTwinStateEventNotificationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipInstanceCreatedWldtEvent) {

    }

    @Override
    protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipInstanceDeletedWldtEvent) {

    }

    @Override
    protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {
        logger.info("Shadowing - onDigitalActionEvent - Received:{}", digitalActionWldtEvent);
        try {
            /*
            if (!this.digitalTwinState.containsAction(digitalActionWldtEvent.getActionKey()))
                this.digitalTwinState.enableAction(new DigitalTwinStateAction(digitalActionWldtEvent.getActionKey(), digitalActionWldtEvent.getType(), digitalActionWldtEvent.getContentType()));
             */
            publishPhysicalAssetActionWldtEvent(
                    digitalActionWldtEvent.getActionKey(),
                    digitalActionWldtEvent.getBody());
            logger.info("Shadowing - onDigitalActionEvent - Published");
        } catch (EventBusException e) {
            logger.error("Shadowing - onDigitalActionEvent - Error publishing");
            e.printStackTrace();
        }
    }
}
