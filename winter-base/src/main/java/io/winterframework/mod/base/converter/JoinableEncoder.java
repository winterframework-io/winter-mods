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
package io.winterframework.mod.base.converter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * @author jkuhn
 *
 */
public interface JoinableEncoder<From, To> extends Encoder<From, To> {

	<T extends From> To encodeList(List<T> value);
	<T extends From> To encodeList(List<T> value, Class<T> type);
	<T extends From> To encodeList(List<T> value, Type type);
	
	<T extends From> To encodeSet(Set<T> value);
	<T extends From> To encodeSet(Set<T> value, Class<T> type);
	<T extends From> To encodeSet(Set<T> value, Type type);
	
	<T extends From> To encodeArray(T[] value);
	<T extends From> To encodeArray(T[] value, Class<T> type);
	<T extends From> To encodeArray(T[] value, Type type);
}
