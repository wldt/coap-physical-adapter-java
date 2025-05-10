package it.wldt.adapter.coap.physical.utils;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.core.state.*;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtDigitalTwinStateEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class ConsoleDigitalAdapter extends DigitalAdapter<String> {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleDigitalAdapter.class);

    public ConsoleDigitalAdapter() {
        super("console-digital-adapter", "default");
    }

    @Override
    protected void onStateUpdate(DigitalTwinState newDigitalTwinState, DigitalTwinState previousDigitalTwinState, ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList) {
        logger.info("{} - State update: {}", super.getId(), newDigitalTwinState);
    }

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.info("{} - Event notification: {}", super.getId(), digitalTwinStateEventNotification);
    }

    @Override
    public void onAdapterStart() {
        logger.debug("DA({}) - onAdapterStart", this.getId());
        notifyDigitalAdapterBound();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("Digital Adapter invoking action with body=15");
                // Test for Adapter's PUT actions
                invokeAction("iot.actuator.temperature.temperature-actuator", "15", "text/plain");
            }
        }, 0, 3000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("Digital Adapter invoking action with body=25");
                // Test for Adapter's PUT actions
                invokeAction("iot.actuator.temperature.temperature-actuator", "25", "text/plain");
            }
        }, 0, 5000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("Digital Adapter invoking action with empty body");
                // Test for Adapter's POST actions
                invokeAction("iot.actuator.temperature.temperature-actuator", "", "text/plain");
            }
        }, 0, 7000);
    }

    @Override
    public void onAdapterStop() {

    }

    @Override
    public void onDigitalTwinSync(DigitalTwinState iDigitalTwinState) {
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
    public void onDigitalTwinUnSync(DigitalTwinState iDigitalTwinState) {
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

    public <T> void invokeAction(String actionKey, T body, String contentType){
        try {
            DigitalActionWldtEvent<T> event = new DigitalActionWldtEvent<>(actionKey, body);
            event.setContentType(contentType);

            publishDigitalActionWldtEvent(event);
        } catch (EventBusException e) {
            e.printStackTrace();
        }
    }
}
