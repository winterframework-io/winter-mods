package io.winterframework.mod.configuration.source;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.lettuce.core.RedisClient;
import io.winterframework.mod.configuration.ConfigurationKey.Parameter;
import io.winterframework.mod.configuration.source.RedisConfigurationSource.RedisConfigurationKey;
import io.winterframework.mod.configuration.source.RedisConfigurationSource.RedisConfigurationQueryResult;

@Disabled
public class RedisConfigurationSourceTest {

	@Test
	public void testRedisConfigurationSourceRedisClient() throws IllegalArgumentException, URISyntaxException {
		RedisClient client = RedisClient.create("redis://localhost:6379");
		
		try {
			RedisConfigurationSource source = new RedisConfigurationSource(client);
			
			source.set("prop1", "abc")
				.and().set("prop2", 42).withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			List<RedisConfigurationQueryResult> result = source.get("prop1")
				.and().get("prop2").withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			Assertions.assertEquals(2, result.size());
			
			Iterator<RedisConfigurationQueryResult> resultIterator = result.iterator();
			
			RedisConfigurationQueryResult current = resultIterator.next();
			Assertions.assertNull(current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop1", current.getResult().get().getKey().getName());
			Assertions.assertEquals(1, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals("abc", current.getResult().get().valueAsString().get());
			
			current = resultIterator.next();
			Assertions.assertNull(current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop2", current.getResult().get().getKey().getName());
			Assertions.assertEquals(1, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals(42, current.getResult().get().valueAsInteger().get());
			
			source.activate().block();
			
			result = source.get("prop1")
				.and().get("prop2").withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			Assertions.assertEquals(2, result.size());
			
			resultIterator = result.iterator();
			
			current = resultIterator.next();
			Assertions.assertEquals(1, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop1", current.getResult().get().getKey().getName());
			Assertions.assertEquals(1, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals("abc", current.getResult().get().valueAsString().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(1, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop2", current.getResult().get().getKey().getName());
			Assertions.assertEquals(1, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals(42, current.getResult().get().valueAsInteger().get());
			
			source.set("prop3", new URI("https://localhost:8443"))
				.and().set("prop2", 84).withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			result = source.get("prop1")
				.and().get("prop3")
				.and().get("prop2").withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			Assertions.assertEquals(3, result.size());
			
			resultIterator = result.iterator();
			
			current = resultIterator.next();
			Assertions.assertEquals(1, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop1", current.getResult().get().getKey().getName());
			Assertions.assertEquals(1, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals("abc", current.getResult().get().valueAsString().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(1, current.getQueryKey().getRevision());
			Assertions.assertFalse(current.getResult().isPresent());
			
			current = resultIterator.next();
			Assertions.assertEquals(1, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop2", current.getResult().get().getKey().getName());
			Assertions.assertEquals(1, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals(42, current.getResult().get().valueAsInteger().get());
			
			result = source.get("prop3").atRevision(2)
				.execute()
				.collectList()
				.block();
			
			Assertions.assertEquals(1, result.size());
			
			resultIterator = result.iterator();
			
			current = resultIterator.next();
			Assertions.assertEquals(2, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop3", current.getResult().get().getKey().getName());
			Assertions.assertEquals(2, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals(new URI("https://localhost:8443"), current.getResult().get().valueAsURI().get());
			
			source.activate(2).block();
			
			result = source.get("prop1")
				.and().get("prop3")
				.and().get("prop2").withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			Assertions.assertEquals(3, result.size());
			
			resultIterator = result.iterator();
			
			current = resultIterator.next();
			Assertions.assertEquals(2, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop1", current.getResult().get().getKey().getName());
			Assertions.assertEquals(1, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals("abc", current.getResult().get().valueAsString().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(2, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop3", current.getResult().get().getKey().getName());
			Assertions.assertEquals(2, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals(new URI("https://localhost:8443"), current.getResult().get().valueAsURI().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(2, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop2", current.getResult().get().getKey().getName());
			Assertions.assertEquals(2, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals(84, current.getResult().get().valueAsInteger().get());
			
			source.set("prop4", "Foo Bar").withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			result = source.get("prop4").withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			Assertions.assertEquals(1, result.size());
			
			resultIterator = result.iterator();
			
			current = resultIterator.next();
			Assertions.assertEquals(2, current.getQueryKey().getRevision());
			Assertions.assertFalse(current.getResult().isPresent());
			
			source.activate(3, "env", "production", "customer", "cust1").block();
			
			result = source.get("prop1")
				.and().get("prop3")
				.and().get("prop2", "prop4").withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			Assertions.assertEquals(4, result.size());
			
			resultIterator = result.iterator();
			
			current = resultIterator.next();
			Assertions.assertEquals(2, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop1", current.getResult().get().getKey().getName());
			Assertions.assertEquals(1, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals("abc", current.getResult().get().valueAsString().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(2, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop3", current.getResult().get().getKey().getName());
			Assertions.assertEquals(2, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals(new URI("https://localhost:8443"), current.getResult().get().valueAsURI().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(3, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop2", current.getResult().get().getKey().getName());
			Assertions.assertEquals(2, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals(84, current.getResult().get().valueAsInteger().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(3, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop4", current.getResult().get().getKey().getName());
			Assertions.assertEquals(3, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals("Foo Bar", current.getResult().get().valueAsString().get());
			
			source.set("prop1", "abcdef")
				.and().set("prop2", 126).withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			result = source.get("prop1")
				.and().get("prop3")
				.and().get("prop2", "prop4").withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			Assertions.assertEquals(4, result.size());
			
			resultIterator = result.iterator();
			
			current = resultIterator.next();
			Assertions.assertEquals(2, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop1", current.getResult().get().getKey().getName());
			Assertions.assertEquals(1, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals("abc", current.getResult().get().valueAsString().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(2, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop3", current.getResult().get().getKey().getName());
			Assertions.assertEquals(2, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals(new URI("https://localhost:8443"), current.getResult().get().valueAsURI().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(3, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop2", current.getResult().get().getKey().getName());
			Assertions.assertEquals(2, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals(84, current.getResult().get().valueAsInteger().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(3, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop4", current.getResult().get().getKey().getName());
			Assertions.assertEquals(3, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals("Foo Bar", current.getResult().get().valueAsString().get());
			
			source.activate().block();
			
			result = source.get("prop1")
				.and().get("prop3")
				.and().get("prop2", "prop4").withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			Assertions.assertEquals(4, result.size());
			
			resultIterator = result.iterator();
			
			current = resultIterator.next();
			Assertions.assertEquals(3, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop1", current.getResult().get().getKey().getName());
			Assertions.assertEquals(3, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals("abcdef", current.getResult().get().valueAsString().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(3, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop3", current.getResult().get().getKey().getName());
			Assertions.assertEquals(2, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals(new URI("https://localhost:8443"), current.getResult().get().valueAsURI().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(3, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop2", current.getResult().get().getKey().getName());
			Assertions.assertEquals(2, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals(84, current.getResult().get().valueAsInteger().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(3, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop4", current.getResult().get().getKey().getName());
			Assertions.assertEquals(3, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals("Foo Bar", current.getResult().get().valueAsString().get());
			
			source.activate("env", "production", "customer", "cust1", "application", "app").block();
			
			result = source.get("prop1")
				.and().get("prop3")
				.and().get("prop2", "prop4").withParameters("env", "production", "customer", "cust1", "application", "app")
				.execute()
				.collectList()
				.block();
			
			Assertions.assertEquals(4, result.size());
			
			resultIterator = result.iterator();
			
			current = resultIterator.next();
			Assertions.assertEquals(3, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop1", current.getResult().get().getKey().getName());
			Assertions.assertEquals(3, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals("abcdef", current.getResult().get().valueAsString().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(3, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop3", current.getResult().get().getKey().getName());
			Assertions.assertEquals(2, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().isEmpty());
			Assertions.assertEquals(new URI("https://localhost:8443"), current.getResult().get().valueAsURI().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(4, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop2", current.getResult().get().getKey().getName());
			Assertions.assertEquals(4, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals(126, current.getResult().get().valueAsInteger().get());
			
			current = resultIterator.next();
			Assertions.assertEquals(4, current.getQueryKey().getRevision());
			Assertions.assertTrue(current.getResult().isPresent());
			Assertions.assertEquals("prop4", current.getResult().get().getKey().getName());
			Assertions.assertEquals(3, ((RedisConfigurationKey)current.getResult().get().getKey()).getRevision());
			Assertions.assertTrue(current.getResult().get().getKey().getParameters().containsAll(List.of(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app"))));
			Assertions.assertEquals("Foo Bar", current.getResult().get().valueAsString().get());
			
			Assertions.assertEquals(4, source.getWorkingRevision().block());
			Assertions.assertEquals(3, source.getActiveRevision().block());
			Assertions.assertEquals(5, source.getWorkingRevision(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app")).block());
			Assertions.assertEquals(4, source.getActiveRevision(Parameter.of("env", "production"), Parameter.of("customer", "cust1"), Parameter.of("application", "app")).block());
			
			// TODO test metadata conflicts use cases
		}
		finally {
			client.connect().reactive().flushall().block();
			client.shutdown();
		}
	}
	
	

}