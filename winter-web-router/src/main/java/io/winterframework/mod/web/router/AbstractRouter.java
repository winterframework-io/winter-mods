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
package io.winterframework.mod.web.router;

import java.util.Set;
import java.util.function.Consumer;

import io.winterframework.mod.web.server.Exchange;
import io.winterframework.mod.web.server.ExchangeHandler;

/**
 * @author jkuhn
 *
 */
public interface AbstractRouter<A extends Exchange, B extends AbstractRouter<A, B, C, D, E>, C extends AbstractRouteManager<A, B, C, D, E>, D extends AbstractRoute<A>, E extends Exchange> extends ExchangeHandler<E> {
	
	C route();
	
	@SuppressWarnings("unchecked")
	default B route(Consumer<C> routeConfigurer) {
		routeConfigurer.accept(this.route());
		return (B)this;
	}
	
	Set<D> getRoutes();
	
	// interceptors?
}
