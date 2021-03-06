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
package io.inverno.mod.web;

/**
 * <p>
 * An error web route specifies criteria used to determine the error web
 * exchange handler to execute to handle a failing request.
 * </p>
 * 
 * <p>
 * It basically supports the following criteria:
 * </p>
 * 
 * <ul>
 * <li>the type of the error thrown during the regular processing of a
 * request</li>
 * <li>the content type of the resource</li>
 * <li>the language tag of the resource</li>
 * </ul>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 * 
 * @see ErrorWebExchange
 * @see ErrorWebRouter
 */
public interface ErrorWebRoute extends 
	ErrorAwareRoute<ErrorWebExchange<Throwable>>, 
	AcceptAwareRoute<ErrorWebExchange<Throwable>>, 
	Route<ErrorWebExchange<Throwable>> {

}
