package it.wldt.adapter.coap.physical.model;

import it.wldt.core.event.WldtEvent;

import java.util.List;

public interface PhysicalAssetResourceListener {
    enum ListenerType {
        PROPERTY,
        EVENT,
        ALL
    }

    void onPropertyChanged(PhysicalAssetResource resource, List<? extends WldtEvent<?>> properties);
    void onEvent(PhysicalAssetResource resource, List<? extends WldtEvent<?>> events);
}
