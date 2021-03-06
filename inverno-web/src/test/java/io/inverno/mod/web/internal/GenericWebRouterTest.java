package io.inverno.mod.web.internal;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.inverno.mod.base.net.URIs;
import io.inverno.mod.base.resource.MediaTypes;
import io.inverno.mod.http.base.Method;
import io.inverno.mod.http.server.HttpServerConfiguration;
import io.inverno.mod.web.Route;
import io.inverno.mod.web.WebExchange;
import io.inverno.mod.web.WebRoute;
import io.inverno.mod.web.WebConfiguration;

public class GenericWebRouterTest {

	private static final WebConfiguration CONFIGURATION = new WebConfiguration() {
		@Override
		public HttpServerConfiguration http_server() {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	@Test
	public void testGetRoutes() {
		GenericWebRouter router = new GenericWebRouter(CONFIGURATION, null, null, null);
		router
			.route().consumes(MediaTypes.APPLICATION_JSON).consumes(MediaTypes.TEXT_HTML).handler(exhange -> {})
			.route().method(Method.GET).method(Method.POST).language("fr-FR").language("en-US").handler(exhange -> {})
			.route().produces(MediaTypes.APPLICATION_JSON).handler(exhange -> {})
			.route().produces(MediaTypes.TEXT_HTML).handler(exhange -> {})
			.route().path("/hello", true).handler(exchange -> {})
			.route().path("/hello/{param1}").handler(exchange -> {})
			.route().path("/hello/{param1}/{param2:[a-b]*}").handler(exchange -> {});
		
		Set<WebRoute<WebExchange>> routes = router.getRoutes();
		
		// 1. consume=JSON *
		// 2. consume=HTML *
		// 3. method=GET, language=fr-FR *
		// 4. method=GET, language=en-US *
		// 5. method=POST, language=fr-FR *
		// 6. method=POST, language=en-US *
		// 7. produce=JSON *
		// 8. produce=HTML *
		// 9. path = /hello
		// 10. path = /hello/
		// 11. path = /hello/{param1}
		// 12. path = /hello/{param1}/{param2:[a-b]*}
		
		Assertions.assertEquals(12, routes.size());
		
		GenericWebRoute route1 = new GenericWebRoute(router);
		route1.setConsume(MediaTypes.APPLICATION_JSON);
		
		GenericWebRoute route2 = new GenericWebRoute(router);
		route2.setConsume(MediaTypes.TEXT_HTML);
		
		GenericWebRoute route3 = new GenericWebRoute(router);
		route3.setMethod(Method.GET);
		route3.setLanguage("fr-FR");
		
		GenericWebRoute route4 = new GenericWebRoute(router);
		route4.setMethod(Method.GET);
		route4.setLanguage("en-US");
		
		GenericWebRoute route5 = new GenericWebRoute(router);
		route5.setMethod(Method.POST);
		route5.setLanguage("fr-FR");
		
		GenericWebRoute route6 = new GenericWebRoute(router);
		route6.setMethod(Method.POST);
		route6.setLanguage("en-US");
		
		GenericWebRoute route7 = new GenericWebRoute(router);
		route7.setProduce(MediaTypes.APPLICATION_JSON);
		
		GenericWebRoute route8 = new GenericWebRoute(router);
		route8.setProduce(MediaTypes.TEXT_HTML);
		
		GenericWebRoute route9 = new GenericWebRoute(router);
		route9.setPath("/hello");
		
		GenericWebRoute route10 = new GenericWebRoute(router);
		route10.setPath("/hello/");
		
		GenericWebRoute route11 = new GenericWebRoute(router);
		route11.setPath("/hello/{param1}");
		route11.setPathPattern(URIs.uri("/hello/{param1}", URIs.Option.NORMALIZED, URIs.Option.PARAMETERIZED).buildPathPattern(false));
		
		GenericWebRoute route12 = new GenericWebRoute(router);
		route12.setPath("/hello/{param1}/{param2:[a-b]*}");
		route12.setPathPattern(URIs.uri("/hello/{param1}/{param2:[a-b]*}", URIs.Option.NORMALIZED, URIs.Option.PARAMETERIZED).buildPathPattern(false));
		
		Assertions.assertEquals(Set.of(route1, route2, route3, route4, route5, route6, route7, route8, route9, route10, route11, route12), routes);
	}
	
	@Test
	public void testFindRoutes() {
		GenericWebRouter router = new GenericWebRouter(CONFIGURATION, null, null, null);
		router
			.route().consumes(MediaTypes.APPLICATION_JSON).consumes(MediaTypes.TEXT_HTML).handler(exhange -> {})
			.route().method(Method.GET).method(Method.POST).language("fr-FR").language("en-US").handler(exhange -> {})
			.route().produces(MediaTypes.APPLICATION_JSON).handler(exhange -> {})
			.route().produces(MediaTypes.TEXT_HTML).handler(exhange -> {})
			.route().path("/hello", true).handler(exchange -> {})
			.route().path("/hello/{param1}").handler(exchange -> {})
			.route().path("/hello/{param1}/{param2:[a-b]*}").handler(exchange -> {});
		
		GenericWebRoute route1 = new GenericWebRoute(router);
		route1.setConsume(MediaTypes.APPLICATION_JSON);
		
		GenericWebRoute route2 = new GenericWebRoute(router);
		route2.setMethod(Method.GET);
		route2.setLanguage("fr-FR");
		
		GenericWebRoute route3 = new GenericWebRoute(router);
		route3.setMethod(Method.POST);
		route3.setLanguage("fr-FR");
		
		Set<WebRoute<WebExchange>> routes = router.route().language("fr-FR").findRoutes();
		Assertions.assertEquals(2, routes.size());
		Assertions.assertEquals(Set.of(route2, route3), routes);
		
		routes = router.route().method(Method.GET).language("fr-FR").findRoutes();
		Assertions.assertEquals(1, routes.size());
		Assertions.assertEquals(Set.of(route2), routes);
		
		routes = router.route().method(Method.PUT).language("fr-FR").findRoutes();
		Assertions.assertEquals(0, routes.size());
		
		routes = router.route().consumes(MediaTypes.APPLICATION_JSON).findRoutes();
		Assertions.assertEquals(1, routes.size());
		Assertions.assertEquals(Set.of(route1), routes);
	}
	
	@Test
	public void testRouteRemove() {
		GenericWebRouter router = new GenericWebRouter(CONFIGURATION, null, null, null);
		router
			.route().consumes(MediaTypes.APPLICATION_JSON).consumes(MediaTypes.TEXT_HTML).handler(exhange -> {})
			.route().method(Method.GET).method(Method.POST).language("fr-FR").language("en-US").handler(exhange -> {})
			.route().produces(MediaTypes.APPLICATION_JSON).handler(exhange -> {})
			.route().produces(MediaTypes.TEXT_HTML).handler(exhange -> {})
			.route().path("/hello", true).handler(exchange -> {})
			.route().path("/hello/{param1}").handler(exchange -> {})
			.route().path("/hello/{param1}/{param2:[a-b]*}").handler(exchange -> {});
		
		Set<WebRoute<WebExchange>> routes = router.getRoutes();
		Assertions.assertEquals(12, routes.size());
		
		routes = router.route().consumes(MediaTypes.APPLICATION_JSON).findRoutes();
		WebRoute<WebExchange> removedRoute = routes.iterator().next();
		removedRoute.remove();
		
		routes = router.getRoutes();
		Assertions.assertEquals(11, routes.size());
	}
	
	@Test
	public void testRouteEnableDisable() {
		GenericWebRouter router = new GenericWebRouter(CONFIGURATION, null, null, null);
		router
			.route().consumes(MediaTypes.APPLICATION_JSON).consumes(MediaTypes.TEXT_HTML).handler(exhange -> {})
			.route().method(Method.GET).method(Method.POST).language("fr-FR").language("en-US").handler(exhange -> {})
			.route().produces(MediaTypes.APPLICATION_JSON).handler(exhange -> {})
			.route().produces(MediaTypes.TEXT_HTML).handler(exhange -> {})
			.route().path("/hello", true).handler(exchange -> {})
			.route().path("/hello/{param1}").handler(exchange -> {})
			.route().path("/hello/{param1}/{param2:[a-b]*}").handler(exchange -> {});
		
		Set<WebRoute<WebExchange>> routes = router.getRoutes();
		Assertions.assertEquals(12, routes.size());
		
		routes = router.route().consumes(MediaTypes.APPLICATION_JSON).findRoutes();
		WebRoute<WebExchange> disabledRoute = routes.iterator().next();
		disabledRoute.disable();
		
		routes = router.getRoutes();
		Assertions.assertEquals(12, routes.size());
		
		Optional<WebRoute<WebExchange>> disabledRouteOptional = routes.stream().filter(Route::isDisabled).findFirst();
		Assertions.assertTrue(disabledRouteOptional.isPresent());
		Assertions.assertEquals(disabledRoute, disabledRouteOptional.get());
		
		disabledRoute.enable();
		
		routes = router.getRoutes();
		Assertions.assertEquals(12, routes.size());
		Assertions.assertTrue(routes.stream().noneMatch(Route::isDisabled));
	}
	
	@Test
	public void testMixPathroute() {
		GenericWebRouter router = new GenericWebRouter(CONFIGURATION, null, null, null);
		router
			.route()
				.path("/hello", true)
				.path("/hello/{param1}")
				.path("/hello/{param1}/{param2:[a-b]*}")
				.handler(exchange -> {});

		// Should result in 4 routes
		Set<WebRoute<WebExchange>> routes = router.getRoutes();
		Assertions.assertEquals(4, routes.size());
	}
	
}
