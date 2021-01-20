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
package io.winterframework.mod.boot.internal.converter;

import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Provide;
import io.winterframework.mod.base.converter.ByteBufConverter;
import io.winterframework.mod.base.converter.MediaTypeConverter;
import io.winterframework.mod.base.converter.ObjectConverter;
import io.winterframework.mod.base.resource.MediaTypes;

/**
 * @author jkuhn
 *
 */
@Bean( name = "textPlainMediaTypeConverter" )
public class TextMediaTypeConverter extends ByteBufConverter implements @Provide MediaTypeConverter<ByteBuf> {

	public TextMediaTypeConverter(ObjectConverter<String> parameterConverter) {
		super(parameterConverter);
	}
	
	@Override
	public Set<String> getSupportedMediaTypes() {
		return Set.of(MediaTypes.TEXT_PLAIN);
	}
}
