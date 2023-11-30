package it.wldt.adapter.coap.physical.resources.methods;

public interface CoapPostSupport {
    String ACTION_KEY = "change";

    void sendPOST(byte[] payload, String ct);
}
