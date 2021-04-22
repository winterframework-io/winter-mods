/*
 * Copyright 2021 Jeremy KUHN
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
package io.winterframework.mod.web.internal;

import java.lang.reflect.Type;

import io.netty.buffer.ByteBuf;
import io.winterframework.mod.http.base.InternalServerErrorException;
import io.winterframework.mod.http.base.header.Headers;
import io.winterframework.mod.http.server.ResponseBody;
import io.winterframework.mod.http.server.ResponseData;
import io.winterframework.mod.web.ResponseDataEncoder;
import io.winterframework.mod.web.WebResponse;
import io.winterframework.mod.web.WebResponseBody;

/**
 * <p>
 * Generic {@link WebResponseBody} implementation.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 * @since 1.0
 */
class GenericWebResponseBody implements WebResponseBody {

	private final WebResponse response;
	
	private final ResponseBody responseBody;
	
	private final DataConversionService dataConversionService;
	
	/**
	 * <p>
	 * Creates a generic web response body with the specified underlying response
	 * and response body and data conversion service.
	 * </p>
	 * 
	 * @param response              the underlying response
	 * @param responseBody          the underlying response body
	 * @param dataConversionService the data conversion service
	 */
	public GenericWebResponseBody(WebResponse response, ResponseBody responseBody, DataConversionService dataConversionService) {
		this.response = response;
		this.dataConversionService = dataConversionService;
		this.responseBody = responseBody;
	}

	@Override
	public void empty() {
		this.responseBody.empty();
	}

	@Override
	public ResponseData<ByteBuf> raw() {
		return this.responseBody.raw();
	}
	
	@Override
	public Resource resource() {
		return this.responseBody.resource();
	}
	
	@Override
	public Sse<ByteBuf, ResponseBody.Sse.Event<ByteBuf>, ResponseBody.Sse.EventFactory<ByteBuf, ResponseBody.Sse.Event<ByteBuf>>> sse() {
		return this.responseBody.sse();
	}
	
	@Override
	public <T> SseEncoder<T> sseEncoder(String mediaType) {
		try {
			return this.dataConversionService.createSseEncoder(this.responseBody.sse(), mediaType);
		} 
		catch (NoConverterException e) {
			throw new InternalServerErrorException("No converter found for media type: " + e.getMediaType(), e);
		}
	}
	
	@Override
	public <T> SseEncoder<T> sseEncoder(String mediaType, Class<T> type) {
		try {
			return this.dataConversionService.createSseEncoder(this.responseBody.sse(), mediaType, type);
		} 
		catch (NoConverterException e) {
			throw new InternalServerErrorException("No converter found for media type: " + e.getMediaType(), e);
		}
	}
	
	@Override
	public <T> SseEncoder<T> sseEncoder(String mediaType, Type type) {
		try {
			return this.dataConversionService.createSseEncoder(this.responseBody.sse(), mediaType, type);
		} 
		catch (NoConverterException e) {
			throw new InternalServerErrorException("No converter found for media type: " + e.getMediaType(), e);
		}
	}
	
	@Override
	public <T> ResponseDataEncoder<T> encoder() {
		// if we don't have a content type specified in the response, it means that the route was created without any produces clause so we can fallback to a default representation assuming it is accepted in the request otherwise we should fail
		// - define a default converter in the conversion service
		// - check that the produced media type matches the Accept header
		// => We don't have to do anything, if the media is empty, we don't know what to do anyway, so it is up to the user to explicitly set the content type on the response which is enough to make the conversion works otherwise we must fail
		return this.response.headers().<Headers.ContentType>getHeader(Headers.NAME_CONTENT_TYPE)
			.map(contentType -> {
				try {
					return this.dataConversionService.<T>createEncoder(this.response.body().raw(), contentType.getMediaType());
				} 
				catch (NoConverterException e) {
					throw new InternalServerErrorException("No converter found for media type: " + e.getMediaType(), e);
				}
			})
			.orElseThrow(() -> new InternalServerErrorException("Empty media type"));
	}

	@Override
	public <T> ResponseDataEncoder<T> encoder(Class<T> type) {
		return this.encoder((Type)type);
	}

	@Override
	public <T> ResponseDataEncoder<T> encoder(Type type) {
		// if we don't have a content type specified in the response, it means that the route was created without any produces clause so we can fallback to a default representation assuming it is accepted in the request otherwise we should fail
		// - define a default converter in the conversion service
		// - check that the produced media type matches the Accept header
		// => We don't have to do anything, if the media is empty, we don't know what to do anyway, so it is up to the user to explicitly set the content type on the response which is enough to make the conversion works otherwise we must fail
		return this.response.headers().<Headers.ContentType>getHeader(Headers.NAME_CONTENT_TYPE)
			.map(contentType -> {
				try {
					return this.dataConversionService.<T>createEncoder(this.response.body().raw(), contentType.getMediaType(), type);
				}
				catch (NoConverterException e) {
					throw new InternalServerErrorException("No converter found for media type: " + e.getMediaType(), e);
				}
			})
			.orElseThrow(() -> new InternalServerErrorException("Empty media type"));
	}
}
