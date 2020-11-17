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
package io.winterframework.mod.web.lab.router;

import java.util.Map;
import java.util.Optional;

/**
 * @author jkuhn
 *
 */
public interface BaseContext {

	void setAttribute(String name, Object value);
	
	void removeAttribute(String name);
	
	<T> Optional<T> getAttribute(String name);
	
	Map<String, Object> getAttributes();
	
	// TODO we could return a PathParameter object here so we can expose some basic value conversion
	Map<String, String> getPathParameters();
}
