package it.wldt.adapter.coap.physical.resources.assets.core;

import it.wldt.adapter.coap.physical.resources.assets.DigitalTwinActionResource;
import it.wldt.adapter.coap.physical.resources.assets.functions.methods.CustomPostRequestFunction;
import it.wldt.adapter.coap.physical.resources.assets.functions.methods.CustomPutRequestFunction;
import it.wldt.adapter.coap.physical.resources.assets.functions.preprocessing.ActionBodyConsumer;
import it.wldt.adapter.coap.physical.resources.assets.functions.preprocessing.EventBodyProducer;
import it.wldt.adapter.coap.physical.resources.assets.functions.preprocessing.PropertyBodyProducer;
import it.wldt.adapter.coap.physical.resources.methods.CoapPostSupport;
import it.wldt.adapter.coap.physical.resources.methods.CoapPutSupport;
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

public class DigitalTwinActuatorResource<P, E, A>
        extends DigitalTwinActionResource
        implements CoapPostSupport, CoapPutSupport {
    private CustomPostRequestFunction postRequestFunction;
    private CustomPutRequestFunction putRequestFunction;

    public DigitalTwinActuatorResource(String serverUrl, String relativeUri, String propertyKey, PropertyBodyProducer<P> propertyBodyProducer, ActionBodyConsumer<A> actionBodyConsumer) {
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
        setPropertyKey(propertyKey);
    }

    public DigitalTwinActuatorResource(String serverUrl, String relativeUri, String propertyKey, PropertyBodyProducer<P> propertyBodyProducer, EventBodyProducer<E> eventBodyProducer, ActionBodyConsumer<A> actionBodyConsumer) {
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
        setPropertyKey(propertyKey);
    }

    public CustomPostRequestFunction getPostRequestFunction() {
        return postRequestFunction;
    }

    public void setPostRequestFunction(CustomPostRequestFunction postRequestFunction) {
        this.postRequestFunction = postRequestFunction;
    }

    public CustomPutRequestFunction getPutRequestFunction() {
        return putRequestFunction;
    }

    public void setPutRequestFunction(CustomPutRequestFunction putRequestFunction) {
        this.putRequestFunction = putRequestFunction;
    }

    @Override
    public void sendPOST(byte[] payload, String ct) {
        if (postRequestFunction != null) {
            setLastEvent(postRequestFunction.send(this, payload, ct));
        } else if (payload != null && payload.length > 0) {
            setLastEvent("Body not supported for default POST operations");
        } else {
            Request request = getRequestOptionsBase(CoAP.Code.POST);

            try {
                CoapResponse response = client.advanced(request);

                if (response.getPayload() != null && response.getPayload().length > 0) {
                    setLastEvent(new String(response.getPayload()));
                }
            } catch (ConnectorException | IOException e) {
                setLastEvent(e.toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendPUT(byte[] payload, String ct) {
        if (putRequestFunction != null) {
            setLastEvent(putRequestFunction.send(this, payload, ct));
        } else if (payload == null || payload.length < 1) {
            setLastEvent("Body is necessary for default PUT operations");
        } else {
            Request request = getRequestOptionsBase(CoAP.Code.PUT);
            request.setPayload(payload);
            request.getOptions().setContentFormat(MediaTypeRegistry.parse(ct));

            try {
                CoapResponse response = client.advanced(request);

                if (response.getPayload() != null && response.getPayload().length > 0) {
                    setLastEvent(new String(response.getPayload()));
                }
            } catch (ConnectorException | IOException e) {
                setLastEvent(e.toString());
                e.printStackTrace();
            }
        }
    }
}
