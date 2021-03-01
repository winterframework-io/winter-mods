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
package io.winterframework.mod.base.resource;

import java.net.URI;
import java.util.concurrent.ExecutorService;

/**
 * @author jkuhn
 *
 */
public interface AsyncResourceProvider<A extends AsyncResource> extends ResourceProvider<A> {

	default A getResource(URI uri, ExecutorService executor) throws IllegalArgumentException, ResourceException {
		A resource = this.getResource(uri);
		resource.setExecutor(executor);
		return resource;
	}
}
