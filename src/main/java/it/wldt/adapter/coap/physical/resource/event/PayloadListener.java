package it.wldt.adapter.coap.physical.resource.event;

/**
 * Represents a listener whose callback function gets called whenever the listened resource's payload gets changed.
 *
 * @see ListenablePayloadResource
 */
public interface PayloadListener {
    void onPayloadChanged(byte[] value);
}
