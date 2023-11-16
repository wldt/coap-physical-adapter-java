package it.wldt.adapter.coap.physical.resource.methods;

public interface CoapPutMethod {
    void sendPUT(byte[] payload, String ct);
}
