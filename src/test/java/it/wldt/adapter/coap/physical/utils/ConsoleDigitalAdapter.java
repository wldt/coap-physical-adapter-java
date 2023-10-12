package it.wldt.adapter.coap.physical.utils;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtDigitalTwinStateEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConsoleDigitalAdapter extends DigitalAdapter<String> {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleDigitalAdapter.class);

    public ConsoleDigitalAdapter() {
        super("console-digital-adapter", "default");
    }

    @Override
    protected void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {

    }

    @Override
    protected void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DA({}) - OnStateChangePropertyUpdate - property: {}", this.getId(), digitalTwinStateProperty);
    }

    @Override
    protected void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {

    }

    @Override
    protected void onStatePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DA({}) - OnStatePropertyUpdate - property:{}", this.getId(), digitalTwinStateProperty);
    }

    @Override
    protected void onStatePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {

    }

    @Override
    protected void onStateChangeActionEnabled(DigitalTwinStateAction digitalTwinStateAction) {

    }

    @Override
    protected void onStateChangeActionUpdated(DigitalTwinStateAction digitalTwinStateAction) {

    }

    @Override
    protected void onStateChangeActionDisabled(DigitalTwinStateAction digitalTwinStateAction) {

    }

    @Override
    protected void onStateChangeEventRegistered(DigitalTwinStateEvent digitalTwinStateEvent) {
        logger.info("DA({}) - onStateChangeEventRegistered - event: {} ", this.getId(), digitalTwinStateEvent);
    }

    @Override
    protected void onStateChangeEventRegistrationUpdated(DigitalTwinStateEvent digitalTwinStateEvent) {

    }

    @Override
    protected void onStateChangeEventUnregistered(DigitalTwinStateEvent digitalTwinStateEvent) {

    }

    @Override
    protected void onDigitalTwinStateEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.info("DA({}) - onEventNotificationReceived - event: {} ", this.getId(), digitalTwinStateEventNotification.getBody());
    }

    @Override
    protected void onStateChangeRelationshipCreated(DigitalTwinStateRelationship<?> digitalTwinStateRelationship) {

    }

    @Override
    protected void onStateChangeRelationshipInstanceCreated(DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance) {

    }

    @Override
    protected void onStateChangeRelationshipDeleted(DigitalTwinStateRelationship<?> digitalTwinStateRelationship) {

    }

    @Override
    protected void onStateChangeRelationshipInstanceDeleted(DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance) {

    }

    @Override
    public void onAdapterStart() {
        logger.debug("DA({}) - onAdapterStart", this.getId());
    }

    @Override
    public void onAdapterStop() {

    }

    @Override
    public void onDigitalTwinSync(IDigitalTwinState iDigitalTwinState) {
        logger.debug("DA({}) - onDTSync - state: {}", this.getId(), digitalTwinState);
        try {
            List<String> eventsKeys = digitalTwinState.getEventList()
                    .orElse(new ArrayList<>()).stream().map(DigitalTwinStateEvent::getKey)
                    .collect(Collectors.toList());
            this.observeDigitalTwinEventsNotifications(eventsKeys);
        } catch (EventBusException | WldtDigitalTwinStateEventException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDigitalTwinUnSync(IDigitalTwinState iDigitalTwinState) {
        logger.info("DA({}) - onDTUnSync - state: {}", this.getId(), digitalTwinState);
    }

    @Override
    public void onDigitalTwinCreate() {

    }

    @Override
    public void onDigitalTwinStart() {

    }

    @Override
    public void onDigitalTwinStop() {

    }

    @Override
    public void onDigitalTwinDestroy() {

    }

    public <T> void invokeAction(String actionKey, T body){
        try {
            publishDigitalActionWldtEvent(actionKey, body);
        } catch (EventBusException e) {
            e.printStackTrace();
        }
    }
}
