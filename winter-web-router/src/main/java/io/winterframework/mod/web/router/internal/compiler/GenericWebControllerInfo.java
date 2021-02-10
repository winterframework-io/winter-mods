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
package io.winterframework.mod.web.router.internal.compiler;

import java.util.List;
import java.util.Objects;

import javax.lang.model.type.DeclaredType;

import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ReporterInfo;
import io.winterframework.core.compiler.spi.support.AbstractInfo;
import io.winterframework.mod.web.router.internal.compiler.spi.WebControllerInfo;
import io.winterframework.mod.web.router.internal.compiler.spi.WebRouteInfo;

/**
 * @author jkuhn
 *
 */
public class GenericWebControllerInfo extends AbstractInfo<BeanQualifiedName> implements WebControllerInfo {

	private final DeclaredType type;
	
	private final String rootPath;
	
	private final List<? extends WebRouteInfo> routes;
	
	public GenericWebControllerInfo(BeanQualifiedName name, ReporterInfo reporter, DeclaredType type, String rootPath, List<? extends WebRouteInfo> routes) {
		super(name, reporter instanceof NoOpReporterInfo ? ((NoOpReporterInfo)reporter).getReporter() : reporter);
		this.type = Objects.requireNonNull(type);
		this.rootPath = rootPath;
		this.routes = routes != null ? routes : List.of();
	}
	
	@Override
	public boolean hasError() {
		return super.hasError() || this.routes.stream().anyMatch(route -> route.hasError());
	}
	
	@Override
	public boolean hasWarning() {
		return super.hasWarning() || this.routes.stream().anyMatch(route -> route.hasWarning());
	}

	@Override
	public DeclaredType getType() {
		return this.type;
	}

	@Override
	public String getRootPath() {
		return this.rootPath;
	}
	
	@Override
	public WebRouteInfo[] getRoutes() {
		return this.routes.stream().toArray(WebRouteInfo[]::new);
	}
}
