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
package io.winterframework.mod.web.lab.router.base;

/**
 * @author jkuhn
 *
 */
public class RoutingException extends RuntimeException {

	private static final long serialVersionUID = 8353817386265691341L;

	private int status;
	
	public RoutingException(int status) {
		this.status = status;
	}

	public RoutingException(int status, String message) {
		super(message);
		this.status = status;
	}

	public RoutingException(int status, Throwable cause) {
		super(cause);
		this.status = status;
	}

	public RoutingException(int status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public RoutingException(int status, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}
}
