package it.wldt.adapter.coap.physical.model;

import it.wldt.core.event.WldtEvent;

import java.util.List;

/**
 * Listener interface for receiving updates from a {@link PhysicalAssetResource}.
 */
public interface PhysicalAssetResourceListener {
    /**
     * What the listener is interested in.
     */
    enum ListenerType {
        PROPERTY,
        EVENT,
        ALL
    }

    /**
     * Called when a property of the resource gets updated.
     *
     * @param resource   The resource which received an update.
     * @param properties The list of updated properties.
     */
    void onPropertyChanged(PhysicalAssetResource resource, List<? extends WldtEvent<?>> properties);

    /**
     * Called when a resource triggers an event.
     *
     * @param resource The resource which triggered the event.
     * @param events   The list of events.
     */
    void onEvent(PhysicalAssetResource resource, List<? extends WldtEvent<?>> events);
}
