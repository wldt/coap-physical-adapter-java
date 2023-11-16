package it.wldt.adapter.coap.physical.resources.assets;

import it.wldt.adapter.coap.physical.resources.CoapResourceDescriptor;
import it.wldt.adapter.coap.physical.resources.assets.functions.CoapWldtEventFunction;
import it.wldt.adapter.coap.physical.resources.assets.functions.CoapWldtPropertyFunction;
import it.wldt.core.event.WldtEvent;

import java.util.Collections;
import java.util.List;

/**
 * Represents a CoAP resource in a WLDT Digital Twin context.
 * It makes possible to apply a custom function to the incoming payloads.
 *
 * @see CoapResourceDescriptor
 * @see CoapWldtPropertyFunction
 * @see WldtEvent
 */
public class DigitalTwinResource extends CoapResourceDescriptor {
    private final CoapWldtPropertyFunction coapWldtPropertyFunction;
    private final CoapWldtEventFunction coapWldtEventFunction;

    public DigitalTwinResource(String serverUrl, String relativeUri, CoapWldtPropertyFunction function) {
        super(serverUrl, relativeUri, false);
        this.coapWldtPropertyFunction = function;
        this.coapWldtEventFunction = null;
    }

    public DigitalTwinResource(String serverUrl, String relativeUri, CoapWldtPropertyFunction payloadFunction, CoapWldtEventFunction errorFunction) {
        super(serverUrl, relativeUri, true);
        this.coapWldtPropertyFunction = payloadFunction;
        this.coapWldtEventFunction = errorFunction;
    }

    public List<WldtEvent<?>> applyPropertyFunction(byte[] payload) {
        return coapWldtPropertyFunction.apply(payload, lastPayloadContentType);
    }

    public List<WldtEvent<?>> applyEventFunction(String message) {
        if (this.coapWldtEventFunction != null) {
            return coapWldtEventFunction.apply(message);
        }
        return Collections.emptyList();
    }

    public CoapWldtPropertyFunction getPropertyFunction() {
        return coapWldtPropertyFunction;
    }

    public CoapWldtEventFunction getEventFunction() {
        return coapWldtEventFunction;
    }
}
