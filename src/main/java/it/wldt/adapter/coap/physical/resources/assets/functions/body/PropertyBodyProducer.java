package it.wldt.adapter.coap.physical.resources.assets.functions.body;

import java.util.function.Function;

public class PropertyBodyProducer <P> {

    private Function<byte[], P> producer;
    private String contentMimeType;

    public PropertyBodyProducer(Function<byte[], P> producer) {
        this(producer, "");
    }

    public PropertyBodyProducer(Function<byte[], P> producer, String contentMimeType) {
        this.producer = producer;
        this.contentMimeType = contentMimeType;
    }

    public Function<byte[], P> getProducer() {
        return producer;
    }

    public void setProducer(Function<byte[], P> producer) {
        this.producer = producer;
    }

    public String getContentMimeType() {
        return contentMimeType;
    }

    public void setContentMimeType(String contentMimeType) {
        this.contentMimeType = contentMimeType;
    }
}
