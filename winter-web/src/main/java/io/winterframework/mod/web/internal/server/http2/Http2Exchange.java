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
package io.winterframework.mod.web.internal.server.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Stream;
import io.winterframework.mod.web.Parameter;
import io.winterframework.mod.web.header.HeaderService;
import io.winterframework.mod.web.internal.server.AbstractExchange;
import io.winterframework.mod.web.internal.server.GenericErrorExchange;
import io.winterframework.mod.web.internal.server.multipart.MultipartDecoder;
import io.winterframework.mod.web.server.ErrorExchange;
import io.winterframework.mod.web.server.Exchange;
import io.winterframework.mod.web.server.ExchangeHandler;
import io.winterframework.mod.web.server.Part;

/**
 * @author jkuhn
 *
 */
public class Http2Exchange extends AbstractExchange {

	private final Http2Stream stream;
	private final Http2ConnectionEncoder encoder;
	private final HeaderService headerService;
	
	public Http2Exchange(
			ChannelHandlerContext context, 
			Http2Stream stream, 
			Http2Headers httpHeaders, 
			Http2ConnectionEncoder encoder,
			HeaderService headerService,
			MultipartDecoder<Parameter> urlEncodedBodyDecoder, 
			MultipartDecoder<Part> multipartBodyDecoder,
			ExchangeHandler<Exchange> rootHandler, 
			ExchangeHandler<ErrorExchange<Throwable>> errorHandler
		) {
		super(context, rootHandler, errorHandler, new Http2Request(context, new Http2RequestHeaders(headerService, httpHeaders), urlEncodedBodyDecoder, multipartBodyDecoder), new Http2Response(context, headerService));
		this.stream = stream;
		this.encoder = encoder;
		this.headerService = headerService;
	}
	
	@Override
	protected ErrorExchange<Throwable> createErrorExchange(Throwable error) {
		return new GenericErrorExchange(this.request, new Http2Response(this.context, this.headerService), error);
	}
	
	@Override
	protected void onNextMany(ByteBuf value) {
		try {
			Http2ResponseHeaders headers = (Http2ResponseHeaders)this.response.headers();
			if(!headers.isWritten()) {
				this.encoder.writeHeaders(this.context, this.stream.id(), headers.getInternalHeaders(), 0, false, this.context.voidPromise());
				headers.setWritten(true);
			}
			// TODO implement back pressure with the flow controller
			/*this.encoder.flowController().listener(new Listener() {
				
				@Override
				public void writabilityChanged(Http2Stream stream) {
					
				}
			});*/
			
			this.encoder.writeData(this.context, this.stream.id(), value, 0, false, this.context.voidPromise());
			this.context.channel().flush();
		}
		finally {
			this.handler.exchangeNext(this.context, value);
		}
	}
	
	@Override
	protected void onCompleteWithError(Throwable throwable) {
		throwable.printStackTrace();
		this.handler.exchangeError(this.context, throwable);
	}
	
	@Override
	protected void onCompleteEmpty() {
		this.onCompleteMany();
	}
	
	@Override
	protected void onCompleteSingle(ByteBuf value) {
		Http2ResponseHeaders headers = (Http2ResponseHeaders)this.response.headers();
		if(!headers.isWritten()) {
			this.encoder.writeHeaders(this.context, this.stream.id(), headers.getInternalHeaders(), 0, false, this.context.voidPromise());
			headers.setWritten(true);
		}
		Http2ResponseTrailers trailers = (Http2ResponseTrailers)this.response.trailers();
		this.encoder.writeData(this.context, this.stream.id(), value, 0, trailers != null, this.context.voidPromise());
		if(trailers != null) {
			this.encoder.writeHeaders(this.context, this.stream.id(), trailers.getInternalTrailers(), 0, true, this.context.voidPromise());	
		}
		this.context.channel().flush();
		this.handler.exchangeNext(this.context, value);
		this.handler.exchangeComplete(this.context);
	}
	
	@Override
	protected void onCompleteMany() {
		Http2ResponseHeaders headers = (Http2ResponseHeaders)this.response.headers();
		if(!headers.isWritten()) {
			this.encoder.writeHeaders(this.context, this.stream.id(), headers.getInternalHeaders(), 0, false, this.context.voidPromise());
			headers.setWritten(true);
		}
		else {
			this.encoder.writeData(this.context, this.stream.id(), Unpooled.EMPTY_BUFFER, 0, true, this.context.voidPromise());
		}
		this.context.channel().flush();
		this.handler.exchangeComplete(this.context);
	}
}
