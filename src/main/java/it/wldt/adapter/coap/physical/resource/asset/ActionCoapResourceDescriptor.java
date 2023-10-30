package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Collections;
import java.util.function.Function;

public class ActionCoapResourceDescriptor<T> extends DigitalTwinCoapResourceDescriptor {
    public ActionCoapResourceDescriptor(String serverUrl, String relativeUri, String propertyKey, Function<byte[], T> actionBodyProducer) {
        super(serverUrl, relativeUri, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetActionWldtEvent<>(propertyKey, actionBodyProducer.apply(payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }
}
