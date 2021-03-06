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

import io.inverno.mod.http.server.Exchange;

/**
 * <p>
 * Base router configurer interface.
 * </p>
 * 
 * <p>
 * A router configurer is used to configure a router.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 * 
 * @see Router
 * 
 * @param <A> the route exchange type
 * @param <B> the router type
 * @param <C> the route manager type
 * @param <D> the route type
 * @param <E> the router exchange type
 */
public interface RouterConfigurer<A extends Exchange, B extends Router<A, B, C, D, E>, C extends RouteManager<A, B, C, D, E>, D extends Route<A>, E extends Exchange> extends Consumer<B> {

}
