package it.wldt.adapter.coap.physical.resources.assets.functions.body;

import java.util.function.Function;

public class EventBodyProducer <E> {
    private Function<String, E> producer;
    private String contentMimeType;

    public EventBodyProducer(Function<String, E> producer) {
        this(producer, "");
    }

    public EventBodyProducer(Function<String, E> producer, String contentMimeType) {
        this.producer = producer;
        this.contentMimeType = contentMimeType;
    }

    public Function<String, E> getProducer() {
        return producer;
    }

    public void setProducer(Function<String, E> producer) {
        this.producer = producer;
    }

    public String getContentMimeType() {
        return contentMimeType;
    }

    public void setContentMimeType(String contentMimeType) {
        this.contentMimeType = contentMimeType;
    }
}
