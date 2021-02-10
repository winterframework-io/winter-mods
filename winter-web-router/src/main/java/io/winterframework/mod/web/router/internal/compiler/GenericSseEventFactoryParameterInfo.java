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
import java.util.Optional;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.spi.ReporterInfo;
import io.winterframework.mod.web.router.internal.compiler.spi.WebParameterQualifiedName;
import io.winterframework.mod.web.router.internal.compiler.spi.WebSseEventFactoryParameterInfo;

/**
 * @author jkuhn
 *
 */
public class GenericSseEventFactoryParameterInfo extends AbstractWebParameterInfo implements WebSseEventFactoryParameterInfo {

	private final SseEventFactoryKind eventFactoryKind;
	
	private final Optional<String> dataMediaType;
	
	public GenericSseEventFactoryParameterInfo(WebParameterQualifiedName name, ReporterInfo reporter, VariableElement parameterElement, TypeMirror eventType, SseEventFactoryKind eventFactoryKind) {
		this(name, reporter, parameterElement, eventType, eventFactoryKind, null);
	}
	
	public GenericSseEventFactoryParameterInfo(WebParameterQualifiedName name, ReporterInfo reporter, VariableElement parameterElement, TypeMirror eventType, SseEventFactoryKind eventFactoryKind, String dataMediaType) {
		super(name, reporter, parameterElement, eventType, true);
		this.eventFactoryKind = Objects.requireNonNull(eventFactoryKind);
		this.dataMediaType = Optional.ofNullable(dataMediaType);
	}

	@Override
	public SseEventFactoryKind getEventFactoryKind() {
		return this.eventFactoryKind;
	}
	
	@Override
	public Optional<String> getDataMediaType() {
		return this.dataMediaType;
	}
}
