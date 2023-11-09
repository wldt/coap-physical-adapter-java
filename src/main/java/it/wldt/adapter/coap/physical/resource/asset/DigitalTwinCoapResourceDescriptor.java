package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.CoapResourceDescriptor;
import it.wldt.adapter.coap.physical.resource.asset.functions.CoapEventFunction;
import it.wldt.adapter.coap.physical.resource.asset.functions.CoapPropertyFunction;
import it.wldt.core.event.WldtEvent;

import java.util.Collections;
import java.util.List;

/**
 * Represents a CoAP resource in a WLDT Digital Twin context.
 * It makes possible to apply a custom function to the incoming payloads.
 *
 * @see CoapResourceDescriptor
 * @see CoapPropertyFunction
 * @see WldtEvent
 */
public class DigitalTwinCoapResourceDescriptor extends CoapResourceDescriptor {
    private final CoapPropertyFunction coapPropertyFunction;
    private final CoapEventFunction coapEventFunction;

    public DigitalTwinCoapResourceDescriptor(String serverUrl, String relativeUri, CoapPropertyFunction function) {
        super(serverUrl, relativeUri, false);
        this.coapPropertyFunction = function;
        this.coapEventFunction = null;
    }

    public DigitalTwinCoapResourceDescriptor(String serverUrl, String relativeUri, CoapPropertyFunction payloadFunction, CoapEventFunction errorFunction) {
        super(serverUrl, relativeUri, true);
        this.coapPropertyFunction = payloadFunction;
        this.coapEventFunction = errorFunction;
    }

    public List<WldtEvent<?>> applyPayloadFunction(byte[] payload) {
        return coapPropertyFunction.apply(payload, lastPayloadContentType);
    }

    public List<WldtEvent<?>> applyErrorFunction(String message) {
        if (this.coapEventFunction != null) {
            return coapEventFunction.apply(message);
        }
        return Collections.emptyList();
    }

    public CoapPropertyFunction getPayloadFunction() {
        return coapPropertyFunction;
    }

    public CoapEventFunction getCoapErrorFunction() {
        return coapEventFunction;
    }
}
