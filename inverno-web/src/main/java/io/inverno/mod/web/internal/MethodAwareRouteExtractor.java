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
package io.inverno.mod.web.internal;

import io.inverno.mod.http.base.Method;
import io.inverno.mod.http.server.Exchange;
import io.inverno.mod.web.Route;
import io.inverno.mod.web.MethodAwareRoute;

/**
 * <p>
 * A route extractor to extract {@link MethodAwareRoute} routes.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 *
 * @param <A> the type of exchange handled by the route
 * @param <B> the route type
 * @param <C> the route extractor type
 */
interface MethodAwareRouteExtractor<A extends Exchange, B extends Route<A>, C extends MethodAwareRouteExtractor<A, B, C>> extends RouteExtractor<A, B> {

	/**
	 * <p>
	 * Sets the extractor to extract routes defined with the specified HTTP method.
	 * </p>
	 * 
	 * @param method the HTTP method of the routes to extract
	 * 
	 * @return the route extractor
	 */
	C method(Method method);
}
