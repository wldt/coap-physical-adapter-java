package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.CoapResourceDescriptor;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.core.event.WldtEvent;

import java.util.List;

/**
 * Represents a CoAP resource in a WLDT Digital Twin context.
 * It makes possible to apply a custom function to the incoming payloads.
 *
 * @see CoapResourceDescriptor
 * @see CoapPayloadFunction
 * @see WldtEvent
 */
public class DigitalTwinCoapResourceDescriptor extends CoapResourceDescriptor {
    private final CoapPayloadFunction coapPayloadFunction;

    public DigitalTwinCoapResourceDescriptor(String serverUrl, String relativeUri, CoapPayloadFunction function) {
        super(serverUrl, relativeUri);
        this.coapPayloadFunction = function;
    }

    public List<WldtEvent<?>> applyPayloadFunction(byte[] payload) {
        return coapPayloadFunction.apply(payload);
    }

    public CoapPayloadFunction getPayloadFunction() {
        return coapPayloadFunction;
    }
}
