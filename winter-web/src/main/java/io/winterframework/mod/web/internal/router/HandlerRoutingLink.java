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
package io.winterframework.mod.web.internal.router;

import io.winterframework.mod.web.Exchange;
import io.winterframework.mod.web.ExchangeHandler;
import io.winterframework.mod.web.WebException;
import io.winterframework.mod.web.router.Route;

/**
 * @author jkuhn
 *
 */
class HandlerRoutingLink<A, B, C extends Exchange<A, B>, D extends Route<A, B, C>> extends RoutingLink<A, B, C, HandlerRoutingLink<A, B, C, D>, D> {

	private ExchangeHandler<A, B, C> handler;
	
	private boolean disabled;
	
	public HandlerRoutingLink() {
		super(HandlerRoutingLink::new);
	}
	
	public HandlerRoutingLink<A, B, C, D> setRoute(D route) {
		this.handler = route.getHandler();
		return this;
	}
	
	@Override
	public <F extends RouteExtractor<A, B, C, D>> void extractRoute(F extractor) {
		super.extractRoute(extractor);
		extractor.handler(this.handler, this.disabled);
	}
	
	@Override
	public void enableRoute(D route) {
		if(this.handler != null) {
			this.disabled = false;
		}
	}
	
	@Override
	public void disableRoute(D route) {
		if(this.handler != null) {
			this.disabled = true;
		}
	}
	
	@Override
	public void removeRoute(D route) {
		this.handler = null;
	}
	
	@Override
	public boolean hasRoute() {
		return this.handler != null;
	}
	
	@Override
	public boolean isDisabled() {
		return this.disabled;
	}
	
	@Override
	public void handle(C exchange) throws WebException {
		if(this.handler == null) {
			throw new RouteNotFoundException();
		}
		if(this.disabled) {
			throw new DisabledRouteException();
		}
		this.handler.handle(exchange);
	}
}
