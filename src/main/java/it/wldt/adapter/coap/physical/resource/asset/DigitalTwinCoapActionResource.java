package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.asset.functions.CoapWldtActionFunction;
import it.wldt.adapter.coap.physical.resource.asset.functions.CoapWldtEventFunction;
import it.wldt.adapter.coap.physical.resource.asset.functions.CoapWldtPropertyFunction;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;

public class DigitalTwinCoapActionResource extends DigitalTwinCoapResource {
    private final CoapWldtActionFunction coapWldtActionFunction;

    public DigitalTwinCoapActionResource(String serverUrl, String relativeUri, CoapWldtPropertyFunction propertyFunction, CoapWldtActionFunction coapWldtActionFunction) {
        super(serverUrl, relativeUri, propertyFunction);
        this.coapWldtActionFunction = coapWldtActionFunction;
    }

    public DigitalTwinCoapActionResource(String serverUrl, String relativeUri, CoapWldtPropertyFunction propertyFunction, CoapWldtEventFunction errorFunction, CoapWldtActionFunction coapWldtActionFunction) {
        super(serverUrl, relativeUri, propertyFunction, errorFunction);
        this.coapWldtActionFunction = coapWldtActionFunction;
    }

    public byte[] applyActionFunction(PhysicalAssetActionWldtEvent<?> actionWldtEvent) {
        return coapWldtActionFunction.apply(actionWldtEvent);
    }

    public CoapWldtActionFunction getCoapActionFunction() {
        return coapWldtActionFunction;
    }
}
