package it.wldt.adapter.coap.physical.server.event;

import java.util.HashSet;
import java.util.Set;

public class DataListenerManager<T> {
    protected transient Set<ResourceDataListener<T>> listeners;

    public DataListenerManager() {
        this.listeners = new HashSet<>();
    }

    public void addDataListener(ResourceDataListener<T> listener) {
        this.listeners.add(listener);
    }

    public void removeDataListener(ResourceDataListener<T> listener) {
        this.listeners.remove(listener);
    }

    protected void notifyListeners(T value) {
        if (this.listeners == null) {
            return;
        }

        for (ResourceDataListener<T> listener : this.listeners) {
            listener.onDataChanged(value);
        }
    }
}
