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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.spi.ReporterInfo;
import io.winterframework.core.compiler.spi.plugin.PluginContext;
import io.winterframework.core.compiler.spi.plugin.PluginExecution;
import io.winterframework.mod.web.router.annotation.Body;
import io.winterframework.mod.web.router.annotation.CookieParam;
import io.winterframework.mod.web.router.annotation.FormParam;
import io.winterframework.mod.web.router.annotation.HeaderParam;
import io.winterframework.mod.web.router.annotation.PathParam;
import io.winterframework.mod.web.router.annotation.QueryParam;
import io.winterframework.mod.web.router.annotation.SseEventFactory;
import io.winterframework.mod.web.router.internal.compiler.spi.WebParameterQualifiedName;
import io.winterframework.mod.web.router.internal.compiler.spi.WebRequestBodyParameterInfo.RequestBodyKind;
import io.winterframework.mod.web.router.internal.compiler.spi.WebRequestBodyParameterInfo.RequestBodyReactiveKind;
import io.winterframework.mod.web.router.internal.compiler.spi.WebRouteQualifiedName;
import io.winterframework.mod.web.router.internal.compiler.spi.WebSseEventFactoryParameterInfo.SseEventFactoryKind;

/**
 * @author jkuhn
 *
 */
public class WebParameterInfoFactory {

	private final PluginContext pluginContext;
	private final PluginExecution pluginExecution;
	private final Map<VariableElement, AbstractWebParameterInfo> processedParameterElements;
	
	/* Web annotations */
	private final TypeMirror bodyAnnotationType;
	private final TypeMirror cookieParameterAnnotationType;
	private final TypeMirror formParameterAnnotationType;
	private final TypeMirror headerParameterAnnotationType;
	private final TypeMirror pathParameterAnnotationType;
	private final TypeMirror queryParameterAnnotationType;
	private final TypeMirror sseEventFactoryAnnotationType;
	
	/* Contextual */
	private final TypeMirror webExchangeType;
	private final TypeMirror rawSseEventFactoryType;
	private final TypeMirror sseEncoderEventFactoryType;
	
	/* Types */
	private final TypeMirror optionalType;
	private final TypeMirror monoType;
	private final TypeMirror fluxType;
	private final TypeMirror publisherType;
	private final TypeMirror byteBufType;
	private final TypeMirror webPartType;
	private final TypeMirror parameterType;
	
	public WebParameterInfoFactory(PluginContext pluginContext, PluginExecution pluginExecution) {
		this.pluginContext = Objects.requireNonNull(pluginContext);
		this.pluginExecution = Objects.requireNonNull(pluginExecution);
		this.processedParameterElements = new HashMap<>();
		
		this.bodyAnnotationType = this.pluginContext.getElementUtils().getTypeElement(Body.class.getCanonicalName()).asType();
		this.cookieParameterAnnotationType = this.pluginContext.getElementUtils().getTypeElement(CookieParam.class.getCanonicalName()).asType();
		this.formParameterAnnotationType = this.pluginContext.getElementUtils().getTypeElement(FormParam.class.getCanonicalName()).asType();
		this.headerParameterAnnotationType = this.pluginContext.getElementUtils().getTypeElement(HeaderParam.class.getCanonicalName()).asType();
		this.pathParameterAnnotationType = this.pluginContext.getElementUtils().getTypeElement(PathParam.class.getCanonicalName()).asType();
		this.queryParameterAnnotationType = this.pluginContext.getElementUtils().getTypeElement(QueryParam.class.getCanonicalName()).asType();
		this.sseEventFactoryAnnotationType = this.pluginContext.getElementUtils().getTypeElement(SseEventFactory.class.getCanonicalName()).asType();
		
		this.optionalType = this.pluginContext.getTypeUtils().erasure(this.pluginContext.getElementUtils().getTypeElement(Optional.class.getCanonicalName()).asType());
		this.monoType = this.pluginContext.getTypeUtils().erasure(this.pluginContext.getElementUtils().getTypeElement("reactor.core.publisher.Mono").asType());
		this.fluxType = this.pluginContext.getTypeUtils().erasure(this.pluginContext.getElementUtils().getTypeElement("reactor.core.publisher.Flux").asType());
		this.publisherType = this.pluginContext.getTypeUtils().erasure(this.pluginContext.getElementUtils().getTypeElement("org.reactivestreams.Publisher").asType());
		this.byteBufType = this.pluginContext.getElementUtils().getTypeElement("io.netty.buffer.ByteBuf").asType();
		this.webPartType = this.pluginContext.getElementUtils().getTypeElement("io.winterframework.mod.web.router.WebPart").asType();
		this.parameterType = this.pluginContext.getElementUtils().getTypeElement("io.winterframework.mod.web.Parameter").asType();
		
		this.webExchangeType = this.pluginContext.getElementUtils().getTypeElement("io.winterframework.mod.web.router.WebExchange").asType();
		TypeMirror rawSseEventType = this.pluginContext.getTypeUtils().getDeclaredType(this.pluginContext.getElementUtils().getTypeElement("io.winterframework.mod.web.server.ResponseBody.Sse.Event"), this.byteBufType);
		this.rawSseEventFactoryType = this.pluginContext.getTypeUtils().getDeclaredType(this.pluginContext.getElementUtils().getTypeElement("io.winterframework.mod.web.server.ResponseBody.Sse.EventFactory"), this.byteBufType, rawSseEventType);
		this.sseEncoderEventFactoryType = this.pluginContext.getTypeUtils().erasure(this.pluginContext.getElementUtils().getTypeElement("io.winterframework.mod.web.router.WebResponseBody.SseEncoder.EventFactory").asType());
	}
	
