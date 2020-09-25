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
package io.winterframework.mod.configuration.internal.compiler;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.NestedBean;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;
import io.winterframework.core.compiler.spi.ReporterInfo;
import io.winterframework.core.compiler.spi.plugin.CompilerPlugin;
import io.winterframework.core.compiler.spi.plugin.PluginContext;
import io.winterframework.core.compiler.spi.plugin.PluginExecution;
import io.winterframework.core.compiler.spi.plugin.PluginExecutionException;
import io.winterframework.mod.configuration.Configuration;
import io.winterframework.mod.configuration.internal.compiler.ConfigurationLoaderClassGenerationContext.GenerationMode;
import io.winterframework.mod.configuration.internal.compiler.spi.ConfigurationInfo;
import io.winterframework.mod.configuration.internal.compiler.spi.ConfigurationPropertyInfo;
import io.winterframework.mod.configuration.internal.compiler.spi.PropertyQualifiedName;

/**
 * @author jkuhn
 *
 */
public class ConfigurationCompilerPlugin implements CompilerPlugin {

	private PluginContext pluginContext;
	
	private TypeMirror configurationAnnotationType;
	private TypeMirror nestedBeanAnnotationType;
	
	private ConfigurationLoaderClassGenerator configurationLoaderClassGenerator;
	
	public ConfigurationCompilerPlugin() {
		this.configurationLoaderClassGenerator = new ConfigurationLoaderClassGenerator();
	}

	@Override
	public String getSupportedAnnotationType() {
		return Configuration.class.getCanonicalName();
	}

	@Override
	public void init(PluginContext pluginContext) {
		this.pluginContext = pluginContext;
		this.configurationAnnotationType = this.pluginContext.getElementUtils().getTypeElement(Configuration.class.getCanonicalName()).asType();
		this.nestedBeanAnnotationType = this.pluginContext.getElementUtils().getTypeElement(NestedBean.class.getCanonicalName()).asType();
	}

