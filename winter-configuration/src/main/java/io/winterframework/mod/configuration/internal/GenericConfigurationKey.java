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
package io.winterframework.mod.configuration.internal;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import io.winterframework.mod.configuration.ConfigurationKey;

/**
 * @author jkuhn
 *
 */
public class GenericConfigurationKey implements ConfigurationKey {

	protected String name;
	
	protected Map<String, Object> parameters;
	
	public GenericConfigurationKey(String name) {
		this(name, null);
	}
	
	public GenericConfigurationKey(String name, Map<String, Object> parameters) {
		if(name == null || name.equals("")) {
			throw new IllegalArgumentException("Name can't be null or empty");
		}
		this.name = name;
		this.parameters = parameters != null ? Collections.unmodifiableMap(parameters) : Map.of();
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Map<String, Object> getParameters() {
		return this.parameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericConfigurationKey other = (GenericConfigurationKey) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.name + "[" + this.parameters.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().toString()).collect(Collectors.joining(",")) + "]";
	}
}