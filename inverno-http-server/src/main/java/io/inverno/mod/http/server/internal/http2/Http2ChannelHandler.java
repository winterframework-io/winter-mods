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
package io.inverno.mod.http.server.internal.http2;

import io.inverno.mod.base.converter.ObjectConverter;
import io.inverno.mod.http.base.Parameter;
import io.inverno.mod.http.base.header.HeaderService;
import io.inverno.mod.http.server.ErrorExchange;
import io.inverno.mod.http.server.Exchange;
import io.inverno.mod.http.server.ExchangeHandler;
import io.inverno.mod.http.server.HttpServerConfiguration;
import io.inverno.mod.http.server.Part;
import io.inverno.mod.http.server.internal.AbstractExchange;
import io.inverno.mod.http.server.internal.multipart.MultipartDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Flags;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import reactor.core.publisher.Sinks.EmitResult;

/**
 * <p>
 * HTTP/2 channel handler implementation.
 * </p>
 * 
 * <p>
 * This is the entry point of a HTTP client connection to the HTTP server using
 * version 2 of the HTTP protocol.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
public class Http2ChannelHandler extends Http2ConnectionHandler implements Http2FrameListener, Http2Connection.Listener {

	private static final ContentEncodingResolver CONTENT_ENCODING_RESOLVER = new ContentEncodingResolver();
	
	private final HttpServerConfiguration configuration; 
	private final ExchangeHandler<Exchange> rootHandler;
	private final ExchangeHandler<ErrorExchange<Throwable>> errorHandler;
	private final HeaderService headerService;
	private final ObjectConverter<String> parameterConverter;
	private final MultipartDecoder<Parameter> urlEncodedBodyDecoder;
	private final MultipartDecoder<Part> multipartBodyDecoder;

	private final IntObjectMap<Http2Exchange> serverStreams;

	/**
	 * <p>
	 * Creates a HTTP/2 channel handler.
	 * </p>
	 * 
	 * @param configuration         the HTTP server configuration
	 * @param decoder               HTTP/2 connection decoder
	 * @param encoder               HTTP/2 connection encoder
	 * @param initialSettings       HTTP/2 initial settings
	 * @param rootHandler           the root exchange handler
	 * @param errorHandler          the error exchange handler
	 * @param headerService         the header service
	 * @param parameterConverter    a string object converter
	 * @param urlEncodedBodyDecoder the application/x-www-form-urlencoded body
	 *                              decoder
	 * @param multipartBodyDecoder  the multipart/form-data body decoder
	 */
	public Http2ChannelHandler(
			HttpServerConfiguration configuration,
			Http2ConnectionDecoder decoder, 
			Http2ConnectionEncoder encoder,
			Http2Settings initialSettings,
			ExchangeHandler<Exchange> rootHandler,
			ExchangeHandler<ErrorExchange<Throwable>> errorHandler,
			HeaderService headerService, 
			ObjectConverter<String> parameterConverter,
			MultipartDecoder<Parameter> urlEncodedBodyDecoder,
			MultipartDecoder<Part> multipartBodyDecoder) {
		super(decoder, encoder, initialSettings);

		this.configuration = configuration;
		this.rootHandler = rootHandler;
		this.errorHandler = errorHandler;
		this.headerService = headerService;
		this.parameterConverter = parameterConverter;
		this.urlEncodedBodyDecoder = urlEncodedBodyDecoder;
		this.multipartBodyDecoder = multipartBodyDecoder;

		this.serverStreams = new IntObjectHashMap<>();
		this.connection().addListener(this);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//    	System.out.println("error");
		super.exceptionCaught(ctx, cause);
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
//    	System.out.println("Channel inactive");
	}

	@Override
	public void onError(ChannelHandlerContext ctx, boolean outbound, Throwable cause) {
		super.onError(ctx, outbound, cause);
		ctx.close();
	}

	/**
	 * If receive a frame with end-of-stream set, send a pre-canned response.
	 */
	@Override
	public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
