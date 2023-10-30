package it.wldt.adapter.coap.physical.resource.event;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a resource whose payload changes can be listened by other classes.
 * To listen to the payload changes a {@code PayloadListener} is required.
 *
 * @see PayloadListener
 */
public abstract class ListenableResource {
    protected Set<PayloadListener> payloadListeners;
    protected Set<ErrorListener> errorListeners;

    public ListenableResource() {
        this.payloadListeners = new HashSet<>();
        this.errorListeners = new HashSet<>();
    }

    public ListenableResource(boolean listenErrorsFlag) {
        this();
    }

    public void addPayloadListener(PayloadListener listener) {
        this.payloadListeners.add(listener);
    }

    public void removePayloadListener(PayloadListener listener) {
        this.payloadListeners.remove(listener);
    }

    protected void notifyPayloadListeners(byte[] value) {
        for (PayloadListener listener : this.payloadListeners) {
            listener.onPayloadChanged(value);
        }
    }

    public void addErrorListener(ErrorListener listener) {
        this.errorListeners.add(listener);
    }

    public void removeErrorListener(ErrorListener listener) {
        errorListeners.remove(listener);
    }

    protected void notifyErrorListeners(String message) {
        for (ErrorListener listener : this.errorListeners) {
            listener.onError(message);
        }
    }
}
