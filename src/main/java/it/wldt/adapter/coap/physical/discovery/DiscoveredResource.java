package it.wldt.adapter.coap.physical.discovery;

public record DiscoveredResource (String uri, String resourceType, String resourceInterface, boolean observable) {

    public static final String WKC_ATTR_RESOURCE_TYPE = "rt";
    public static final String WKC_ATTR_RESOURCE_INTERFACE = "if";
    public static final String WKC_ATTR_OBSERVABLE = "obs";

    public static final String INTERFACE_SENSOR = "core.s";
    public static final String INTERFACE_ACTUATOR = "core.a";
}
