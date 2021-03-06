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
package io.inverno.mod.http.server.internal.http1x;

import io.inverno.mod.base.converter.ObjectConverter;
import io.inverno.mod.http.base.Parameter;
import io.inverno.mod.http.base.header.HeaderService;
import io.inverno.mod.http.server.ErrorExchange;
import io.inverno.mod.http.server.Exchange;
import io.inverno.mod.http.server.ExchangeHandler;
import io.inverno.mod.http.server.Part;
import io.inverno.mod.http.server.internal.AbstractExchange;
import io.inverno.mod.http.server.internal.multipart.MultipartDecoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * <p>
 * HTTP1.x channel handler implementation.
 * </p>
 * 
 * <p>
 * This is the entry point of a HTTP client connection to the HTTP server using
 * version 1.x of the HTTP protocol.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
public class Http1xChannelHandler extends ChannelDuplexHandler implements Http1xConnectionEncoder, AbstractExchange.Handler {

	private Http1xExchange requestingExchange;
	private Http1xExchange respondingExchange;
	
	private Http1xExchange exchangeQueue;
	
	private ExchangeHandler<Exchange> rootHandler;
	private ExchangeHandler<ErrorExchange<Throwable>> errorHandler; 
	private HeaderService headerService;
	private ObjectConverter<String> parameterConverter;
	private MultipartDecoder<Parameter> urlEncodedBodyDecoder; 
	private MultipartDecoder<Part> multipartBodyDecoder;
	
	private boolean read;
	private boolean flush;
	
	/**
	 * <p>
	 * Creates a HTTP1.x channel handler.
	 * </p>
	 * 
	 * @param rootHandler           the root exchange handler
	 * @param errorHandler          the error exchange handler
	 * @param headerService         the header service
	 * @param parameterConverter    a string object converter
	 * @param urlEncodedBodyDecoder the application/x-www-form-urlencoded body decoder
	 * @param multipartBodyDecoder  the multipart/form-data body decoder
	 */
	public Http1xChannelHandler(
			ExchangeHandler<Exchange> rootHandler, 
			ExchangeHandler<ErrorExchange<Throwable>> errorHandler, 
			HeaderService headerService, 
			ObjectConverter<String> parameterConverter,
			MultipartDecoder<Parameter> urlEncodedBodyDecoder, 
			MultipartDecoder<Part> multipartBodyDecoder) {
		this.rootHandler = rootHandler;
		this.errorHandler = errorHandler;
		this.headerService = headerService;
		this.parameterConverter = parameterConverter;
		this.urlEncodedBodyDecoder = urlEncodedBodyDecoder;
		this.multipartBodyDecoder = multipartBodyDecoder;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		System.out.println("Channel read");
		this.read = true;
		if(msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest)msg;
			if(httpRequest.decoderResult().isFailure()) {
				this.onDecoderError(ctx, httpRequest);
				return;
			}
			this.requestingExchange = new Http1xExchange(ctx, httpRequest, this, this.headerService, this.parameterConverter, this.urlEncodedBodyDecoder, this.multipartBodyDecoder, this.rootHandler, this.errorHandler);
			if(this.exchangeQueue == null) {
				this.exchangeQueue = this.requestingExchange;
				this.requestingExchange.start(this);
			}
			else {
				this.exchangeQueue.next = this.requestingExchange;
				this.exchangeQueue = this.requestingExchange;
			}
		}
		else if(this.requestingExchange != null) {
			if(msg == LastHttpContent.EMPTY_LAST_CONTENT) {
				this.requestingExchange.request().data().ifPresent(sink -> sink.tryEmitComplete());
			}
			else {
				HttpContent httpContent = (HttpContent)msg;
				if(httpContent.decoderResult().isFailure()) {
					this.onDecoderError(ctx, httpContent);
					return;
				}
				this.requestingExchange.request().data().ifPresentOrElse(emitter -> emitter.tryEmitNext(httpContent.content()), () -> httpContent.release());
				if(httpContent instanceof LastHttpContent) {
					this.requestingExchange.request().data().ifPresent(sink -> sink.tryEmitComplete());
				}
			}
		}
		else {
			// This can happen when an exchange has been disposed before we actually
			// received all the data in that case we have to dismiss the content and wait
			// for the next request
			((HttpContent)msg).release();
		}
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//		System.out.println("Channel read complete");
		if(this.read) {
			this.read = false;
			if(this.flush) {
				ctx.flush();
				this.flush = false;
			}
		}
	}

	private void onDecoderError(ChannelHandlerContext ctx, HttpObject httpObject) {
		Throwable cause = httpObject.decoderResult().cause();
		if (cause instanceof TooLongFrameException) {
			String causeMsg = cause.getMessage();
			HttpResponseStatus status;
			if(causeMsg.startsWith("An HTTP line is larger than")) {
				status = HttpResponseStatus.REQUEST_URI_TOO_LONG;
			} 
			else if(causeMsg.startsWith("HTTP header is larger than")) {
				status = HttpResponseStatus.REQUEST_HEADER_FIELDS_TOO_LARGE;
			} 
			else {
				status = HttpResponseStatus.BAD_REQUEST;
			}
			ChannelPromise writePromise = ctx.newPromise();
			ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status), writePromise);
			writePromise.addListener(res -> {
				ctx.fireExceptionCaught(cause);
			});
		} 
		else {
			ctx.fireExceptionCaught(cause);
		}
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//		System.out.println("User Event triggered");
		// TODO idle
		ctx.fireUserEventTriggered(evt);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//		System.out.println("Exception caught");
		ctx.close();
	}
	
	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
//		System.out.println("close");
		ctx.close();
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//		System.out.println("channel inactive");
		if(this.respondingExchange != null) {
			this.respondingExchange.dispose();
		}
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
//		super.channelWritabilityChanged(ctx);
//		System.out.println("Channel writability changed");
	}
	
	@Override
	public ChannelFuture writeFrame(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		if(this.read) {
			this.flush = true;
			return ctx.write(msg, promise);
		}
		else {
			return ctx.writeAndFlush(msg, promise);
		}
	}
	
	@Override
	public void exchangeStart(ChannelHandlerContext ctx, AbstractExchange exchange) {
		this.respondingExchange = (Http1xExchange)exchange;
	}
	
	@Override
	public void exchangeError(ChannelHandlerContext ctx, Throwable t) {
		// If we get there it means we weren't able to properly handle the error before
		if(this.flush) {
			ctx.flush();
		}
		// We have to to release data...
		if(this.respondingExchange.next != null) {
			this.respondingExchange.next.dispose();
		}
		// ...and close the connection
		ctx.close();
	}
	
	@Override
	public void exchangeComplete(ChannelHandlerContext ctx) {
		if(this.respondingExchange.keepAlive) {
			if(this.respondingExchange.next != null) {
				this.respondingExchange.next.start(this);
			}
			else {
				this.exchangeQueue = null;
				this.respondingExchange = null;
			}
		}
		else {
			if(this.respondingExchange.next != null) {
				this.respondingExchange.next.dispose();
			}
			ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}
}