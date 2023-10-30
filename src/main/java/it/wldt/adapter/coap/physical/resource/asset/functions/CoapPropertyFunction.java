package it.wldt.adapter.coap.physical.resource.asset.functions;

import it.wldt.core.event.WldtEvent;

import java.util.List;
import java.util.function.Function;

/**
 *
 * Represents a function that accepts a CoAP payload and, when applied, produces a {@code WldtEvent}.
 *
 * @see FunctionalInterface
 * @see Function
 */
@FunctionalInterface
public interface CoapPropertyFunction extends Function<byte[], List<WldtEvent<?>>> {
}
