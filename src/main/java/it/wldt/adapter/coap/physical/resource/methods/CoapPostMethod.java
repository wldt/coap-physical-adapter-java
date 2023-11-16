package it.wldt.adapter.coap.physical.resource.methods;

public interface CoapPostMethod {
    void sendPOST(byte[] payload, String ct);
}
