package it.wldt.adapter.coap.physical.resource.event;

import java.util.HashSet;
import java.util.Set;

public abstract class ListenablePayloadResource {
    protected Set<PayloadListener> listeners;

    public ListenablePayloadResource() {
        this.listeners = new HashSet<>();
    }

    public void addPayloadListener(PayloadListener listener) {
        this.listeners.add(listener);
    }

    public void removePayloadListener(PayloadListener listener) {
        this.listeners.remove(listener);
    }

    protected void notifyListeners(byte[] value) {
        for (PayloadListener listener : this.listeners) {
            listener.onPayloadChanged(value);
        }
    }
}
