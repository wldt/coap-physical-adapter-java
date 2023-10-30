package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Collections;
import java.util.function.Function;

/**
 * An extension of a {@code DigitalTwinCoapResourceDescriptor} class whose payload function returns a list of {@code PhysicalAssetPropertyWldtEvent}
 *
 * @see DigitalTwinCoapResourceDescriptor
 * @see CoapPayloadFunction
 * @see WldtEvent
 * @see PhysicalAssetPropertyWldtEvent
 */
public class PropertyCoapResourceDescriptor<T> extends DigitalTwinCoapResourceDescriptor {

    public PropertyCoapResourceDescriptor(String serverUrl, String relativeUri, String propertyKey, Function<byte[], T> propertyBodyProducer) {
        super(serverUrl, relativeUri, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyBodyProducer.apply(payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }

}
