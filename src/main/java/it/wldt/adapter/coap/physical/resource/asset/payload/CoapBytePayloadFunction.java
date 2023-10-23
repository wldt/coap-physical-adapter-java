package it.wldt.adapter.coap.physical.resource.asset.payload;

import it.wldt.core.event.WldtEvent;

import java.util.List;
import java.util.function.Function;

/**
 * An extension to <code>CoapPayloadFunction</code> used for <code>byte[]</code> payloads.
 *
 * @see FunctionalInterface
 * @see CoapPayloadFunction
 */
@FunctionalInterface
public interface CoapBytePayloadFunction extends CoapPayloadFunction<byte[]> {
}
