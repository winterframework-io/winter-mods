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
package io.winterframework.mod.web;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
	
	Charset getCharset();
	
	Long getSize();
	
	// TODO add more common request headers
	
	Set<String> getNames();
	
	<T extends Header> Optional<T> get(String name);
	
	<T extends Header> List<T> getAll(String name);
	
	Map<String, List<? extends Header>> getAll();
}
