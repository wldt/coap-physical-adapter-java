package it.wldt.adapter.coap.physical.resources.events;

/**
 * Represents a listener whose callback function gets called whenever the listened resource's payload gets changed.
 *
 * @see ListenableResource
 */
public interface PayloadListener {
    void onPayloadChanged(byte[] value);
}
