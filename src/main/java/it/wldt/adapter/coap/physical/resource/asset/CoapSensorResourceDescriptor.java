package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.Collections;
import java.util.function.Function;

public class CoapSensorResourceDescriptor<P, E> extends DigitalTwinCoapResourceDescriptor {

    public CoapSensorResourceDescriptor(String serverUrl, String resourceUri, String propertyKey, Function<byte[], P> propertyBodyProducer) {
        super(serverUrl, resourceUri, (payload, ct) -> {
            try {

                // TODO: What if content type is not the payload ct but it is modified by the propertyBodyProducer?
                PhysicalAssetPropertyWldtEvent<?> propertyWldtEvent = new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyBodyProducer.apply(payload));
                propertyWldtEvent.setContentType(MediaTypeRegistry.toString(ct));

                return Collections.singletonList(propertyWldtEvent);
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }

    public CoapSensorResourceDescriptor(String serverUrl, String resourceUri, String propertyKey, Function<byte[], P> propertyBodyProducer, Function<String, E> eventBodyProducer) {
        super(serverUrl, resourceUri, (payload, ct) -> {
            try {
                PhysicalAssetPropertyWldtEvent<?> propertyWldtEvent = new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyBodyProducer.apply(payload));
                propertyWldtEvent.setContentType(MediaTypeRegistry.toString(ct));

                return Collections.singletonList(propertyWldtEvent);
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
