package it.wldt.adapter.coap.physical.resources.discovery;

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
public record DiscoveredResource (
        String uri,
        String resourceType,
        int contentType,
        Interface resourceInterface,
        boolean observable
) {
    /**
     *
     */
    public enum Interface {
        /*
        From Iana CoRE parameters:
        https://www.iana.org/assignments/core-parameters/core-parameters.xhtml#if-link-target-att-value

            +----------------------------+----------------+------------+
            |                      Range |	 Registration | Procedures |
            +----------------------------+----------------+------------+
            |   value starts with "core" |	  IETF Review |            |
            |           all other values |	Specification |   Required |
            +----------------------------+----------------+------------+

        From IETF CoRE Interfaces:
        https://datatracker.ietf.org/doc/html/draft-ietf-core-interfaces-01

            +-------------------+----------+------------------------------------+
            |         Interface | if=      | Methods                            |
            +-------------------+----------+------------------------------------+
            |         Link List | core.ll  | GET                                |
            |             Batch | core.b   | GET, PUT, POST (where applicable)  |
            |      Linked Batch | core.lb  | GET, PUT, POST, DELETE (where      |
            |                   |          | applicable)                        |
            |            Sensor | core.s   | GET                                | *
            |         Parameter | core.p   | GET, PUT                           | *
            |         Read-only | core.rp  | GET                                | *
            |         Parameter |          |                                    |
            |          Actuator | core.a   | GET, PUT, POST                     | *
            |           Binding | core.bnd | GET, POST, DELETE                  |
            +-------------------+----------+------------------------------------+

        * The ones we are interested in
         */

        SENSOR("core.s"),
        ACTUATOR("core.a"),
        PARAMETER("core.p"),
        READ_ONLY("core.rp"),
        UNKNOWN("");

        private final String value;

        Interface(String value) {
            this.value = value;
        }

        public static Interface fromString(String value) {
            if (SENSOR.value.equals(value)) {
                return SENSOR;
            } else if (ACTUATOR.value.equals(value)) {
                return ACTUATOR;
            } else if (PARAMETER.value.equals(value)) {
                return PARAMETER;
            } else if (READ_ONLY.value.equals(value)) {
                return READ_ONLY;
            }
            return UNKNOWN;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    public static final String WKC_ATTR_RESOURCE_TYPE = "rt";
    public static final String WKC_ATTR_RESOURCE_INTERFACE = "if";
    public static final String WKC_ATTR_CONTENT_TYPE = "ct";
    public static final String WKC_ATTR_OBSERVABLE = "obs";

    @Override
    public String toString() {
        return "DiscoveredResource{" +
                "uri='" + uri + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", contentType=" + contentType +
                ", resourceInterface=" + resourceInterface +
                ", observable=" + observable +
                '}';
    }
}
