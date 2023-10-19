package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.CoapResource;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapBytePayloadFunction;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapStringPayloadFunction;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;
import org.eclipse.californium.core.CoapClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class PropertyCoapResource extends DigitalTwinCoapResource {

    public PropertyCoapResource(String serverUrl, String relativeUri, long autoUpdatePeriod, String propertyKey, CoapPayloadFunction<?> propertyValueProducer) {
        super(serverUrl, relativeUri, autoUpdatePeriod, payload -> PropertyCoapResource.applyFunction(payload, propertyKey, propertyValueProducer));
    }

    public PropertyCoapResource(String serverUrl, String relativeUri, boolean observable, String propertyKey, CoapPayloadFunction<?> propertyValueProducer) {
        super(serverUrl, relativeUri, observable, payload -> PropertyCoapResource.applyFunction(payload, propertyKey, propertyValueProducer));
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
