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
package io.winterframework.mod.configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.winterframework.mod.base.converter.PrimitiveDecoder;
import io.winterframework.mod.configuration.ConfigurationKey.Parameter;
import io.winterframework.mod.configuration.internal.GenericConfigurationKey;
import io.winterframework.mod.configuration.internal.GenericConfigurationQueryResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author jkuhn
 *
 */
public abstract class AbstractHashConfigurationSource<A, B extends AbstractHashConfigurationSource<A, B>> extends AbstractConfigurationSource<AbstractHashConfigurationSource.HashConfigurationQuery<A, B>, AbstractHashConfigurationSource.HashExecutableConfigurationQuery<A, B>, AbstractHashConfigurationSource.HashConfigurationQueryResult<A, B>, A> {
	
	public AbstractHashConfigurationSource(PrimitiveDecoder<A> decoder) {
		super(decoder);
	}
	
	protected abstract Mono<List<ConfigurationProperty<ConfigurationKey, B>>> load();
	
	@Override
	public HashExecutableConfigurationQuery<A, B> get(String... names) throws IllegalArgumentException {
		return new HashExecutableConfigurationQuery<>(this).and().get(names);
	}
	
	public static class HashConfigurationQuery<A, B extends AbstractHashConfigurationSource<A, B>> implements ConfigurationQuery<HashConfigurationQuery<A, B>, HashExecutableConfigurationQuery<A, B>, HashConfigurationQueryResult<A, B>> {

		private HashExecutableConfigurationQuery<A, B> executableQuery;
		
		private List<String> names;
		
		private LinkedList<Parameter> parameters;
		
		private HashConfigurationQuery(HashExecutableConfigurationQuery<A, B> executableQuery) {
			this.executableQuery = executableQuery;
			this.names = new LinkedList<>();
			this.parameters = new LinkedList<>();
		}
		
		@Override
		public HashExecutableConfigurationQuery<A, B> get(String... names) {
			if(names == null || names.length == 0) {
				throw new IllegalArgumentException("You can't query an empty list of configuration properties");
			}
			this.names.addAll(Arrays.asList(names));
			return this.executableQuery;
		}
	}
	
	public static class HashExecutableConfigurationQuery<A, B extends AbstractHashConfigurationSource<A, B>> implements ExecutableConfigurationQuery<HashConfigurationQuery<A, B>, HashExecutableConfigurationQuery<A, B>, HashConfigurationQueryResult<A, B>> {
		
		private B source;
		
		private LinkedList<HashConfigurationQuery<A, B>> queries;
		
		@SuppressWarnings("unchecked")
		private HashExecutableConfigurationQuery(AbstractHashConfigurationSource<A, B> source) {
			this.source = (B)source;
			this.queries = new LinkedList<>();
		}

		@Override
		public HashConfigurationQuery<A, B> and() {
			this.queries.add(new HashConfigurationQuery<>(this));
			return this.queries.peekLast();
		}
		
		@Override
		public HashExecutableConfigurationQuery<A, B> withParameters(Parameter... parameters) throws IllegalArgumentException {
			if(parameters != null && parameters.length > 0) {
				HashConfigurationQuery<A, B> currentQuery = this.queries.peekLast();
				Set<String> parameterKeys = new HashSet<>();
				currentQuery.parameters.clear();
				List<String> duplicateParameters = new LinkedList<>();
				for(Parameter parameter : parameters) {
					currentQuery.parameters.add(parameter);
					if(!parameterKeys.add(parameter.getKey())) {
						duplicateParameters.add(parameter.getKey());
					}
				}
				if(duplicateParameters != null && duplicateParameters.size() > 0) {
					throw new IllegalArgumentException("The following parameters were specified more than once: " + duplicateParameters.stream().collect(Collectors.joining(", ")));
				}
			}
			return this;
		}

		@Override
		public Flux<HashConfigurationQueryResult<A, B>> execute() {
			return this.source.load()
				.map(properties -> properties.stream().collect(Collectors.toMap(property -> new GenericConfigurationKey(property.getKey().getName(), property.getKey().getParameters()), Function.identity())))
				.flatMapMany(indexedProperties -> Flux.fromStream(this.queries.stream()
					.flatMap(query -> query.names.stream().map(name -> new GenericConfigurationKey(name, query.parameters)))
					.map(key -> new HashConfigurationQueryResult<>(key, indexedProperties.get(key))))
				)
				.onErrorResume(ex -> true, ex -> Flux.fromStream(this.queries.stream()
					.flatMap(query -> query.names.stream().map(name -> new HashConfigurationQueryResult<>(new GenericConfigurationKey(name, query.parameters), this.source, ex))))
				);
		}
	}
	
	public static class HashConfigurationQueryResult<A, B extends AbstractHashConfigurationSource<A,B>> extends GenericConfigurationQueryResult<ConfigurationKey, ConfigurationProperty<ConfigurationKey, B>> {

		private HashConfigurationQueryResult(ConfigurationKey queryKey, ConfigurationProperty<ConfigurationKey, B> queryResult) {
			super(queryKey, queryResult);
		}
		
		public HashConfigurationQueryResult(ConfigurationKey queryKey, B source, Throwable error) {
			super(queryKey, source, error);
		}
	}
}