package it.wldt.adapter.coap.physical.resources.assets;

import it.wldt.adapter.coap.physical.resources.assets.functions.CoapWldtActionFunction;
import it.wldt.adapter.coap.physical.resources.assets.functions.CoapWldtEventFunction;
import it.wldt.adapter.coap.physical.resources.assets.functions.CoapWldtPropertyFunction;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;

public class DigitalTwinActionResource extends DigitalTwinResource {
    private final CoapWldtActionFunction coapWldtActionFunction;
    private final String actionContentType;

    public DigitalTwinActionResource(String serverUrl, String relativeUri, CoapWldtPropertyFunction propertyFunction, CoapWldtActionFunction coapWldtActionFunction, String actionContentType) {
        super(serverUrl, relativeUri, propertyFunction);
        this.coapWldtActionFunction = coapWldtActionFunction;
        this.actionContentType = actionContentType;
    }

    public DigitalTwinActionResource(String serverUrl, String relativeUri, CoapWldtPropertyFunction propertyFunction, CoapWldtEventFunction errorFunction, CoapWldtActionFunction coapWldtActionFunction, String actionContentType) {
        super(serverUrl, relativeUri, propertyFunction, errorFunction);
        this.coapWldtActionFunction = coapWldtActionFunction;
        this.actionContentType = actionContentType;
    }

    public byte[] applyActionFunction(PhysicalAssetActionWldtEvent<?> actionWldtEvent) {
        return coapWldtActionFunction.apply(actionWldtEvent);
    }

    public CoapWldtActionFunction getCoapWldtActionFunction() {
        return coapWldtActionFunction;
    }

    public String getActionContentType() {
        return actionContentType;
    }
}
