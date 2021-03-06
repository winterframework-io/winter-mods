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
package io.inverno.mod.web;

import java.util.function.Consumer;

import io.inverno.mod.http.server.Response;
import io.inverno.mod.http.server.ResponseCookies;
import io.inverno.mod.http.server.ResponseHeaders;
import io.inverno.mod.http.server.ResponseTrailers;

/**
 * <p>
 * A request with supports for body encoding based on the response content type.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 * 
 * @see WebExchange
 */
public interface WebResponse extends Response {

	@Override
	WebResponseBody body();
	
	@Override
	WebResponse headers(Consumer<ResponseHeaders> headersConfigurer) throws IllegalStateException;
	
	@Override
	WebResponse trailers(Consumer<ResponseTrailers> trailersConfigurer);
	
	@Override
	WebResponse cookies(Consumer<ResponseCookies> cookiesConfigurer) throws IllegalStateException;
}
