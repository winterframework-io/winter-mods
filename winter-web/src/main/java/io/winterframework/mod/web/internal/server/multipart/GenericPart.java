/*
 * Copyright 2020 Jeremy KUHN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.winterframework.mod.web.internal.server.multipart;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.reactivestreams.Publisher;

import io.netty.buffer.ByteBuf;
import io.winterframework.mod.base.converter.ObjectConverter;
import io.winterframework.mod.web.header.Header;
import io.winterframework.mod.web.server.Part;
import io.winterframework.mod.web.server.PartHeaders;
import io.winterframework.mod.web.server.RequestData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * @author jkuhn
 *
 */
class GenericPart implements Part {

	private String name;
	
	private String filename;

	private PartHeaders partHeaders;
	
	private Flux<ByteBuf> data;
	private FluxSink<ByteBuf> dataEmitter;
	
	private RequestData<ByteBuf> rawData;
	
	public GenericPart(ObjectConverter<String> parameterConverter, String name, Map<String, List<Header>> headers, String contentType, Charset charset, Long contentLength) {
		this(parameterConverter, name, null, headers, contentType, charset, contentLength);
	}
	
	public GenericPart(ObjectConverter<String> parameterConverter, String name, String filename, Map<String, List<Header>> headers, String contentType, Charset charset, Long contentLength) {
		this.name = name;
		this.filename = filename;
		this.partHeaders = new GenericPartHeaders(headers, contentType, charset, contentLength, parameterConverter);
		
		this.data = Flux.<ByteBuf>create(emitter -> {
			this.dataEmitter = emitter;
		}).doOnDiscard(ByteBuf.class, ByteBuf::release);
	}
	
	public Optional<FluxSink<ByteBuf>> getDataEmitter() {
		return Optional.ofNullable(this.dataEmitter);
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Optional<String> getFilename() {
		return Optional.ofNullable(this.filename);
	}
	
	@Override
	public PartHeaders headers() {
		return this.partHeaders;
	}
	
	// Emitted ByteBuf must be released 
	// But if nobody subscribe they are released
	@Override
	public RequestData<ByteBuf> raw() {
		if(this.rawData == null) {
			this.rawData = new GenericPartRawData();
		}
		return this.rawData;
	}
	
	private class GenericPartRawData implements RequestData<ByteBuf> {

		@Override
		public Publisher<ByteBuf> stream() {
			return GenericPart.this.data;
		}
	}
}
