package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Collections;
import java.util.function.Function;

public class ActionCoapResourceDescriptor<T> extends DigitalTwinCoapResourceDescriptor {
    public ActionCoapResourceDescriptor(String serverUrl, String relativeUri, long autoUpdatePeriod, String propertyKey, Function<byte[], T> eventBodyProducer) {
        super(serverUrl, relativeUri, autoUpdatePeriod, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetActionWldtEvent<>(propertyKey, eventBodyProducer.apply((byte[]) payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }

    public ActionCoapResourceDescriptor(String serverUrl, String relativeUri, boolean observable, String propertyKey, Function<byte[], T> eventBodyProducer) {
        super(serverUrl, relativeUri, observable, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetActionWldtEvent<>(propertyKey, eventBodyProducer.apply((byte[]) payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }
}
