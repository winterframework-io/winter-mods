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

/**
 * <p>
 * Exchange handler used to handle error server exchanges.
 * </p>
 * 
 * <p>
 * The HTTP server relies on an error exchange handler to handle errors thrown
 * during the processing of a client request in order to provide a proper
 * response to the client.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 *
 * @see ErrorExchange
 * @see ExchangeHandler
 * 
 * @param <A> the error type
 */
public interface ErrorExchangeHandler<A extends Throwable, B extends ErrorExchange<A>> extends ExchangeHandler<B> {

}
