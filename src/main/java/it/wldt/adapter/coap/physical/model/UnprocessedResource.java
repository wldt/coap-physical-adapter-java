package it.wldt.adapter.coap.physical.model;

/**
 * Represents a resource that has not been processed yet.
 * This class is used to store information about the resource
 * such as its name, type, content type, and supported methods.
 */
public class UnprocessedResource {
    private final String name;
    private final String resourceType;
    private final int contentType;
    private final boolean postSupport;
    private final boolean putSupport;
    private final boolean observable;

    public UnprocessedResource(String name, String resourceType, int contentType,
                             boolean postSupport, boolean putSupport, boolean observable) {
        this.name = name;
        this.resourceType = resourceType;
        this.contentType = contentType;
        this.postSupport = postSupport;
        this.putSupport = putSupport;
        this.observable = observable;
    }

    public String getName() {
        return name;
    }

    public String getResourceType() {
        return resourceType;
    }

    public int getContentType() {
        return contentType;
    }

    public boolean hasPostSupport() {
        return postSupport;
    }

    public boolean hasPutSupport() {
        return putSupport;
    }

    public boolean isObservable() {
        return observable;
    }
}