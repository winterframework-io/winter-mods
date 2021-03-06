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
package io.inverno.mod.http.server;

import io.netty.buffer.ByteBuf;
import io.inverno.mod.http.base.Parameter;

/**
 * <p>
 * Represents the payload body of a client request in a server exchange.
 * </p>
 * 
 * <p>
 * The request body basically provides multiple ways to consume the request
 * payload depending on the request content type.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 * 
 * @see Request
 */
public interface RequestBody {
	
	/**
	 * <p>
	 * Returns a raw payload consumer.
	 * </p>
	 * 
	 * @return the raw data
	 * 
	 * @throws IllegalStateException if the payload has already been consumed using
	 *                               another decoder
	 */
	RequestData<ByteBuf> raw() throws IllegalStateException;
	
	/**
	 * <p>
	 * Returns a multipart/form-data payload consumer.
	 * </p>
	 * 
	 * @return body a multipart/form-data payload consumer
	 * 
	 * @throws IllegalStateException if the payload has already been consumed using
	 *                               another decoder
	 */
	RequestBody.Multipart<? extends Part> multipart() throws IllegalStateException;
	
	/**
	 * <p>
	 * Returns an application/x-www-form-urlencoded payload consumer.
	 * </p>
	 * 
	 * @return body an application/x-www-form-urlencoded payload consumer
	 * 
	 * @throws IllegalStateException if the payload has already been consumed using
	 *                               another decoder
	 */
	RequestBody.UrlEncoded urlEncoded() throws IllegalStateException;

	/**
	 * <p>
	 * A multipart/form-data consumer as defined by
	 * <a href="https://tools.ietf.org/html/rfc7578">RFC 7578</a>.
	 * </p>
	 * 
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 * 
	 * @param <A> the part type
	 */
	public static interface Multipart<A extends Part> extends RequestData<A> {
	}
	
	/**
	 * <p>
	 * An application/x-www-form-urlencoded data consumer as defined by <a href=
	 * "https://url.spec.whatwg.org/#application/x-www-form-urlencoded">application/x-www-form-urlencoded</a>.
	 * </p>
	 * 
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 */
	public static interface UrlEncoded extends RequestData<Parameter> {
	}
}
