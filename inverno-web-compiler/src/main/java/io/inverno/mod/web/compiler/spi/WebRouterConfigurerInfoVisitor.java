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
package io.inverno.mod.web.compiler.spi;

/**
 * <p>
 * A web router configurer info visitor is used to process a web router
 * configurer info.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 *
 * @param <R> the visitor result type
 * @param <P> the visitor parameter type
 */
public interface WebRouterConfigurerInfoVisitor<R, P> {

	/**
	 * <p>
	 * Visits web router configurer info.
	 * </p>
	 * 
	 * @param routerConfigurerInfo the info to visit
	 * @param p                    a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebRouterConfigurerInfo routerConfigurerInfo, P p);

	/**
	 * <p>
	 * Visits web provided router configurer info.
	 * </p>
	 * 
	 * @param providedRouterConfigurerInfo the info to visit
	 * @param p                            a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebProvidedRouterConfigurerInfo providedRouterConfigurerInfo, P p);

	/**
	 * <p>
	 * Visits web controller info.
	 * </p>
	 * 
	 * @param controllerInfo the info to visit
	 * @param p              a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebControllerInfo controllerInfo, P p);

	/**
	 * <p>
	 * Visits web route info.
	 * </p>
	 * 
	 * @param routeInfo the info to visit
	 * @param p         a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebRouteInfo routeInfo, P p);

	/**
	 * <p>
	 * Visits web response body info.
	 * </p>
	 * 
	 * @param responseBodyInfo the info to visit
	 * @param p                a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebResponseBodyInfo responseBodyInfo, P p);

	/**
	 * <p>
	 * Visits web parameter info.
	 * </p>
	 * 
	 * @param parameterInfo the info to visit
	 * @param p             a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebParameterInfo parameterInfo, P p);

	/**
	 * <p>
	 * Visits web basic parameter info.
	 * </p>
	 * 
	 * @param basicParameterInfo the info to visit
	 * @param p                  a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebBasicParameterInfo basicParameterInfo, P p);

	/**
	 * <p>
	 * Visits web cookie parameter info.
	 * </p>
	 * 
	 * @param cookieParameterInfo the info to visit
	 * @param p                   a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebCookieParameterInfo cookieParameterInfo, P p);

	/**
	 * <p>
	 * Visits web form parameter info.
	 * </p>
	 * 
	 * @param formParameterInfo the info to visit
	 * @param p                 a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebFormParameterInfo formParameterInfo, P p);

	/**
	 * <p>
	 * Visits web header parameter info.
	 * </p>
	 * 
	 * @param headerParameterInfo the info to visit
	 * @param p                   a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebHeaderParameterInfo headerParameterInfo, P p);

	/**
	 * <p>
	 * Visits web path parameter info.
	 * </p>
	 * 
	 * @param pathParameterInfo the info to visit
	 * @param p                 a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebPathParameterInfo pathParameterInfo, P p);

	/**
	 * <p>
	 * Visits web query parameter info.
	 * </p>
	 * 
	 * @param queryParameterInfo the info to visit
	 * @param p                  a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebQueryParameterInfo queryParameterInfo, P p);

	/**
	 * <p>
	 * Visits web body parameter info.
	 * </p>
	 * 
	 * @param bodyParameterInfo the info to visit
	 * @param p                 a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebRequestBodyParameterInfo bodyParameterInfo, P p);

	/**
	 * <p>
	 * Visits web exchange parameter info.
	 * </p>
	 * 
	 * @param exchangeParameterInfo the info to visit
	 * @param p                     a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebExchangeParameterInfo exchangeParameterInfo, P p);

	/**
	 * <p>
	 * Visits web server-sent event factory parameter info.
	 * </p>
	 * 
	 * @param sseEventFactoryParameterInfo the info to visit
	 * @param p                            a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WebSseEventFactoryParameterInfo sseEventFactoryParameterInfo, P p);
}