	public AbstractWebParameterInfo createParameter(WebRouteQualifiedName routeQName, VariableElement parameterElement, VariableElement annotatedParameterElement, TypeMirror parameterType) {
		AbstractWebParameterInfo result = null;
		
		WebParameterQualifiedName parameterQName = new WebParameterQualifiedName(routeQName, parameterElement.getSimpleName().toString());
		boolean required = !this.pluginContext.getTypeUtils().isSameType(this.pluginContext.getTypeUtils().erasure(parameterType), this.optionalType);
		if(!required) {
			// For optional parameter consider the Optional<> argument
			parameterType = ((DeclaredType)parameterType).getTypeArguments().get(0);
		}

		ReporterInfo parameterReporter = this.pluginExecution.getReporter(parameterElement);
		if(this.processedParameterElements.containsKey(parameterElement)) {
			parameterReporter = new NoOpReporterInfo(this.processedParameterElements.get(parameterElement));
		}
		else {
			parameterReporter = this.pluginExecution.getReporter(parameterElement);
		}
		
		// A web parameter can't be annotated with multiple web parameter annotations
		for(AnnotationMirror annotation : annotatedParameterElement.getAnnotationMirrors()) {
			AbstractWebParameterInfo currentParameterInfo = null;
			if(this.pluginContext.getTypeUtils().isSameType(annotation.getAnnotationType(), this.bodyAnnotationType)) {
				if(!required) {
					parameterReporter.error("Request body parameter can't be optional");
				}
				else {
					currentParameterInfo = this.createRequestBodyParameter(parameterReporter, parameterQName, parameterElement, parameterType);
				}
			}
			else if(this.pluginContext.getTypeUtils().isSameType(annotation.getAnnotationType(), this.cookieParameterAnnotationType)) {
				currentParameterInfo = this.createCookieParameter(parameterReporter, parameterQName, parameterElement, parameterType, required);
			}
			else if(this.pluginContext.getTypeUtils().isSameType(annotation.getAnnotationType(), this.formParameterAnnotationType)) {
				currentParameterInfo = this.createFormParameter(parameterReporter, parameterQName, parameterElement, parameterType, required);
			}
			else if(this.pluginContext.getTypeUtils().isSameType(annotation.getAnnotationType(), this.headerParameterAnnotationType)) {
				currentParameterInfo = this.createHeaderParameter(parameterReporter, parameterQName, parameterElement, parameterType, required);
			}
			else if(this.pluginContext.getTypeUtils().isSameType(annotation.getAnnotationType(), this.pathParameterAnnotationType)) {
				currentParameterInfo = this.createPathParameter(parameterReporter, parameterQName, parameterElement, parameterType, required);
			}
			else if(this.pluginContext.getTypeUtils().isSameType(annotation.getAnnotationType(), this.queryParameterAnnotationType)) {
				currentParameterInfo = this.createQueryParameter(parameterReporter, parameterQName, parameterElement, parameterType, required);
			}
			else if(this.pluginContext.getTypeUtils().isSameType(annotation.getAnnotationType(), this.sseEventFactoryAnnotationType)) {
				if(!required) {
					parameterReporter.error("SSE event factory parameter can't be optional");
				}
				else {
					currentParameterInfo = this.createSseEventFactoryParameter(parameterReporter, parameterQName, parameterElement);
				}
			}
			
			if(currentParameterInfo != null) {
				if(result != null) {
					parameterReporter.error("Too many web parameter annotations specified, only one is allowed");
					break;
				}
				result = currentParameterInfo;
			}
		}
		
		// Contextual
		if(result == null) {
			if(this.pluginContext.getTypeUtils().isAssignable(this.webExchangeType, parameterElement.asType())) {
				result = this.createExchangeParameter(parameterReporter, parameterQName, parameterElement);
			}
		}
		
		if(result == null) {
			if(!parameterReporter.hasError()) {
				parameterReporter.error("Invalid parameter which is neither a web parameter, nor a valid contextual parameter");
			}
			result = new ErrorWebParameterInfo(parameterQName, parameterReporter, parameterElement, required);
		}
		this.processedParameterElements.putIfAbsent(parameterElement, result);
		return result;
	}
	
	private GenericWebCookieParameterInfo createCookieParameter(ReporterInfo reporter, WebParameterQualifiedName parameterQName, VariableElement parameterElement, TypeMirror parameterType, boolean required) {
		return new GenericWebCookieParameterInfo(parameterQName, reporter, parameterElement, parameterType, required);
	}
	
