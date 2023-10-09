package it.wldt.adapter.coap.physical.resource.event;

public interface PayloadListener {
    void onPayloadChanged(byte[] value);
}
