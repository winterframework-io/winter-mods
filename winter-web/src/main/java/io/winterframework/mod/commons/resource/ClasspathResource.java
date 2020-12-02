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
package io.winterframework.mod.commons.resource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;

/**
 * @author jkuhn
 *
 */
public class ClasspathResource extends AbstractAsyncResource {

	public static final String SCHEME_CLASSPATH = "classpath";
	
	private Optional<Resource> resource;
	
	private URI uri;
	
	private Class<?> clazz;
	private ClassLoader classLoader;
	
	public ClasspathResource(URI uri) throws IllegalArgumentException, IOException {
		this(uri, (MediaTypeService)null);
	}
	
	public ClasspathResource(URI uri, Class<?> clazz) throws IllegalArgumentException, IOException {
		this(uri, clazz, null);
	}

	public ClasspathResource(URI uri, ClassLoader classLoader) throws IllegalArgumentException, IOException {
		this(uri, classLoader, null);
	}
	
	protected ClasspathResource(URI uri, MediaTypeService mediaTypeService) throws IllegalArgumentException, IOException {
		this(uri, Thread.currentThread().getContextClassLoader(), mediaTypeService);
	}
	
	protected ClasspathResource(URI uri, Class<?> clazz, MediaTypeService mediaTypeService) throws IllegalArgumentException, IOException {
		super(mediaTypeService);
		this.uri = this.checkUri(uri);
		this.clazz = Objects.requireNonNull(clazz);
	}
	
	protected ClasspathResource(URI uri, ClassLoader classLoader, MediaTypeService mediaTypeService) throws IllegalArgumentException, IOException {
		super(mediaTypeService);
		this.uri = this.checkUri(uri);
		if(!this.uri.getScheme().equals(SCHEME_CLASSPATH)) {
			throw new IllegalArgumentException("Not a " + SCHEME_CLASSPATH + " uri");
		}
		
		if(classLoader == null) {
			try {
				this.classLoader = Thread.currentThread().getContextClassLoader();
			}
			catch (Throwable ex) {
				this.classLoader = ClasspathResource.class.getClassLoader();
				if (this.classLoader == null) {
					this.classLoader = ClassLoader.getSystemClassLoader();
				}
			}
		}
		else {
			this.classLoader = classLoader;
		}
	}
	
	@Override
	public void setExecutor(ExecutorService executor) {
		super.setExecutor(executor);
		if(this.resource != null) {
			this.resource.ifPresent(resource -> {
				if(resource instanceof AsyncResource) {
					((AsyncResource) resource).setExecutor(this.getExecutor());
				}
			});
		}
	}
	
	private Optional<Resource> resolveResource() throws IllegalArgumentException, IOException {
		if(this.resource == null) {
			URL url;
			if(this.clazz != null) {
				url = this.clazz.getResource(this.uri.getPath());
			}
			else {
				String path = this.uri.getPath();
				if(path.startsWith("/")) {
					path = path.substring(1);
				}
				url = this.classLoader.getResource(path);
			}
			if(url != null) {
				URI uri;
				try {
					uri = url.toURI();
				} 
				catch (URISyntaxException e) {
					throw new ResourceException("Error resolving classpath resource: " + this.uri, e);
				}
				String scheme = uri.getScheme();
				Resource resolvedResource;
				if(scheme.equals(FileResource.SCHEME_FILE)) {
					resolvedResource = new FileResource(uri, this.getMediaTypeService());
				}
				else if(scheme.equals(JarResource.SCHEME_JAR)) {
					resolvedResource = new JarResource(uri, this.getMediaTypeService());
				}
				else if(scheme.equals(ZipResource.SCHEME_ZIP)) {
					resolvedResource = new ZipResource(uri, this.getMediaTypeService());
				}
				else {
					throw new ResourceException("Unsupported resource scheme: " + scheme);
				}
				if(resolvedResource instanceof AsyncResource) {
					((AsyncResource) resolvedResource).setExecutor(this.getExecutor());
				}
				this.resource = Optional.of(resolvedResource);
			}
			else {
				this.resource = Optional.empty();
			}
		}
		return this.resource;
	}
	
	private URI checkUri(URI uri) {
		if(!Objects.requireNonNull(uri).getScheme().equals(SCHEME_CLASSPATH)) {
			throw new IllegalArgumentException("Not a " + SCHEME_CLASSPATH + " uri");
		}
		return uri.normalize();
	}
	
	@Override
	public String getFilename() throws IOException {
		if(this.resolveResource().isPresent()) {
			return this.resolveResource().get().getFilename();
		}
		return null;
	}

	@Override
	public String getMediaType() throws IOException {
		if(this.resolveResource().isPresent()) {
			return this.resolveResource().get().getMediaType();
		}
		return null;
	}
	
	@Override
	public URI getURI() {
		return this.uri;
	}
	
	@Override
	public Boolean exists() throws IOException {
		if(this.resolveResource().isPresent()) {
			return this.resolveResource().get().exists();
		}
		return false;
	}
	
	@Override
	public Long size() throws IOException {
		if(this.resolveResource().isPresent()) {
			return this.resolveResource().get().size();
		}
		return null;
	}
	
	@Override
	public FileTime lastModified() throws IOException {
		if(this.resolveResource().isPresent()) {
			return this.resolveResource().get().lastModified();
		}
		return null;
	}
	
	@Override
	public Optional<ReadableByteChannel> openReadableByteChannel() throws IOException {
		if(this.resolveResource().isPresent()) {
			return this.resolveResource().get().openReadableByteChannel();
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<WritableByteChannel> openWritableByteChannel(boolean append, boolean createParents) throws IOException {
		return Optional.empty();
	}
	
	@Override
	public Optional<Flux<ByteBuf>> read() throws IOException {
		if(this.resolveResource().isPresent()) {
			return this.resolveResource().get().read();
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<Flux<Integer>> write(Flux<ByteBuf> data, boolean append, boolean createParents) {
		return Optional.empty();
	}
	
	@Override
	public boolean delete() throws IOException {
		throw new UnsupportedOperationException("Can't delete a classpath resource");
	}

	@Override
	public void close() throws IOException {
		if(this.resolveResource().isPresent()) {
			this.resolveResource().get().close();
		}
	}
	
	@Override
	public Resource resolve(URI uri) throws IllegalArgumentException, IOException {
		URI resolvedUri = this.uri.resolve(uri.normalize());
		ClasspathResource resolvedResource;
		if(this.clazz != null) {
			resolvedResource = new ClasspathResource(resolvedUri, this.clazz, this.getMediaTypeService());
		}
		else {
			resolvedResource = new ClasspathResource(resolvedUri, this.classLoader, this.getMediaTypeService());
		}
		resolvedResource.setExecutor(this.getExecutor());
		return resolvedResource;
	}
}