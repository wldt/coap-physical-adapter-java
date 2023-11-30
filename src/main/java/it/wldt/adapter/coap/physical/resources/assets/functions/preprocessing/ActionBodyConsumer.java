package it.wldt.adapter.coap.physical.resources.assets.functions.preprocessing;

import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.function.Function;

public class ActionBodyConsumer <A> {
    private Function<A, byte[]> consumer;
    private String contentMimeType;

    public ActionBodyConsumer(Function<A, byte[]> consumer) {
        this(consumer, MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN));
    }

    public ActionBodyConsumer(Function<A, byte[]> consumer, int contentMimeType) {
        this(consumer, MediaTypeRegistry.toString(contentMimeType));
    }

    public ActionBodyConsumer(Function<A, byte[]> consumer, String contentMimeType) {
        this.consumer = consumer;
        this.contentMimeType = contentMimeType;
    }

    public Function<A, byte[]> getConsumer() {
        return consumer;
    }

    public void setConsumer(Function<A, byte[]> consumer) {
        this.consumer = consumer;
    }

    public String getContentMimeType() {
        return contentMimeType;
    }

    public void setContentMimeType(String contentMimeType) {
        this.contentMimeType = contentMimeType;
    }
}
