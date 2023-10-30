package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Collections;
import java.util.function.Function;

public class CoapSensorResourceDescriptor<P, E> extends DigitalTwinCoapResourceDescriptor {

    public CoapSensorResourceDescriptor(String serverUrl, String resourceUri, String propertyKey, Function<byte[], P> propertyBodyProducer) {
        super(serverUrl, resourceUri, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyBodyProducer.apply(payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }

    public CoapSensorResourceDescriptor(String serverUrl, String resourceUri, String propertyKey, Function<byte[], P> propertyBodyProducer, Function<String, E> eventBodyProducer) {
        super(serverUrl, resourceUri, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyBodyProducer.apply(payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }, errorMessage -> {
            try {
                return Collections.singletonList(new PhysicalAssetEventWldtEvent<>(propertyKey, eventBodyProducer.apply(errorMessage)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }
}
