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
package io.inverno.mod.web.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import io.inverno.mod.base.resource.ModuleResource;
import io.inverno.mod.base.resource.Resource;
import io.inverno.mod.base.resource.ResourceService;
import io.inverno.mod.http.base.Method;
import io.inverno.mod.web.StaticHandler;
import io.inverno.mod.web.WebConfiguration;
import io.inverno.mod.web.WebExchange;
import io.inverno.mod.web.WebRouter;
import io.inverno.mod.web.WebRouterConfigurer;

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
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
public class WebjarsWebRouterConfigurer implements WebRouterConfigurer<WebExchange> {

	private static final String WEBJARS_MODULE_PREFIX = "org.webjars";
	
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
		
		/* 2 possibilities:
		 * - modular webjar
		 *   - /[module_name]/webjars/* -> module://[module_name]/META-INF/resources/webjars/[module_name]/[module_version]/* 
		 *   => WE HAVE TO modify the modularized webjars so that the path is correct
		 * - no modular webjar (ie. we haven't found the requested module) fallback to classpath
		 *   - /[module_name]/webjars/* -> classpath:/META-INF/resources/webjars
		 */
		
		ModuleLayer moduleLayer = this.getClass().getModule().getLayer();
		if(moduleLayer != null) {
			moduleLayer.modules().stream().filter(module -> module.getName().startsWith(WEBJARS_MODULE_PREFIX) && module.getDescriptor().rawVersion().isPresent()).forEach(module -> {
				String webjarName = module.getDescriptor().name().substring(WEBJARS_MODULE_PREFIX.length() + 1);
				String webjarVersion = module.getDescriptor().rawVersion().get();
				Resource baseResource = this.resourceService.getResource(URI.create(ModuleResource.SCHEME_MODULE + "://" + module.getName() + "/META-INF/resources/webjars/" + webjarName + "/" + webjarVersion + "/"));
				String webjarRootPath = WebjarsWebRouterConfigurer.BASE_WEBJARS_PATH + "/" + webjarName + "/{path:.*}";
				router.route().path(webjarRootPath).method(Method.GET).handler(new StaticHandler(baseResource));
			});
		}
		
		try {
			this.resourceService.getResources(new URI("classpath:/META-INF/resources/webjars"))
				.flatMap(resource -> this.resourceService.getResources(URI.create(resource.getURI().toString() + "/*/*")))
				.forEach(baseResource -> {
					String spec = baseResource.getURI().getSchemeSpecificPart();
					int versionIndex = spec.lastIndexOf("/");
					int webjarIndex = spec.substring(0, versionIndex).lastIndexOf("/");
					
					String webjarName = toModuleName(spec.substring(webjarIndex + 1, versionIndex));
					String webjarRootPath = WebjarsWebRouterConfigurer.BASE_WEBJARS_PATH + "/" + webjarName + "/{path:.*}";
					router.route().path(webjarRootPath).method(Method.GET).handler(new StaticHandler(baseResource));
				});
		} 
		catch (URISyntaxException e) {
			throw new IllegalStateException("Error resolving webjars", e);
		}
	}
	
    private static final Pattern NON_ALPHANUM = Pattern.compile("[^A-Za-z0-9]");
    private static final Pattern REPEATING_DOTS = Pattern.compile("(\\.)(\\1)+");
    private static final Pattern LEADING_DOTS = Pattern.compile("^\\.");
    private static final Pattern TRAILING_DOTS = Pattern.compile("\\.$");
	
    /*
	 * Borrowed from jdk.internal.module.ModulePath#cleanModuleName
	 */
	private static String toModuleName(String mn) {
        // replace non-alphanumeric
        mn = WebjarsWebRouterConfigurer.NON_ALPHANUM.matcher(mn).replaceAll(".");

        // collapse repeating dots
        mn = WebjarsWebRouterConfigurer.REPEATING_DOTS.matcher(mn).replaceAll(".");

        // drop leading dots
        if (!mn.isEmpty() && mn.charAt(0) == '.')
            mn = WebjarsWebRouterConfigurer.LEADING_DOTS.matcher(mn).replaceAll("");

        // drop trailing dots
        int len = mn.length();
        if (len > 0 && mn.charAt(len-1) == '.')
            mn = WebjarsWebRouterConfigurer.TRAILING_DOTS.matcher(mn).replaceAll("");

        return mn;
    }
}
