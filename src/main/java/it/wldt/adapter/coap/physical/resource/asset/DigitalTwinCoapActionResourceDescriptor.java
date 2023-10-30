package it.wldt.adapter.coap.physical.resource.asset;

import it.wldt.adapter.coap.physical.resource.asset.functions.CoapActionFunction;
import it.wldt.adapter.coap.physical.resource.asset.functions.CoapEventFunction;
import it.wldt.adapter.coap.physical.resource.asset.functions.CoapPropertyFunction;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.event.WldtEvent;

import java.util.Collections;
import java.util.List;

public class DigitalTwinCoapActionResourceDescriptor extends DigitalTwinCoapResourceDescriptor{
    private final CoapActionFunction coapActionFunction;

    public DigitalTwinCoapActionResourceDescriptor(String serverUrl, String relativeUri, CoapPropertyFunction propertyFunction, CoapActionFunction coapActionFunction) {
        super(serverUrl, relativeUri, propertyFunction);
        this.coapActionFunction = coapActionFunction;
    }

    public DigitalTwinCoapActionResourceDescriptor(String serverUrl, String relativeUri, CoapPropertyFunction propertyFunction, CoapEventFunction errorFunction, CoapActionFunction coapActionFunction) {
        super(serverUrl, relativeUri, propertyFunction, errorFunction);
        this.coapActionFunction = coapActionFunction;
    }

    public String applyPayloadFunction(PhysicalAssetActionWldtEvent<?> actionWldtEvent) {
        return coapActionFunction.apply(actionWldtEvent);
    }

    public CoapActionFunction getCoapActionFunction() {
        return coapActionFunction;
    }
}
