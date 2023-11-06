package it.wldt.adapter.coap.physical.discovery;

/**
 * A wrapper record used to represent a discovered CoAP resource.
 * The {@code resourceInterface} field must use the {@code Interface} enum to specify the CoRE format interface for the correct Physical Adapter operations
 *
 * @param uri               the resource uri
 * @param resourceType      the resource type (rt)
 * @param resourceInterface the resource interface (if)
 * @param observable        the resource observability flag (obs)
 *
 * @see Interface
 */
public record DiscoveredResource (String uri, String resourceType, Interface resourceInterface, boolean observable) {
    /**
     *
     */
    public enum Interface {
        SENSOR("core.s"),
        ACTUATOR("core.a"),
        UNKNOWN("");

        private final String value;

        Interface(String value) {
            this.value = value;
        }

        public String getValueString() {
            return value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    public static final String WKC_ATTR_RESOURCE_TYPE = "rt";
    public static final String WKC_ATTR_RESOURCE_INTERFACE = "if";
    public static final String WKC_ATTR_OBSERVABLE = "obs";
}
