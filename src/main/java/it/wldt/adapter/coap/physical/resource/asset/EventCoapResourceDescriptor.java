package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Collections;
import java.util.function.Function;

/**
 * An extension of a {@code DigitalTwinCoapResourceDescriptor} class whose payload function returns a list of {@code PhysicalAssetEventWldtEvent}
 *
 * @see DigitalTwinCoapResourceDescriptor
 * @see CoapPayloadFunction
 * @see WldtEvent
 * @see PhysicalAssetEventWldtEvent
 */
public class EventCoapResourceDescriptor<T> extends DigitalTwinCoapResourceDescriptor {
    public EventCoapResourceDescriptor(String serverUrl, String relativeUri, String propertyKey, Function<byte[], T> eventBodyProducer) {
        super(serverUrl, relativeUri, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetEventWldtEvent<>(propertyKey, eventBodyProducer.apply((byte[]) payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }
}
