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
package io.winterframework.mod.web.server;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.winterframework.mod.web.Status;
import io.winterframework.mod.web.header.Header;
import io.winterframework.mod.web.header.Headers;

/**
 * @author jkuhn
 *
 */
public interface ResponseHeaders {

	ResponseHeaders status(Status status);
	
	ResponseHeaders status(int status);
	
	ResponseHeaders contentType(String contentType);
	
	ResponseHeaders contentLength(long length);
	
	ResponseHeaders add(CharSequence name, CharSequence value);
	
	ResponseHeaders add(Header... headers);
	
	ResponseHeaders set(CharSequence name, CharSequence value);
	
	ResponseHeaders set(Header... headers);
	
	ResponseHeaders remove(CharSequence... names);
	
	int getStatus();
	
	Optional<String> getContentType();
	
	Optional<Headers.ContentType> getContentTypeHeader();
	
	Set<String> getNames();
	
	Optional<String> get(CharSequence name);
	
	List<String> getAll(CharSequence name);
	
	List<Map.Entry<String, String>> getAll();
	
	<T extends Header> Optional<T> getHeader(CharSequence name);
	
	<T extends Header> List<T> getAllHeader(CharSequence name);
	
	List<Header> getAllHeader();
	
	boolean contains(CharSequence name, CharSequence value);
	
}
