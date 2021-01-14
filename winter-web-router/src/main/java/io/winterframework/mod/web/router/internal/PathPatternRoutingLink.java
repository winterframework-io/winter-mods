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
package io.winterframework.mod.web.router.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;

import io.winterframework.mod.web.BadRequestException;
import io.winterframework.mod.web.WebException;
import io.winterframework.mod.web.router.PathAwareRoute;
import io.winterframework.mod.web.server.Exchange;

/**
 * @author jkuhn
 *
 */
class PathPatternRoutingLink<A extends Exchange, B extends PathAwareRoute<A>> extends RoutingLink<A, PathPatternRoutingLink<A, B>, B> {

	private Map<PathAwareRoute.PathPattern, RoutingLink<A, ?, B>> handlers;
	
	public PathPatternRoutingLink() {
		super(PathPatternRoutingLink::new);
		this.handlers = new HashMap<>();
	}

	@Override
	public PathPatternRoutingLink<A, B> setRoute(B route) {
		PathAwareRoute.PathPattern pathPattern = route.getPathPattern();
		if(pathPattern != null) {
			if(this.handlers.containsKey(pathPattern)) {
				this.handlers.get(pathPattern).setRoute(route);
			}
			else {
				this.handlers.put(pathPattern, this.nextLink.createNextLink().setRoute(route));
			}
		}
		else {
			this.nextLink.setRoute(route);
		}
		return this;
	}
	
	@Override
	public void enableRoute(B route) {
		PathAwareRoute.PathPattern pathPattern = route.getPathPattern();
		if(pathPattern != null) {
			RoutingLink<A, ?, B> handler = this.handlers.get(pathPattern);
			if(handler != null) {
				handler.enableRoute(route);
			}
			// route doesn't exist so let's do nothing
		}
		else {
			this.nextLink.enableRoute(route);
		}
	}
	
	@Override
	public void disableRoute(B route) {
		PathAwareRoute.PathPattern pathPattern = route.getPathPattern();
		if(pathPattern != null) {
			RoutingLink<A, ?, B> handler = this.handlers.get(pathPattern);
			if(handler != null) {
				handler.disableRoute(route);
			}
			// route doesn't exist so let's do nothing
		}
		else {
			this.nextLink.disableRoute(route);
		}
	}
	
	@Override
	public void removeRoute(B route) {
		PathAwareRoute.PathPattern pathPattern = route.getPathPattern();
		if(pathPattern != null) {
			RoutingLink<A, ?, B> handler = this.handlers.get(pathPattern);
			if(handler != null) {
				handler.removeRoute(route);
				if(!handler.hasRoute()) {
					// The link has no more routes, we can remove it for good 
					this.handlers.remove(pathPattern);
				}
			}
			// route doesn't exist so let's do nothing
		}
		else {
			this.nextLink.removeRoute(route);
		}
	}
	
	@Override
	public boolean hasRoute() {
		return !this.handlers.isEmpty() || this.nextLink.hasRoute();
	}
	
