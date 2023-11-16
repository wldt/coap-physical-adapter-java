package it.wldt.adapter.coap.physical.resources.assets.core.interfaces;

import it.wldt.adapter.coap.physical.resources.assets.DigitalTwinCoapActionResource;
import it.wldt.adapter.coap.physical.resources.assets.functions.body.ActionBodyConsumer;
import it.wldt.adapter.coap.physical.resources.assets.functions.body.EventBodyProducer;
import it.wldt.adapter.coap.physical.resources.assets.functions.body.PropertyBodyProducer;
import it.wldt.adapter.coap.physical.resources.methods.CoapPostMethod;
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

public class CoapCoreActuator <P, E, A>
        extends DigitalTwinCoapActionResource
        implements CoapPostMethod, CoapPutMethod {
    // TODO: Custom POST and PUT methods

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
    public void sendPOST(byte[] payload, String ct) {
        // TODO: Custom POST function

        if (payload != null && payload.length > 0) {
            setLastEvent("Body not supported for default POST operations");
            return;
        }

        Request request = getRequestOptionsBase(CoAP.Code.POST);

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