	private GenericWebExchangeParameterInfo createExchangeParameter(ReporterInfo reporter, WebParameterQualifiedName parameterQName, VariableElement parameterElement) {
		return new GenericWebExchangeParameterInfo(parameterQName, reporter, parameterElement);
	}
	
	private GenericSseEventFactoryParameterInfo createSseEventFactoryParameter(ReporterInfo reporter, WebParameterQualifiedName parameterQName, VariableElement parameterElement) {
		if(this.pluginContext.getTypeUtils().isSameType(this.sseEncoderEventFactoryType, this.pluginContext.getTypeUtils().erasure(parameterElement.asType()))) {
			return new GenericSseEventFactoryParameterInfo(parameterQName, reporter, parameterElement, ((DeclaredType)parameterElement.asType()).getTypeArguments().get(0), SseEventFactoryKind.ENCODER, parameterElement.getAnnotation(SseEventFactory.class).value());
		}
		else if(this.pluginContext.getTypeUtils().isSameType(this.rawSseEventFactoryType, parameterElement.asType())) {
			return new GenericSseEventFactoryParameterInfo(parameterQName, reporter, parameterElement, this.byteBufType, SseEventFactoryKind.RAW, parameterElement.getAnnotation(SseEventFactory.class).value());
		}
		else {
			reporter.error("Invalid SSE event factory parameter which must be of type " + this.rawSseEventFactoryType + " or " + this.sseEncoderEventFactoryType);
			return new GenericSseEventFactoryParameterInfo(parameterQName, reporter, parameterElement, this.byteBufType, SseEventFactoryKind.RAW);
		}
	}
	
	private GenericWebFormParameterInfo createFormParameter(ReporterInfo reporter, WebParameterQualifiedName parameterQName, VariableElement parameterElement, TypeMirror parameterType, boolean required) {
		return new GenericWebFormParameterInfo(parameterQName, reporter, parameterElement, parameterType, required);
	}
	
	private GenericWebHeaderParameterInfo createHeaderParameter(ReporterInfo reporter, WebParameterQualifiedName parameterQName, VariableElement parameterElement, TypeMirror parameterType, boolean required) {
		return new GenericWebHeaderParameterInfo(parameterQName, reporter, parameterElement, parameterType, required);
	}
	
	private GenericWebPathParameterInfo createPathParameter(ReporterInfo reporter, WebParameterQualifiedName parameterQName, VariableElement parameterElement, TypeMirror parameterType, boolean required) {
		return new GenericWebPathParameterInfo(parameterQName, reporter, parameterElement, parameterType, required);
	}
	
	private GenericWebQueryParameterInfo createQueryParameter(ReporterInfo reporter, WebParameterQualifiedName parameterQName, VariableElement parameterElement, TypeMirror parameterType, boolean required) {
		return new GenericWebQueryParameterInfo(parameterQName, reporter, parameterElement, parameterType, required);
	}
	
	private GenericWebRequestBodyParameterInfo createRequestBodyParameter(ReporterInfo reporter, WebParameterQualifiedName parameterQName, VariableElement parameterElement, TypeMirror parameterType) {
		TypeMirror requestBodyType = parameterType;
		TypeMirror erasedRequestBodyType = this.pluginContext.getTypeUtils().erasure(requestBodyType);
		
		RequestBodyReactiveKind requestBodyReactiveKind = null;
		if(this.pluginContext.getTypeUtils().isSameType(erasedRequestBodyType, this.publisherType)) {
			requestBodyReactiveKind = RequestBodyReactiveKind.PUBLISHER;
		}
		else if(this.pluginContext.getTypeUtils().isSameType(erasedRequestBodyType, this.monoType)) {
			requestBodyReactiveKind = RequestBodyReactiveKind.ONE;
		}
		else if(this.pluginContext.getTypeUtils().isSameType(erasedRequestBodyType, this.fluxType)) {
			requestBodyReactiveKind = RequestBodyReactiveKind.MANY;
		}
		else {
			requestBodyReactiveKind = RequestBodyReactiveKind.NONE;
		}
		
		RequestBodyKind requestBodyKind = RequestBodyKind.ENCODED;
		if(requestBodyReactiveKind != RequestBodyReactiveKind.NONE) {
			requestBodyType = ((DeclaredType)requestBodyType).getTypeArguments().get(0);
			if(this.pluginContext.getTypeUtils().isSameType(requestBodyType, this.byteBufType)) {
				requestBodyKind = RequestBodyKind.RAW;
			}
			else if(this.pluginContext.getTypeUtils().isAssignable(this.webPartType, requestBodyType)) {
				requestBodyKind = RequestBodyKind.MULTIPART;
			}
			else if(this.pluginContext.getTypeUtils().isSameType(requestBodyType, this.parameterType)) {
				requestBodyKind = RequestBodyKind.URLENCODED;
			}
		}
		else if(this.pluginContext.getTypeUtils().isSameType(requestBodyType, this.byteBufType)) {
			requestBodyKind = RequestBodyKind.RAW;
		}
		return new GenericWebRequestBodyParameterInfo(parameterQName, reporter, parameterElement, requestBodyReactiveKind, requestBodyKind, requestBodyType);
	}
}