//        System.out.println("onDataRead() " + streamId + " - "+ endOfStream);

		// TODO flow control?
		int processed = data.readableBytes() + padding;

		Http2Exchange serverStream = this.serverStreams.get(streamId);
		if (serverStream != null) {
			serverStream.request().data().ifPresent(sink -> {
				data.retain();
				if(sink.tryEmitNext(data) != EmitResult.OK) {
					data.release();
				}
			});
			if (endOfStream) {
				serverStream.request().data().ifPresent(sink -> sink.tryEmitComplete());
			}
		} 
		else {
			// TODO this should never happen?
			throw new IllegalStateException("Unable to push data to unmanaged stream " + streamId);
		}

		return processed;
	}

	@Override
	public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endOfStream) throws Http2Exception {
//        System.out.println("onHeaderReads(2) " + streamId + " - " + endOfStream + " - " + this.hashCode());
		Http2Exchange exchange = this.serverStreams.get(streamId);
		if (exchange == null) {
			Http2Exchange streamExchange = new Http2Exchange(ctx, this.connection().stream(streamId), headers, this.encoder(), this.headerService, this.parameterConverter, this.urlEncodedBodyDecoder, this.multipartBodyDecoder, this.rootHandler, this.errorHandler);
			if(this.configuration.compression_enabled()) {
				String acceptEncoding = headers.get(HttpHeaderNames.ACCEPT_ENCODING) != null ? headers.get(HttpHeaderNames.ACCEPT_ENCODING).toString() : null;
				if(acceptEncoding != null) {
					streamExchange.setContentEncoding(CONTENT_ENCODING_RESOLVER.resolve(acceptEncoding));
				}
			}
			this.serverStreams.put(streamId, streamExchange);
			if (endOfStream) {
				streamExchange.request().data().ifPresent(sink -> sink.tryEmitComplete());
			}
			streamExchange.start(new AbstractExchange.Handler() {
				@Override
				public void exchangeError(ChannelHandlerContext ctx, Throwable t) {
					Http2ChannelHandler.this.resetStream(ctx, streamId, Http2Error.INTERNAL_ERROR.code(), ctx.voidPromise());
				}
			});
		} 
		else {
			// Continuation frame
			((Http2RequestHeaders) exchange.request().headers()).getUnderlyingHeaders().add(headers);
		}
	}
	
	@Override
	public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endOfStream) throws Http2Exception {
//        System.out.println("onHeaderReads(1)");
		onHeadersRead(ctx, streamId, headers, padding, endOfStream);
	}

	@Override
	public void onPriorityRead(ChannelHandlerContext ctx, int streamId, int streamDependency, short weight, boolean exclusive) throws Http2Exception {
		// System.out.println("onPriorityRead()");
	}

	@Override
	public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
//        System.out.println("onRstStreamRead()");
		Http2Exchange serverStream = this.serverStreams.remove(streamId);
		if (serverStream != null) {
			serverStream.dispose();
		} 
		else {
			// TODO this should never happen?
//			System.err.println("Unable to reset unmanaged stream " + streamId);
//    		throw new IllegalStateException("Unable to reset unmanaged stream " + streamId);
		}
	}

	@Override
	public void onSettingsAckRead(ChannelHandlerContext ctx) throws Http2Exception {
//        System.out.println("onSettingsAckRead()");
	}

	@Override
	public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {
//        System.out.println("onSettingsRead()");
		if (this.configuration.decompression_enabled()) {
			this.decoder().frameListener(new DelegatingDecompressorFrameListener(decoder().connection(), this));
		} 
		else {
			this.decoder().frameListener(this);
		}
	}

	@Override
	public void onPingRead(ChannelHandlerContext ctx, long data) throws Http2Exception {
//        System.out.println("onPingRead()");
	}

	@Override
	public void onPingAckRead(ChannelHandlerContext ctx, long data) throws Http2Exception {
//        System.out.println("onPingAckRead()");
	}

	@Override
	public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding) throws Http2Exception {
//        System.out.println("onPushPromiseRead()");
	}

	@Override
	public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData)
			throws Http2Exception {
//        System.out.println("onGoAwayRead()");
	}

	@Override
	public void onWindowUpdateRead(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement)
			throws Http2Exception {
//        System.out.println("onWindowUpdateRead()");
	}

	@Override
	public void onUnknownFrame(ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf payload) throws Http2Exception {
//        System.out.println("onUnknownFrame()");
	}

	@Override
	public void onStreamAdded(Http2Stream stream) {
//		System.out.println("Stream added: " + stream.id());
	}

	@Override
	public void onStreamActive(Http2Stream stream) {
//		System.out.println("Stream active: " + stream.id());		
	}

	@Override
	public void onStreamHalfClosed(Http2Stream stream) {
//		System.out.println("Stream half closed");		
	}

	@Override
	public void onStreamClosed(Http2Stream stream) {
//		System.out.println("Stream closed " + stream.id());
		Http2Exchange serverStream = this.serverStreams.remove(stream.id());
		if (serverStream != null) {
			serverStream.dispose();
		} 
		else {
			// TODO this should never happen?
//			System.err.println("Unable to reset unmanaged stream " + stream.id());
//    		throw new IllegalStateException("Unable to reset unmanaged stream " + stream.id());
		}
	}

	@Override
	public void onStreamRemoved(Http2Stream stream) {
//		System.out.println("Stream removed");
	}

	@Override
	public void onGoAwaySent(int lastStreamId, long errorCode, ByteBuf debugData) {
//		System.out.println("Stream go away sent");		
	}

	@Override
	public void onGoAwayReceived(int lastStreamId, long errorCode, ByteBuf debugData) {
//		System.out.println("Stream go away received");		
	}

	/**
	 * <p>
	 * Used to determine the target content encoding of a response based on the
	 * {@code accept-encoding} header of a request.
	 * </p>
	 * 
	 * @author <a href="jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 */
	private static class ContentEncodingResolver extends HttpContentCompressor {

		/**
		 * <p>
		 * Resolves the response content encoding.
		 * </p>
		 * 
		 * @param acceptEncoding the accept encoding header of a request
		 * 
		 * @return a content encoding or null
		 * @throws NullPointerException if acceptEncoding is null
		 */
		public String resolve(String acceptEncoding) throws NullPointerException {
			ZlibWrapper wrapper = super.determineWrapper(acceptEncoding);
			if (wrapper != null) {
				switch(wrapper) {
					case GZIP:
						return "gzip";
					case ZLIB:
						return "deflate";
					default:
						return null;
				}
			}
			return null;
		}
	}
}