package it.wldt.adapter.coap.physical.server.event;

public interface ResourceDataListener<T> {
    void onDataChanged(T value);
}
