package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.Collections;
import java.util.function.Function;

public class CoapActuatorResourceDescriptor<P, A, E> extends DigitalTwinCoapActionResourceDescriptor {

    public CoapActuatorResourceDescriptor(String serverUrl, String relativeUri, String propertyKey, Function<byte[], P> propertyBodyProducer, Function<A, byte[]> actionBodyConsumer) {
        super(serverUrl, relativeUri, (payload, ct) -> {
            try {
                PhysicalAssetPropertyWldtEvent<?> propertyWldtEvent = new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyBodyProducer.apply(payload));
                propertyWldtEvent.setContentType(MediaTypeRegistry.toString(ct));

                return Collections.singletonList(propertyWldtEvent);
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }, actionWldtEvent -> actionBodyConsumer.apply((A) actionWldtEvent.getBody()));
    }

    public CoapActuatorResourceDescriptor(String serverUrl, String relativeUri, String propertyKey, Function<byte[], P> propertyBodyProducer, Function<String, E> eventBodyProducer, Function<A, byte[]> actionBodyConsumer) {
        super(serverUrl, relativeUri, (payload, ct) -> {
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
        }, actionWldtEvent -> actionBodyConsumer.apply((A) actionWldtEvent.getBody()));
    }
}
