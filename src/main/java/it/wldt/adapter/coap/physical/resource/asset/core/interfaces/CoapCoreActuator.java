package it.wldt.adapter.coap.physical.resource.asset.core.interfaces;

import it.wldt.adapter.coap.physical.resource.CoapResourceDescriptor;
import it.wldt.adapter.coap.physical.resource.asset.DigitalTwinCoapActionResource;
import it.wldt.adapter.coap.physical.resource.asset.functions.body.ActionBodyConsumer;
import it.wldt.adapter.coap.physical.resource.asset.functions.body.EventBodyProducer;
import it.wldt.adapter.coap.physical.resource.asset.functions.body.PropertyBodyProducer;
import it.wldt.adapter.coap.physical.resource.methods.CoapPostMethod;
import it.wldt.adapter.coap.physical.resource.methods.CoapPutMethod;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.Collections;
import java.util.function.Function;

public class CoapCoreActuator <P, E, A>
        extends DigitalTwinCoapActionResource
        implements CoapPostMethod, CoapPutMethod {

    public CoapCoreActuator(String serverUrl, String relativeUri, String propertyKey, PropertyBodyProducer<P> propertyBodyProducer, ActionBodyConsumer<A> actionBodyConsumer) {
        super(serverUrl, relativeUri, (payload, ct) -> {
            try {
                PhysicalAssetPropertyWldtEvent<?> propertyWldtEvent = new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyBodyProducer.getProducer().apply(payload));

                String finalContentType = propertyBodyProducer.getContentMimeType();
                if (finalContentType == null || finalContentType.isBlank()) {
                    finalContentType = MediaTypeRegistry.toString(ct);
                }
                propertyWldtEvent.setContentType(finalContentType);

                return Collections.singletonList(propertyWldtEvent);
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }, actionWldtEvent -> actionBodyConsumer.getConsumer().apply((A) actionWldtEvent.getBody()));
    }

    public CoapCoreActuator(String serverUrl, String relativeUri, String propertyKey, PropertyBodyProducer<P> propertyBodyProducer, EventBodyProducer<E> eventBodyProducer, ActionBodyConsumer<A> actionBodyConsumer) {
        super(serverUrl, relativeUri, (payload, ct) -> {
            try {
                PhysicalAssetPropertyWldtEvent<?> propertyWldtEvent = new PhysicalAssetPropertyWldtEvent<>(propertyKey, propertyBodyProducer.getProducer().apply(payload));

                String finalContentType = propertyBodyProducer.getContentMimeType();
                if (finalContentType == null || finalContentType.isBlank()) {
                    finalContentType = MediaTypeRegistry.toString(ct);
                }
                propertyWldtEvent.setContentType(finalContentType);

                return Collections.singletonList(propertyWldtEvent);
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }, errorMessage -> {
            try {
                PhysicalAssetEventWldtEvent<?> eventWldtEvent = new PhysicalAssetEventWldtEvent<>(propertyKey, eventBodyProducer.getProducer().apply(errorMessage));

                String finalContentType = eventBodyProducer.getContentMimeType();
                if (finalContentType == null || finalContentType.isBlank()) {
                    finalContentType = MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN);
                }

                eventWldtEvent.setContentType(finalContentType);

                return Collections.singletonList(eventWldtEvent);
            } catch (EventBusException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }, actionWldtEvent -> actionBodyConsumer.getConsumer().apply((A) actionWldtEvent.getBody()));
    }

    @Override
    public void sendPOST() {
        // TODO
    }

    @Override
    public void sendPUT(byte[] payload) {
        // TODO
    }
}
