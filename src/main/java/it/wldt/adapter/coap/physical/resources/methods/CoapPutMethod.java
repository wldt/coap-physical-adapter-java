package it.wldt.adapter.coap.physical.resources.methods;

public interface CoapPutMethod {
    void sendPUT(byte[] payload, String ct);
}
