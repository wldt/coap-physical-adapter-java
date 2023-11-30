package it.wldt.adapter.coap.physical.resources.assets.functions.methods;

import it.wldt.adapter.coap.physical.resources.assets.DigitalTwinActionResource;
import it.wldt.adapter.coap.physical.resources.methods.CoapPostSupport;

@FunctionalInterface
public interface CustomPostRequestFunction {
    String send(DigitalTwinActionResource resource, byte[] payload, String ct);
}
