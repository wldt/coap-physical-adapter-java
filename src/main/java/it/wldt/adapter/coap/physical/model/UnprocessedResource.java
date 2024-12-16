package it.wldt.adapter.coap.physical.model;

public record UnprocessedResource(
        String name,
        String resourceType,
        int contentType,
        boolean hasPostSupport,
        boolean hasPutSupport,
        boolean observable) { }
