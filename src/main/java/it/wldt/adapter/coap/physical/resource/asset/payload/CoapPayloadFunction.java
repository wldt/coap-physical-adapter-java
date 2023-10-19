package it.wldt.adapter.coap.physical.resource.asset.payload;

import it.wldt.core.event.WldtEvent;

import java.util.List;
import java.util.function.Function;

/**
 * Represents a function that accepts a CoAP payload and, when applied, produces a WldtEvent.
 */
@FunctionalInterface
public interface CoapPayloadFunction<T> extends Function<T, List<WldtEvent<?>>> {
}
