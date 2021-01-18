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
package io.winterframework.mod.web.internal.header;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpConstants;
import io.winterframework.core.annotation.Bean;
import io.winterframework.mod.base.Charsets;
import io.winterframework.mod.web.header.Header;
import io.winterframework.mod.web.header.HeaderCodec;
import io.winterframework.mod.web.header.HeaderService;

/**
 * @author jkuhn
 *
 */
@Bean(name = "headerService")
public class GenericHeaderService implements HeaderService {

	private Map<String, HeaderCodec<?>> codecs;
	
	private HeaderCodec<?> defaultCodec;
	
	public GenericHeaderService(List<HeaderCodec<?>> codecs) {
		this.codecs = new HashMap<>();
		
		for(HeaderCodec<?> codec : codecs) {
			for(String supportedHeaderName : codec.getSupportedHeaderNames()) {
				supportedHeaderName = supportedHeaderName.toLowerCase();
				// TODO at some point this is an issue in Spring as well, we should fix this in winter
				// provide annotation for sorting at compile time and be able to inject maps as well 
				// - annotations defined on the beans with some meta data
				// - annotations defined on multiple bean socket to specify sorting for list, array or sets
				// - we can also group by key to inject a map => new multi socket type
				// - this is a bit tricky as for selector when it comes to the injection of list along with single values 
				HeaderCodec<?> previousCodec = this.codecs.put(supportedHeaderName, codec);
				if(previousCodec != null) {
					throw new IllegalStateException("Multiple codecs found for header " + supportedHeaderName + ": " + previousCodec.toString() + ", " + codec.toString());
				}
			}
		}
		
		this.defaultCodec = this.codecs.get("*");
		if(this.defaultCodec == null) {
			this.defaultCodec = new GenericHeaderCodec();
		}
	}
	
	@Override
	public <T extends Header> T decode(String header) {
		ByteBuf buffer = Unpooled.copiedBuffer(header, Charsets.UTF_8);
		buffer.writeByte(HttpConstants.LF);
		try {
			return this.decode(buffer, Charsets.UTF_8);
		}
		finally {
			buffer.release();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Header> T decode(ByteBuf buffer, Charset charset) {
		int readerIndex = buffer.readerIndex();
		Charset charsetOrDefault = Charsets.orDefault(charset);
		String name = this.readName(buffer, charsetOrDefault);
		
		if(name == null) {
			return null;
		}
		
		T result = this.<T>getHeaderCodec(name).orElse((HeaderCodec<T>)this.defaultCodec).decode(name, buffer, charsetOrDefault);
		if(result == null) {
			buffer.readerIndex(readerIndex);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Header> String encode(T headerField) {
		return this.<T>getHeaderCodec(headerField.getHeaderName()).orElse((HeaderCodec<T>)this.defaultCodec).encode(headerField);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Header> void encode(T headerField, ByteBuf buffer, Charset charset) {
		Charset charsetOrDefault = Charsets.orDefault(charset);
		this.<T>getHeaderCodec(headerField.getHeaderName()).orElse((HeaderCodec<T>)this.defaultCodec).encode(headerField, buffer, charsetOrDefault);
	}

	@Override
	public <T extends Header> T decode(String name, String value) {
		ByteBuf buffer = Unpooled.copiedBuffer(value, Charsets.UTF_8);
		buffer.writeByte(HttpConstants.LF);
		try {
			return this.decode(name, buffer, Charsets.UTF_8);
		}
		finally {
			buffer.release();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Header> T decode(String name, ByteBuf buffer, Charset charset) {
		return this.<T>getHeaderCodec(name).orElse((HeaderCodec<T>)this.defaultCodec).decode(name, buffer, charset);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Header> String encodeValue(T headerField) {
		return this.<T>getHeaderCodec(headerField.getHeaderName()).orElse((HeaderCodec<T>)this.defaultCodec).encodeValue(headerField);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Header> void encodeValue(T headerField, ByteBuf buffer, Charset charset) {
		Charset charsetOrDefault = Charsets.orDefault(charset);
		this.<T>getHeaderCodec(headerField.getHeaderName()).orElse((HeaderCodec<T>)this.defaultCodec).encodeValue(headerField, buffer, charsetOrDefault);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Header> Optional<HeaderCodec<T>> getHeaderCodec(String name) {
		return Optional.ofNullable((HeaderCodec<T>)this.codecs.get(name));
	}
	
	private String readName(ByteBuf buffer, Charset charset) {
		int readerIndex = buffer.readerIndex();
		Integer startIndex = null;
		Integer endIndex = null;
		while(buffer.isReadable()) {
			 byte nextByte = buffer.readByte();

			 if(startIndex == null && Character.isWhitespace(nextByte)) {
				 continue;
			 }
			 
			 if(startIndex == null) {
				 startIndex = buffer.readerIndex() - 1;
			 }
			 
			 if(nextByte == ':') {
				 endIndex = buffer.readerIndex() - 1;
				 if(startIndex == endIndex) {
					 buffer.readerIndex(readerIndex);
					 throw new MalformedHeaderException("Malformed Header: empty name");
				 }
				 return buffer.slice(startIndex, endIndex - startIndex).toString(charset).toLowerCase();
			 }
			 else if(Character.isWhitespace(nextByte)) {
				 // There's a white space between the header name and the colon
				 buffer.readerIndex(readerIndex);
				 throw new MalformedHeaderException("Malformed Header: name can't contain white space");
			 }
			 else if(!HeaderService.isTokenCharacter((char)nextByte)) {
				 buffer.readerIndex(readerIndex);
				 buffer.readerIndex(readerIndex);
				 throw new MalformedHeaderException("Malformed Header: " + (buffer.readerIndex()-1) + " " + buffer.toString(Charsets.UTF_8) + " " + String.valueOf(Character.toChars(nextByte)));
			 }
		}
		buffer.readerIndex(readerIndex);
		return null;
	}
}