	@Override
	public void execute(PluginExecution execution) throws PluginExecutionException {
		for(TypeElement element : execution.getElements()) {
			if(!TypeElement.class.isAssignableFrom(element.getClass())) {
				throw new PluginExecutionException("The specified element must be a TypeElement");
			}
			
			TypeElement typeElement = (TypeElement)element;
			DeclaredType configurationType = (DeclaredType)typeElement.asType();
			
			AnnotationMirror configurationAnnotation = null;
			for(AnnotationMirror annotation : typeElement.getAnnotationMirrors()) {
				if(this.pluginContext.getTypeUtils().isSameType(annotation.getAnnotationType(), this.configurationAnnotationType)) {
					configurationAnnotation = annotation;
				}
			}
			if(configurationAnnotation == null) {
				throw new PluginExecutionException("The specified element is not annotated with " + Configuration.class.getSimpleName());
			}
			
			if(!this.pluginContext.getElementUtils().getModuleOf(typeElement).getQualifiedName().toString().equals(execution.getModule().toString())) {
				throw new PluginExecutionException("The specified element doesn't belong to module " + execution.getModule());
			}
			
			ReporterInfo beanReporter = execution.getReporter(typeElement, configurationAnnotation);		
			
			if(!typeElement.getKind().equals(ElementKind.INTERFACE)) {
				// This should never happen, we shouldn't get there if it wasn't an interface
				beanReporter.error("A configuration must be an interface");
				continue;
			}
			
			String name = null;
			boolean generateBean = false;
			boolean overridable = false;
			for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.pluginContext.getElementUtils().getElementValuesWithDefaults(configurationAnnotation).entrySet()) {
				switch(value.getKey().getSimpleName().toString()) {
					case "name" : name = (String)value.getValue().getValue();
						break;
					case "generateBean" : generateBean = Boolean.valueOf(value.getValue().getValue().toString());
						break;
					case "overridable" : overridable = Boolean.valueOf(value.getValue().getValue().toString());
						break;
				}
			}
			
			// Bean qualified name
			if(name == null || name.equals("")) {
				name = typeElement.getSimpleName().toString();
				name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
			}
			
			BeanQualifiedName configurationQName;
			try {
				configurationQName = new BeanQualifiedName(execution.getModule(), name);
			} 
			catch (QualifiedNameFormatException e) {
				beanReporter.error("Invalid bean qualified name: " + e.getMessage());
				continue;
			}

			List<? extends ConfigurationPropertyInfo> configurationProperties = this.pluginContext.getElementUtils().getAllMembers(typeElement).stream()
				.filter(el -> el.getKind().equals(ElementKind.METHOD) && !this.pluginContext.getTypeUtils().isSameType(this.pluginContext.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType(), el.getEnclosingElement().asType()))
				.map(el -> {
					ExecutableElement propertyMethod = (ExecutableElement)el;
					
					PropertyQualifiedName propertyQName = new PropertyQualifiedName(configurationQName, propertyMethod.getSimpleName().toString());
					ReporterInfo propertyReporter = execution.getReporter(propertyMethod);
					boolean invalid = false;
					if(propertyMethod.getParameters().size() > 0) {
						execution.getReporter(propertyMethod.getParameters().get(0)).error("Configuration property must be declared as a no-argument method");
						invalid = true;
					}
					if(propertyMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
						propertyReporter.error("Configuration property must be declared as a non-void method");
						invalid = true;
					}
					if(invalid) {
						return null;
					}
					
					if(this.isNestedConfiguration(propertyMethod, execution.getModule())) {
						return new GenericNestedConfigurationProperty(propertyQName, propertyReporter, propertyMethod, this.extractNestedConfigurationInfo(propertyReporter, propertyQName, (TypeElement)this.pluginContext.getTypeUtils().asElement(propertyMethod.getReturnType())));
					}
					else {
						return new GenericConfigurationPropertyInfo(propertyQName, propertyReporter, propertyMethod);
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
			
			ConfigurationInfo configurationInfo = new GenericConfigurationInfo(configurationQName, beanReporter, configurationType, configurationProperties, generateBean, overridable);
			
			try {
				execution.createSourceFile(configurationInfo.getType().toString() + "Loader", typeElement, () -> {
					return configurationInfo.accept(this.configurationLoaderClassGenerator, new ConfigurationLoaderClassGenerationContext(this.pluginContext.getTypeUtils(), this.pluginContext.getElementUtils(), GenerationMode.CONFIGURATION_LOADER_CLASS)).toString();
				});
			} 
			catch (IOException e) {
				throw new PluginExecutionException("Unable to generate configuration bean class " + configurationInfo.getType().toString() + "Bean", e);
			}
		}
	}
	
	private ConfigurationInfo extractNestedConfigurationInfo(ReporterInfo rootPropertyReporter, PropertyQualifiedName nestedPropertyName, TypeElement nestedTypeElement) {
		List<? extends ConfigurationPropertyInfo> configurationProperties = this.pluginContext.getElementUtils().getAllMembers(nestedTypeElement).stream()
			.filter(el -> el.getKind().equals(ElementKind.METHOD) && !this.pluginContext.getTypeUtils().isSameType(this.pluginContext.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType(), el.getEnclosingElement().asType()))
			.map(el -> {
				ExecutableElement propertyMethod = (ExecutableElement)el;
				PropertyQualifiedName propertyQName = new PropertyQualifiedName(nestedPropertyName, propertyMethod.getSimpleName().toString());
				
				boolean invalid = false;
				if(propertyMethod.getParameters().size() > 0) {
					rootPropertyReporter.warning("Ignoring invalid nested property " + propertyQName.getBeanName() + " which should be defined as a no-argument method");
					invalid = true;
				}
				if(propertyMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
					rootPropertyReporter.warning("Ignoring invalid nested property " + propertyQName.getBeanName() + " which should be defined as a non-void method");
					invalid = true;
				}
				if(invalid) {
					return null;
				}
				
				if(this.isNestedConfiguration(propertyMethod, nestedPropertyName.getModuleQName())) {
					return new GenericNestedConfigurationProperty(propertyQName, rootPropertyReporter, propertyMethod, this.extractNestedConfigurationInfo(rootPropertyReporter, propertyQName, (TypeElement)this.pluginContext.getTypeUtils().asElement(propertyMethod.getReturnType())));
				}
				else {
					return new GenericConfigurationPropertyInfo(propertyQName, rootPropertyReporter, propertyMethod);
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		
		return new GenericConfigurationInfo(nestedPropertyName, rootPropertyReporter, (DeclaredType)nestedTypeElement.asType(), configurationProperties);
	}
	
	private boolean isNestedConfiguration(ExecutableElement propertyMethod, ModuleQualifiedName module) {
		if(propertyMethod.getAnnotationMirrors().stream().anyMatch(annotation -> this.pluginContext.getTypeUtils().isSameType(annotation.getAnnotationType(), this.nestedBeanAnnotationType))) {
			TypeMirror type = propertyMethod.getReturnType();
			if(type.getKind().equals(TypeKind.DECLARED)) {
				TypeElement typeElement = (TypeElement)this.pluginContext.getTypeUtils().asElement(type);
				return !this.pluginContext.getElementUtils().getModuleOf(typeElement).toString().equals(module.toString())
						&& typeElement.getAnnotationMirrors().stream().anyMatch(annotation -> this.pluginContext.getTypeUtils().isSameType(annotation.getAnnotationType(), this.configurationAnnotationType));
			}
			else {
				// primitive, array...
				return false;
			}
		}
		return false;
	}
}
