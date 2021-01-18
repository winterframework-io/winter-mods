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

/**
 * 
 * @author jkuhn
 *
 */
@io.winterframework.core.annotation.Module(excludes = {"io.winterframework.mod.web"})
module io.winterframework.mod.web.router {
	requires io.winterframework.core;
	requires io.winterframework.core.compiler;
	
	requires transitive io.winterframework.mod.base;
	requires transitive io.winterframework.mod.web;
	
	requires reactor.core;
	requires org.reactivestreams;
	
	exports io.winterframework.mod.web.router;
	exports io.winterframework.mod.web.router.annotation;
}