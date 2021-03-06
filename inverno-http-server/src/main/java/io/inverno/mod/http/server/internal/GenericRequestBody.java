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
package io.inverno.mod.http.server.internal;

import java.util.Optional;

import org.reactivestreams.Publisher;

import io.netty.buffer.ByteBuf;
import io.inverno.mod.http.base.Parameter;
import io.inverno.mod.http.base.header.Headers;
import io.inverno.mod.http.server.Part;
import io.inverno.mod.http.server.RequestBody;
import io.inverno.mod.http.server.RequestData;
import io.inverno.mod.http.server.internal.multipart.MultipartDecoder;
import reactor.core.publisher.Flux;

/**
 * <p>
 * Generic {@link RequestBody} implementation.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
public class GenericRequestBody implements RequestBody {
	
	private Flux<ByteBuf> data;
	private Optional<Headers.ContentType> contentType;
	
	private RequestData<ByteBuf> rawData;
	private RequestBody.UrlEncoded urlEncodedData;
	private RequestBody.Multipart<Part> multipartData;
	
	private MultipartDecoder<Parameter> urlEncodedBodyDecoder;
	private MultipartDecoder<Part> multipartBodyDecoder;

	/**
	 * <p>
	 * Creates a request body with the specified content type, url encoded body
	 * decoder, multipart body decoder and payload data publisher.
	 * </p>
	 * 
	 * @param contentType           the request content type
	 * @param urlEncodedBodyDecoder the application/x-www-form-urlencoded body decoder
	 * @param multipartBodyDecoder  the multipart/form-data body decoder
	 * @param data                  the payload data publisher
	 */
	public GenericRequestBody(Optional<Headers.ContentType> contentType, MultipartDecoder<Parameter> urlEncodedBodyDecoder, MultipartDecoder<Part> multipartBodyDecoder, Flux<ByteBuf> data) {
		this.contentType = contentType;
		this.urlEncodedBodyDecoder = urlEncodedBodyDecoder;
		this.multipartBodyDecoder = multipartBodyDecoder;
		this.data = data;
	}
	
	@Override
	public RequestData<ByteBuf> raw() {
		// We don't need to check whether another data method has been invoke since the data Flux is a unicast Flux, an IllegalStateSxception will be thrown if multiple subscriptions are made
		if(this.rawData == null) {
			this.rawData = new GenericRequestBodyRawData();
		}
		return this.rawData;
	}

	@Override
	public RequestBody.Multipart<Part> multipart() {
		// We don't need to check whether another data method has been invoke since the data Flux is a unicast Flux, an IllegalStateSxception will be thrown if multiple subscriptions are made
		if(this.multipartData == null) {
			this.multipartData = new GenericRequestBodyMultipartData(this.multipartBodyDecoder.decode(this.data, this.contentType.orElse(null)));
		}
		return this.multipartData;
	}

	@Override
	public RequestBody.UrlEncoded urlEncoded() {
		// We don't need to check whether another data method has been invoke since the data Flux is a unicast Flux, an IllegalStateSxception will be thrown if multiple subscriptions are made
		if(this.urlEncodedData == null) {
			this.urlEncodedData = new GenericRequestBodyUrlEncodedData(this.urlEncodedBodyDecoder.decode(this.data, this.contentType.orElse(null)));
		}
		return this.urlEncodedData;
	}
	
	private class GenericRequestBodyRawData implements RequestData<ByteBuf> {

		@Override
		public Publisher<ByteBuf> stream() {
			return GenericRequestBody.this.data;
		}
	}

	/**
	 * <p>
	 * Generic {@link RequestBody.UrlEncoded} implementation.
	 * </p>
	 * 
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 */
	private class GenericRequestBodyUrlEncodedData implements RequestBody.UrlEncoded {

		private Publisher<Parameter> parameters;
		
		/**
		 * <p>
		 * Creates an application/x-www-form-urlencoded data consumer with the specified
		 * source of parameters.
		 * </p>
		 * 
		 * @param parameters the parameter publisher
		 */
		public GenericRequestBodyUrlEncodedData(Publisher<Parameter> parameters) {
			this.parameters = parameters;
		}

		@Override
		public Publisher<Parameter> stream() {
			return this.parameters;
		}
	}
	
	/**
	 * <p>
	 * Generic {@link RequestBody.Multipart} implementation.
	 * </p>
	 * 
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 */
	private class GenericRequestBodyMultipartData implements RequestBody.Multipart<Part> {

		private Publisher<Part> parts;
		
		/**
		 * <p>
		 * Creates a multipart/form-data consumer with the specified source of parts.
		 * </p>
		 * 
		 * @param parameters the parameter publisher
		 */
		public GenericRequestBodyMultipartData(Publisher<Part> parts) {
			this.parts = parts;
		}

		@Override
		public Publisher<Part> stream() {
			return this.parts;
		}
	}
}
