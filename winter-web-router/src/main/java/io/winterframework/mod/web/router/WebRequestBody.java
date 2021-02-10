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
package io.winterframework.mod.web.router;

import java.lang.reflect.Type;

import io.winterframework.mod.web.server.RequestBody;

/**
 * @author jkuhn
 *
 */
public interface WebRequestBody extends RequestBody {

	<A> RequestDataDecoder<A> decoder(Class<A> type);
	
	<A> RequestDataDecoder<A> decoder(Type type);
	
	@Override
	Multipart<? extends WebPart> multipart() throws IllegalStateException;
	
	public static interface WebMultipart extends Multipart<WebPart> {
	}
}
