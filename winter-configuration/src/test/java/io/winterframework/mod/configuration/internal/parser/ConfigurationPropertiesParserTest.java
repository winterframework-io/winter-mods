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
package io.winterframework.mod.configuration.internal.parser;

import org.junit.jupiter.api.Test;

/**
 * @author jkuhn
 *
 */
public class ConfigurationPropertiesParserTest {

	@Test
	public void testConfigurationPropertiesParser() {
		/*String block = "plip {\n" + 
				"	plap {\n" + 
				"		json = \"\"\"\n" + 
				"			{\n" + 
				"				\"title\":\"Some json\",\n" + 
				"				table = [\"abc,\"bcd\"]\n" + 
				"			}\n" + 
				"		\"\"\"\n" + 
				"	}\n" + 
				"}\n" +
				"prop=\"abc\"";
		
		ConfigurationPropertiesParser<?> parser = new ConfigurationPropertiesParser<>(new StringReader(block));
		
		try {
			List<?> r = parser.StartConfigurationProperties();
			r.stream().forEach(o -> System.out.println(((ConfigurationEntry<ConfigurationKey,?>)o).getKey()+ " = [" + ((ConfigurationEntry<ConfigurationKey,?>)o).valueAsString().get()+ "]"));
			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}