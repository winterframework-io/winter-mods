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
package io.winterframework.mod.boot.internal.resource;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Bean.Visibility;
import io.winterframework.mod.base.resource.AbstractResourceProvider;
import io.winterframework.mod.base.resource.AsyncResourceProvider;
import io.winterframework.mod.base.resource.FileResource;
import io.winterframework.mod.base.resource.MediaTypeService;
import io.winterframework.mod.base.resource.ResourceException;
import io.winterframework.mod.base.resource.ResourceProvider;
import io.winterframework.mod.base.resource.ResourceService;

/**
 * <p>
 * {@link ResourceProvider} implementation used to resolve resources on the
 * file system (ie. {@code file:/path/to/resource}).
 * </p>
 * 
 * <p>
 * This implementation supports path patterns and can then resolve multiple
 * resources matching a given URI pattern.
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * FileResourceProvider provider = new FileResourceProvider();
 * 
 * // Returns: /path/test1/a, /path/test1/a/b, /path/test2/c...
 * Stream{@literal<FileResource>} resources = provider.getResources(URI.create("file:/path/test?/{@literal **}/*");
 * </pre>
 * 
 * </blockquote>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 * @since 1.0
 * 
 * @see FileResource
 * @see AsyncResourceProvider
 * @see ResourceService
 * @see PathPatternResolver
 */
@Bean(visibility = Visibility.PRIVATE)
public class FileResourceProvider extends AbstractResourceProvider<FileResource> implements AsyncResourceProvider<FileResource> {

	@Override
	public void setMediaTypeService(MediaTypeService mediaTypeService) {
		super.setMediaTypeService(mediaTypeService);
	}
	
	@Override
	public FileResource getResource(URI uri) throws NullPointerException, IllegalArgumentException, ResourceException {
		return new FileResource(uri, this.mediaTypeService);
	}
	
	@Override
	public Stream<FileResource> getResources(URI uri) throws NullPointerException, IllegalArgumentException, ResourceException {
		Path pathPattern = Paths.get(FileResource.checkUri(uri));
		return PathPatternResolver.resolve(pathPattern, path -> new FileResource(path.toUri(), this.mediaTypeService));
	}
	
	@Override
	public Set<String> getSupportedSchemes() {
		return Set.of(FileResource.SCHEME_FILE);
	}
}
