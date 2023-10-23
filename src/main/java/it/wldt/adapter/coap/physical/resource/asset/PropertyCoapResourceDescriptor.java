package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.asset.payload.CoapBytePayloadFunction;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapStringPayloadFunction;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Collections;
import java.util.List;

/**
 * An extension of a {@code DigitalTwinCoapResourceDescriptor} class whose payload function returns a list of {@code PhysicalAssetPropertyWldtEvent}
 *
 * @see DigitalTwinCoapResourceDescriptor
 * @see CoapPayloadFunction
 * @see WldtEvent
 * @see PhysicalAssetPropertyWldtEvent
 */
public class PropertyCoapResourceDescriptor extends DigitalTwinCoapResourceDescriptor {

    public PropertyCoapResourceDescriptor(String serverUrl, String relativeUri, long autoUpdatePeriod, String propertyKey, CoapPayloadFunction<?> propertyValueProducer) {
        super(serverUrl, relativeUri, autoUpdatePeriod, payload -> PropertyCoapResourceDescriptor.applyFunction(payload, propertyKey, propertyValueProducer));
    }

    public PropertyCoapResourceDescriptor(String serverUrl, String relativeUri, boolean observable, String propertyKey, CoapPayloadFunction<?> propertyValueProducer) {
        super(serverUrl, relativeUri, observable, payload -> PropertyCoapResourceDescriptor.applyFunction(payload, propertyKey, propertyValueProducer));
    }

    private static List<WldtEvent<?>> applyFunction(Object payload, String propertyKey, CoapPayloadFunction<?> propertyValueProducer) {
        try {
            // TODO: Fix the conversion from Object to String to byte[]

            if (propertyValueProducer instanceof CoapStringPayloadFunction)
                return Collections.singletonList(new PhysicalAssetPropertyWldtEvent<>(propertyKey, ((CoapStringPayloadFunction)propertyValueProducer).apply(String.valueOf(payload))));
            return Collections.singletonList(new PhysicalAssetPropertyWldtEvent<>(propertyKey, ((CoapBytePayloadFunction)propertyValueProducer).apply(String.valueOf(payload).getBytes())));
        } catch (EventBusException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
