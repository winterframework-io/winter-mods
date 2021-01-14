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

import io.winterframework.mod.web.Method;
import io.winterframework.mod.web.header.Header;

/**
 * @author jkuhn
 *
 */
public interface RequestHeaders {
	
	String getAuthority();
	
	String getPath();
	
	Method getMethod();
	
	String getScheme();

	String getContentType();
	
	Long getContentLength();
	
	Set<String> getNames();
	
	Optional<String> get(CharSequence name);
	
	List<String> getAll(CharSequence name);
	
	List<Map.Entry<String, String>> getAll();
	
	<T extends Header> Optional<T> getHeader(CharSequence name);
	
	<T extends Header> List<T> getAllHeader(CharSequence name);
	
	List<Header> getAllHeader();
	
	boolean contains(CharSequence name, CharSequence value);
}