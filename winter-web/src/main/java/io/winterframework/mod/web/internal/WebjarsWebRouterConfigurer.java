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
package io.winterframework.mod.web.internal;

import java.net.URI;
import java.net.URISyntaxException;

import io.winterframework.mod.base.resource.ResourceService;
import io.winterframework.mod.http.base.Method;
import io.winterframework.mod.web.StaticHandler;
import io.winterframework.mod.web.WebConfiguration;
import io.winterframework.mod.web.WebExchange;
import io.winterframework.mod.web.WebRouter;
import io.winterframework.mod.web.WebRouterConfigurer;

/**
 * <p>
 * Web router configurer used to configure routes to WebJars resources deployed
 * in the classpath.
 * </p>
 * 
 * <p>
 * When activated in the {@link WebConfiguration#enable_webjars() web module
 * configuration}, this configurer defines as many routes as there are webjars
 * defined on the classpath. It assumes a webjar provides static content under <code>/META-INF/resources/webjars/{@literal <name>}/{@literal <version>}</code> as defined by <a href="https://www.webjars.org">WebJars</a>.
 * </p>
 * 
 * <p>For instance assuming {@code example-webjar} is on the classpath in version {@code 1.2.3}, its resource can be accessed at {@code /webjars/example-webjar/*}.</p> 
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 * @since 1.0
 */
public class WebjarsWebRouterConfigurer implements WebRouterConfigurer<WebExchange> {

	private static final String BASE_WEBJARS_PATH = "/webjars";
	
	private final ResourceService resourceService;
	
	/**
	 * <p>
	 * Creates a WebJars web router configurer with the specified resource service.
	 * </p>
	 * 
	 * @param resourceService the resource service
	 */
	public WebjarsWebRouterConfigurer(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	@Override
	public void accept(WebRouter<WebExchange> router) {
		try {
			this.resourceService.getResources(new URI("classpath:/META-INF/resources/webjars"))
				.flatMap(resource -> this.resourceService.getResources(URI.create(resource.getURI().toString() + "/*/*")))
				.forEach(resource -> {
					String spec = resource.getURI().getSchemeSpecificPart();
					int versionIndex = spec.lastIndexOf("/");
					int webjarIndex = spec.substring(0, versionIndex).lastIndexOf("/");
					
					String webjarRootPath = BASE_WEBJARS_PATH + spec.substring(webjarIndex, versionIndex + 1) + "{path}";
					
					router.route().path(webjarRootPath).method(Method.GET).handler(new StaticHandler(resource));
				});
		} 
		catch (URISyntaxException e) {
			throw new IllegalStateException("Error resolving webjars", e);
		}
	}
}