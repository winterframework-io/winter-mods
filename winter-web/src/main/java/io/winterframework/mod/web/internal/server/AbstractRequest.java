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
package io.winterframework.mod.web.internal.server;

import java.net.SocketAddress;
import java.util.Optional;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.winterframework.mod.web.Method;
import io.winterframework.mod.web.Parameter;
import io.winterframework.mod.web.header.Headers;
import io.winterframework.mod.web.internal.server.multipart.MultipartDecoder;
import io.winterframework.mod.web.server.Part;
import io.winterframework.mod.web.server.Request;
import io.winterframework.mod.web.server.RequestBody;
import io.winterframework.mod.web.server.RequestCookies;
import io.winterframework.mod.web.server.RequestHeaders;
import io.winterframework.mod.web.server.RequestParameters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * @author jkuhn
 *
 */
public abstract class AbstractRequest implements Request {

	protected final ChannelHandlerContext context;
	protected final RequestHeaders requestHeaders;
	protected final MultipartDecoder<Parameter> urlEncodedBodyDecoder;
	protected final MultipartDecoder<Part> multipartBodyDecoder;
	
	protected GenericRequestParameters requestParameters;
	protected GenericRequestCookies requestCookies;
	
	private Optional<RequestBody> requestBody;
	private Sinks.Many<ByteBuf> data;
	
	public AbstractRequest(ChannelHandlerContext context, RequestHeaders requestHeaders, MultipartDecoder<Parameter> urlEncodedBodyDecoder, MultipartDecoder<Part> multipartBodyDecoder) {
		this.context = context;
		this.requestHeaders = requestHeaders;
		this.urlEncodedBodyDecoder = urlEncodedBodyDecoder;
		this.multipartBodyDecoder = multipartBodyDecoder;
	}

	@Override
	public RequestHeaders headers() {
		return this.requestHeaders;
	}

	@Override
	public RequestParameters parameters() {
		if(this.requestParameters == null) {
			this.requestParameters = new GenericRequestParameters(this.requestHeaders.getPath());
		}
		return this.requestParameters;
	}

	@Override
	public RequestCookies cookies() {
		if(this.requestCookies == null) {
			this.requestCookies = new GenericRequestCookies(this.requestHeaders);
		}
		return this.requestCookies;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return this.context.channel().remoteAddress();
	}
	
	@Override
	public Optional<RequestBody> body() {
		if(this.requestBody == null) {
			Method method = this.headers().getMethod();
			if(method == Method.POST || method == Method.PUT || method == Method.PATCH) {
				if(this.requestBody == null) {
					// TODO deal with backpressure using a custom queue: if the queue reach a given threshold we should suspend the read on the channel: this.context.channel().config().setAutoRead(false)
					// and resume when this flux is actually consumed (doOnRequest? this might impact performance)
					this.data = Sinks.many().unicast().onBackpressureBuffer();
					Flux<ByteBuf> requestBodyData = this.data.asFlux()
						.doOnDiscard(ByteBuf.class, ByteBuf::release);
					
					this.requestBody = Optional.of(new GenericRequestBody(
						this.headers().<Headers.ContentType>getHeader(Headers.NAME_CONTENT_TYPE),
						this.urlEncodedBodyDecoder, 
						this.multipartBodyDecoder, 
						requestBodyData
					));
				}
			}
			else {
				this.requestBody = Optional.empty();
			}
		}
		return this.requestBody;
	}

	public Optional<Sinks.Many<ByteBuf>> data() {
		return Optional.ofNullable(this.data);
	}
	
	public void dispose() {
		if(this.data != null) {
			// Try to drain and release buffered data 
			this.data.asFlux().subscribe(
				chunk -> chunk.release(), 
				ex -> {
					// TODO Should be ignored but can be logged as debug or trace log
				}
			);
		}
	}
}
