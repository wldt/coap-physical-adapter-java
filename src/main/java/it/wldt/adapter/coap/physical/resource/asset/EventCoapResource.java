package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.exception.EventBusException;
import org.eclipse.californium.core.CoapClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

public class EventCoapResource<T> extends DigitalTwinCoapResource {
    public EventCoapResource(CoapClient client, String relativeUri, long autoUpdatePeriod, String propertyKey, Function<byte[], T> propertyValueProducer) {
        super(client, relativeUri, autoUpdatePeriod, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetEventWldtEvent<>(propertyKey, propertyValueProducer.apply(payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public EventCoapResource(CoapClient client, String relativeUri, boolean observable, String propertyKey, Function<byte[], T> propertyValueProducer) {
        super(client, relativeUri, observable, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetEventWldtEvent<>(propertyKey, propertyValueProducer.apply(payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
