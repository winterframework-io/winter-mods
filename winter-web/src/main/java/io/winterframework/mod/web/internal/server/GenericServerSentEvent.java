/**
 * 
 */
package io.winterframework.mod.web.internal.server;

import org.reactivestreams.Publisher;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.winterframework.mod.web.Charsets;
import io.winterframework.mod.web.ServerSentEvent;
import reactor.core.publisher.Mono;

/**
 * @author jkuhn
 *
 */
public class GenericServerSentEvent implements ServerSentEvent<ByteBuf>, ServerSentEvent.Configurator<ByteBuf> {

	private String id;
	
	private String comment;
	
	private String event;
	
	private Publisher<ByteBuf> data;
	
	@Override
	public Configurator<ByteBuf> id(String id) {
		this.id = id;
		return this;
	}

	@Override
	public Configurator<ByteBuf> comment(String comment) {
		this.comment = comment;
		return this;
	}

	@Override
	public Configurator<ByteBuf> event(String event) {
		this.event = event;
		return this;
	}

	@Override
	public Configurator<ByteBuf> data(Publisher<ByteBuf> data) {
		this.data = data;
		return this;
	}
	
	@Override
	public Configurator<ByteBuf> data(byte[] data) {
		return this.data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(data))));
	}
	
	@Override
	public Configurator<ByteBuf> data(String data) {
		return this.data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(data, Charsets.UTF_8))));
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getComment() {
		return this.comment;
	}

	@Override
	public String getEvent() {
		return this.event;
	}

	@Override
	public Publisher<ByteBuf> getData() {
		return this.data;
	}

}
