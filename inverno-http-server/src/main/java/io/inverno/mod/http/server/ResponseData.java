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
package io.inverno.mod.http.server;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Mono;

/**
 * <p>
 * A generic response payload producer.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 * 
 * @see ResponseBody#raw()
 * @see ResponseBody.Sse.Event
 * 
 * @param <A> the type of data
 */
@FunctionalInterface
public interface ResponseData<A> {

	/**
	 * <p>
	 * Sets the payload data.
	 * </p>
	 * 
	 * @param <T>   the type of data
	 * @param value the data publisher
	 * 
	 * @throws IllegalStateException if the payload has already been set
	 */
	<T extends A> void stream(Publisher<T> value) throws IllegalStateException;
	
	/**
	 * <p>
	 * Sets the specified data.
	 * </p>
	 * 
	 * @param <T>   the type of data
	 * @param value the value to set
	 * 
	 * @throws IllegalStateException if the payload has already been set
	 */
	default <T extends A> void value(T value) throws IllegalStateException {
		this.stream(Mono.just(value));
	}
}
