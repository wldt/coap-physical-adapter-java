package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.CoapResource;
import it.wldt.core.event.WldtEvent;
import org.eclipse.californium.core.CoapClient;

import java.util.List;

public class DigitalTwinCoapResource extends CoapResource {
    private final CoapPayloadFunction coapPayloadFunction;

    public DigitalTwinCoapResource(String serverUrl, String relativeUri, long autoUpdatePeriod, CoapPayloadFunction function) {
        super(serverUrl, relativeUri, autoUpdatePeriod);
        this.coapPayloadFunction = function;
    }

    public DigitalTwinCoapResource(String serverUrl, String relativeUri, boolean observable, CoapPayloadFunction function) {
        super(serverUrl, relativeUri, observable);
        this.coapPayloadFunction = function;
    }

    public List<WldtEvent<?>> applyPayloadFunction(byte[] payload) {
        return coapPayloadFunction.apply(payload);
    }

    public CoapPayloadFunction getPayloadFunction() {
        return coapPayloadFunction;
    }
}
