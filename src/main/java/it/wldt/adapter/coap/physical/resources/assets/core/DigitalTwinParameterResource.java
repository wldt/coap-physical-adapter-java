package it.wldt.adapter.coap.physical.resources.assets.core;

import it.wldt.adapter.coap.physical.resources.assets.DigitalTwinActionResource;
import it.wldt.adapter.coap.physical.resources.assets.functions.preprocessing.ActionBodyConsumer;
import it.wldt.adapter.coap.physical.resources.assets.functions.preprocessing.EventBodyProducer;
import it.wldt.adapter.coap.physical.resources.assets.functions.preprocessing.PropertyBodyProducer;
import it.wldt.adapter.coap.physical.resources.methods.CoapPutMethod;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.util.Collections;

/**
 *
 * @param <P>
 * @param <E>
 * @param <A>
 */
public class DigitalTwinParameterResource<P, E, A>
        extends DigitalTwinActionResource
        implements CoapPutMethod {
    // TODO: Custom PUT method

    public DigitalTwinParameterResource(String serverUrl, String relativeUri, String propertyKey, PropertyBodyProducer<P> propertyBodyProducer, ActionBodyConsumer<A> actionBodyConsumer) {
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
        }, actionWldtEvent -> actionBodyConsumer.getConsumer().apply((A) actionWldtEvent.getBody()),
                actionBodyConsumer.getContentMimeType());
    }

    public DigitalTwinParameterResource(String serverUrl, String relativeUri, String propertyKey, PropertyBodyProducer<P> propertyBodyProducer, EventBodyProducer<E> eventBodyProducer, ActionBodyConsumer<A> actionBodyConsumer) {
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
        }, actionWldtEvent -> actionBodyConsumer.getConsumer().apply((A) actionWldtEvent.getBody()),
                actionBodyConsumer.getContentMimeType());
    }


    @Override
    public void sendPUT(byte[] payload, String ct) {
        // TODO: Custom PUT

        if (payload == null || payload.length < 1) {
            setLastEvent("Body is necessary for default PUT operations");
            return;
        }

        Request request = getRequestOptionsBase(CoAP.Code.PUT);
        request.setPayload(payload);
        request.getOptions().setContentFormat(MediaTypeRegistry.parse(ct));

        try {
            CoapResponse response = client.advanced(request);

            if (response == null) {
                setLastEvent("Response is null");
            } else if (!response.isSuccess()) {
                setLastEvent("Response code: " + response.getCode());
            } else if (response.getPayload() != null && response.getPayload().length > 0) {
                setLastEvent(new String(response.getPayload()));
            } else {
                setLastEvent("Response code: " + response.getCode());
            }
        } catch (ConnectorException | IOException e) {
            setLastEvent(e.toString());
            e.printStackTrace();
        }
    }
}
