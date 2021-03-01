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

import java.util.Objects;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.spi.ReporterInfo;
import io.winterframework.mod.web.router.internal.compiler.spi.WebParameterQualifiedName;
import io.winterframework.mod.web.router.internal.compiler.spi.WebRequestBodyParameterInfo;

/**
 * @author jkuhn
 *
 */
class GenericWebRequestBodyParameterInfo extends AbstractWebParameterInfo implements WebRequestBodyParameterInfo {

	private final RequestBodyKind requestBodyKind;
	
	private final RequestBodyReactiveKind requestBodyReactiveKind;
	
	public GenericWebRequestBodyParameterInfo(WebParameterQualifiedName name, ReporterInfo reporter, VariableElement element, RequestBodyReactiveKind requestBodyReactiveKind, RequestBodyKind requestBodyKind, TypeMirror requestBodyType) {
		super(name, reporter, element, requestBodyType, false);
		this.requestBodyKind = Objects.requireNonNull(requestBodyKind);
		this.requestBodyReactiveKind = Objects.requireNonNull(requestBodyReactiveKind);
	}

	@Override
	public RequestBodyReactiveKind getBodyReactiveKind() {
		return this.requestBodyReactiveKind;
	}
	
	@Override
	public RequestBodyKind getBodyKind() {
		return this.requestBodyKind;
	}
}
