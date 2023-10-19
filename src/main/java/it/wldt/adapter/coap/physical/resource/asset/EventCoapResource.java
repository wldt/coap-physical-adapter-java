package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.asset.payload.CoapBytePayloadFunction;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapStringPayloadFunction;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;
import org.eclipse.californium.core.CoapClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class EventCoapResource extends DigitalTwinCoapResource {
    public EventCoapResource(String serverUrl, String relativeUri, long autoUpdatePeriod, String propertyKey, CoapPayloadFunction<?> propertyValueProducer) {
        super(serverUrl, relativeUri, autoUpdatePeriod, payload -> EventCoapResource.applyFunction(payload, propertyKey, propertyValueProducer));
    }

    public EventCoapResource(String serverUrl, String relativeUri, boolean observable, String propertyKey, CoapPayloadFunction<?> propertyValueProducer) {
        super(serverUrl, relativeUri, observable, payload -> EventCoapResource.applyFunction(payload, propertyKey, propertyValueProducer));
    }

    private static <T> List<WldtEvent<?>> applyFunction(Object payload, String propertyKey, CoapPayloadFunction<T> propertyValueProducer) {
        try {
            // TODO: Fix the conversion from Object to String to byte[]

            if (propertyValueProducer instanceof CoapStringPayloadFunction)
                return Collections.singletonList(new PhysicalAssetEventWldtEvent<>(propertyKey, ((CoapStringPayloadFunction)propertyValueProducer).apply(String.valueOf(payload))));
            return Collections.singletonList(new PhysicalAssetEventWldtEvent<>(propertyKey, ((CoapBytePayloadFunction)propertyValueProducer).apply(String.valueOf(payload).getBytes())));
        } catch (EventBusException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
