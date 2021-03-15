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
package io.winterframework.mod.web;

import io.netty.buffer.ByteBuf;
import io.winterframework.mod.base.converter.MediaTypeConverter;
import io.winterframework.mod.http.server.ResponseData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>
 * A response payload producer used to encode the payload from a single object
 * or many objects.
 * </p>
 * 
 * <p>
 * Implementors should rely on a {@link MediaTypeConverter} to encode a payload
 * as a publisher of objects to a raw payload as a publisher of {@link ByteBuf}.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 * @since 1.0
 * 
 * @see MediaTypeConverter
 * 
 * @param <A> the type of the object to encode
 */
public interface ResponseDataEncoder<A> extends ResponseData<A> {
	
	/**
	 * <p>
	 * Encodes many objects.
	 * </p>
	 * 
	 * @param <T>   the type of the object to encode
	 * @param value a flux emitting the objects to encode
	 */
	<T extends A> void many(Flux<T> value);
	
	/**
	 * <p>
	 * Encodes one object.
	 * </p>
	 * 
	 * @param <T>   the type of the object to encode
	 * @param value a mono emitting the object to encode
	 */
	<T extends A> void one(Mono<T> value);
}