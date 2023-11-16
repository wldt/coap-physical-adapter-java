package it.wldt.adapter.coap.physical.resources.methods;

public interface CoapPostMethod {
    void sendPOST(byte[] payload, String ct);
}
