package it.wldt.adapter.coap.physical.resources.methods;

public interface CoapPutSupport {
    String ACTION_KEY = "update";

    void sendPUT(byte[] payload, String ct);
}