	@Override
	public boolean isDisabled() {
		return this.handlers.values().stream().allMatch(RoutingLink::isDisabled) && this.nextLink.isDisabled();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <F extends RouteExtractor<A, B>> void extractRoute(F extractor) {
		if(!(extractor instanceof PathAwareRouteExtractor)) {
			throw new IllegalArgumentException("Route extractor is not path aware");
		}
		this.handlers.entrySet().stream().forEach(e -> {
			e.getValue().extractRoute(((PathAwareRouteExtractor<A, B, ?>)extractor).pathPattern(e.getKey()));
		});
		super.extractRoute(extractor);
	}

	private PathPatternMatch<A, B> matches(String path, PathAwareRoute.PathPattern pathPattern, RoutingLink<A, ?, B> handler) {
		Matcher matcher = pathPattern.getPattern().matcher(path);
		if(matcher.matches()) {
			return new PathPatternMatch<>(matcher, pathPattern, handler);
		}
		return null;
	}
	
	private static class PathPatternMatch<A extends Exchange, B extends PathAwareRoute<A>> implements Comparable<PathPatternMatch<A, B>> {
		
		private Matcher matcher;
		
		private PathAwareRoute.PathPattern pathPattern;
		
		private RoutingLink<A, ?, B> handler;
		
		public PathPatternMatch(Matcher matcher, PathAwareRoute.PathPattern pathPattern, RoutingLink<A, ?, B> handler) {
			this.matcher = matcher;
			this.pathPattern = pathPattern;
			this.handler = handler;
		}
		
		public Map<String, String> getPathParameters() {
			Map<String, String> pathParameters = new HashMap<>();
			int matcherIndex = 1;
			for(String pathParameterName : this.pathPattern.getPathParameterNames()) {
				if(pathParameterName != null && !pathParameterName.equals(":")) {
					pathParameters.put(pathParameterName.substring(1), this.matcher.group(matcherIndex));
				}
				matcherIndex++;
			}
			return pathParameters;
		}
		
		public RoutingLink<A, ?, B> getHandler() {
			return this.handler;
		}

		@Override
		public int compareTo(PathPatternMatch<A, B> other) {
			// /toto/tata/titi/{}... > /toto/tata/{}...
			// /toto/tata/titi/{}... > /toto/tata/{}
			// /toto/{}/tutu > /toto/{}/{}
			// /toto/tutu > /toto/{:.*}
			// /toto/{}/tata > /toto/{:.*}
			// /toto/{}/{} > /toto/{:.*} 
			
			int groupIndex = 1;
			while (groupIndex < Math.min(this.matcher.groupCount(), other.matcher.groupCount())) {
				String thisGroup = this.matcher.group(groupIndex);
				boolean thisRegex = this.pathPattern.getPathParameterNames().get(groupIndex - 1) != null;
				
				String otherGroup = other.matcher.group(groupIndex);
				boolean otherRegex = other.pathPattern.getPathParameterNames().get(groupIndex - 1) != null;

				if(thisGroup.length() < otherGroup.length()) {
					if(!otherRegex) {
						return -1;
					}
					else {
						return 1;
					}
				}
				else if(thisGroup.length() > otherGroup.length()) {
					if(!thisRegex) {
						return 1;
					}
					else {
						return -1;
					}
				}
				else {
					if(!thisRegex && otherRegex) {
						return 1;
					}
					else if(thisRegex && !otherRegex) {
						return -1;
					}
				}
				groupIndex++;
			}
			
			if(this.matcher.groupCount() < other.matcher.groupCount()) {
				return 1;
			}
			else if(this.matcher.groupCount() > other.matcher.groupCount()) {
				return -1;
			}
			return 0;
		}
	}

	@Override
	public void handle(A exchange) throws WebException {
		if(this.handlers.isEmpty()) {
			this.nextLink.handle(exchange);
		}
		else {
			// TODO this is done twice after the pathroutinglink...
			String normalizedPath;
			try {
				normalizedPath = new URI(exchange.request().headers().getPath()).normalize().getPath().toString();
			} 
			catch (URISyntaxException e) {
				throw new BadRequestException("Bad URI", e);
			}
			
			// TODO If we have more than one, we need to prioritize them or we need to fail depending on a routing strategy
			// - we can choose to trust the Winter compiler to detect conflicts and have a defaulting behavior at runtime
			// - we can choose to make this behavior configurable
			// ==> for the time being let's just take the first one
			Optional<RoutingLink<A, ?, B>> handler = this.handlers.entrySet().stream()
				.map(e -> this.matches(normalizedPath, e.getKey(), e.getValue()))
				.filter(Objects::nonNull)
				.sorted(Comparator.reverseOrder())
				.findFirst()
				.map(match -> {
					if(exchange instanceof GenericWebExchange) {
						((GenericWebExchange)exchange).setPathParameters(match.getPathParameters());
					}
					return match.getHandler();
				});
			
			if(handler.isPresent()) {
				handler.get().handle(exchange);
			}
			else {
				this.nextLink.handle(exchange);
			}
		}
	}
}