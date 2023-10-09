package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.CoapResource;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;
import org.eclipse.californium.core.CoapClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

public class PropertyCoapResource<T> extends DigitalTwinCoapResource {

    public PropertyCoapResource(String serverUrl, String relativeUri, long autoUpdatePeriod, String propertyKey, Function<byte[], T> propertyValueProducer) {
        super(serverUrl, relativeUri, autoUpdatePeriod, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyValueProducer.apply(payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public PropertyCoapResource(String serverUrl, String relativeUri, boolean observable, String propertyKey, Function<byte[], T> propertyValueProducer) {
        super(serverUrl, relativeUri, observable, payload -> {
            try {
                return Collections.singletonList(new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyValueProducer.apply(payload)));
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
