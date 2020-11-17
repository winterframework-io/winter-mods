/**
 * 
 */
package io.winterframework.mod.web.internal.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.winterframework.mod.web.HeaderService;
import io.winterframework.mod.web.Headers;
import io.winterframework.mod.web.Method;
import io.winterframework.mod.web.Parameter;
import io.winterframework.mod.web.Part;
import io.winterframework.mod.web.Request;
import io.winterframework.mod.web.RequestBody;
import io.winterframework.mod.web.RequestHandler;
import io.winterframework.mod.web.Response;
import io.winterframework.mod.web.ResponseBody;
import io.winterframework.mod.web.internal.Charsets;
import io.winterframework.mod.web.internal.RequestBodyDecoder;
import io.winterframework.mod.web.lab.router.BaseContext;
import io.winterframework.mod.web.lab.router.base.GenericBaseRouter;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author jkuhn
 *
 */
public abstract class AbstractHttpServerExchangeBuilder<A extends HttpServerExchange> {

	protected final HeaderService headerService;
	protected final RequestBodyDecoder<Parameter> urlEncodedBodyDecoder;
	protected final RequestBodyDecoder<Part> multipartBodyDecoder;
	
	public AbstractHttpServerExchangeBuilder(HeaderService headerService, RequestBodyDecoder<Parameter> urlEncodedBodyDecoder, RequestBodyDecoder<Part> multipartBodyDecoder) {
		this.headerService = headerService;
		this.urlEncodedBodyDecoder = urlEncodedBodyDecoder;
		this.multipartBodyDecoder = multipartBodyDecoder;
	}
	
	public abstract Mono<A> build(ChannelHandlerContext context);
	
	protected RequestHandler<RequestBody, Void, ResponseBody> findHandler(Request<RequestBody, Void> request) {
		if(request.headers().getMethod() == Method.POST) {
			return multipartEcho();
		}
		else {
			return printRequest();
		}
		
		/*if(handler == null) {
			handler = this.webRouter();
		}
		return handler;*/
	}
	
	private static RequestHandler<RequestBody, Void, ResponseBody> handler;
	
	private RequestHandler<RequestBody, Void, ResponseBody> webRouter() {
		return new GenericBaseRouter(this.headerService)
			.route().path("/toto", true).method(Method.GET).handler(this.simple().map(this::handlerAdapter))
			.route().path("/tata", true).method(Method.POST).handler(this.echo().map(this::handlerAdapter))
			.route().path("/json", true).method(Method.POST).handler(this.json().map(this::handlerAdapter))
			.route().path("/toto/{param1}/tata/{param2}", true).handler(this.a().map(this::handlerAdapter))
			.route().path("/toto/titi/tata/{param2}", true).handler(this.b().map(this::handlerAdapter))
			.route().path("/toto/{param1}/tata/titi", true).handler(this.c().map(this::handlerAdapter));
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> webRouter2() {
		return new GenericBaseRouter(this.headerService)
			.route().path("/toto", true).method(Method.POST).consumes("application/json").handler(this.echo().map(this::handlerAdapter))
			.route().path("/toto", true).method(Method.POST).consumes("text/*").handler(this.printRequest().map(this::handlerAdapter));
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> webRouter3() {
		return new GenericBaseRouter(this.headerService)
			.route().path("/toto", true).method(Method.GET).produces("application/json").handler(
				(request, response) -> {
					response.headers(headers -> headers.contentType("application/json")).body().data().data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("{\"toto\":5}", Charsets.DEFAULT))));
				}
			)
			.route().path("/toto", true).method(Method.GET).produces("text/plain").handler(
				(request, response) -> {
					response.headers(headers -> headers.contentType("text/plain")).body().data().data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("toto 5", Charsets.DEFAULT))));
				}
			)
			.route().path("/tata", true).method(Method.POST).consumes("application/json").produces("application/json").handler(
				(request, response) -> {
					response.headers(headers -> headers.contentType("application/json")).body().data().data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("{\"toto\":5}", Charsets.DEFAULT))));
				}
			);
	}
	
	private RequestHandler<RequestBody, BaseContext, ResponseBody> handlerAdapter(RequestHandler<RequestBody, Void, ResponseBody> handler) {
		return handler.map(h -> (request, response) -> h.handle(request.map(Function.identity(), ign -> null), response));
	}
	
	public static class JsonRequest {
		private String field1;
		
		private String field2;

		public String getField1() {
			return field1;
		}

		public void setField1(String field1) {
			this.field1 = field1;
		}

		public String getField2() {
			return field2;
		}

		public void setField2(String field2) {
			this.field2 = field2;
		}
	}
	
	public static class JsonResponse {
		private String message;
		
		public String getMessage() {
			return message;
		}
		
		public void setMessage(String message) {
			this.message = message;
		}
	}
	
	public static class EntityResponseBody<T> {
		
		private Response<ResponseBody> response;
		
		private T entity;
		
		private EntityResponseBody(Response<ResponseBody> response) {
			this.response = response;
		}
		
		public Response<EntityResponseBody<T>> entity(T entity) {
			this.entity = entity;
			return this.response.map(ign -> this);
		}
		
		private T getEntity() {
			return entity;
		}
	}
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private RequestHandler<RequestBody, Void, ResponseBody> json() {
		
		Function<JsonRequest, JsonResponse> handler0 = request -> {
			JsonResponse response = new JsonResponse();
			response.setMessage("Received request with field1 " + request.field1 + " and field2 " + request.field2);
			return response;
		};
		
		RequestHandler<JsonRequest, Void, EntityResponseBody<JsonResponse>> handler1 = (request, response) -> {
			response.headers(headers -> headers.contentType("application/json")).body().entity(handler0.apply(request.body().get())); 
		};
		
		return handler1.map(handler -> 
			 (request, response) -> {
				 
				if(request.headers().<Headers.ContentType>get(Headers.CONTENT_TYPE).get().getMediaType().equals("application/json")) {
					// convert json
				}
				else if(request.headers().<Headers.ContentType>get(Headers.CONTENT_TYPE).get().getMediaType().equals("application/xml")) {
					// convert xml
				}
				 
				response.body().data().data(
					request.body().get().data().data()
						.reduce(new ByteArrayOutputStream(), (out, chunk) -> {
							try {
								chunk.getBytes(chunk.readerIndex(), out, chunk.readableBytes());
							} 
							catch (IOException e) {
								throw Exceptions.propagate(e);
							}
							return out;
						})
						.map(ByteArrayOutputStream::toByteArray)
						.map(bytes -> {
							try {
								return this.mapper.readValue(bytes, JsonRequest.class);
							}
							catch (IOException e) {
								throw Exceptions.propagate(e);
							}
						})
						.map(jsonRequest -> {
							Request<JsonRequest, Void> entityRequest = request.map(ign -> jsonRequest, Function.identity());
							Response<EntityResponseBody<JsonResponse>> entityResponse = response.map(body -> {
								return new EntityResponseBody<>(response);
							});
							handler.handle(entityRequest, entityResponse);
							
							// response entity can come in an asynchronous way so we must delegate the whole process to the other handler
							// if we want to chain things we need to use publishers
							// handler1 is actually synchronous since there are no publisher accessible in handler1
							
							return entityResponse.body().getEntity();
						})
						.flatMap(jsonResponse -> {
							try {
								return Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(this.mapper.writeValueAsBytes(jsonResponse))));
							} 
							catch (JsonProcessingException e) {
								throw Exceptions.propagate(e);
							}
						})
					);
		});
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> hello() {
		return (request, response) -> response
			.headers(headers -> headers
				.status(200)
				.contentType("text/html; charset=\"UTF-8\"")
			)
			.body().data().data(
				Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("<html><head><title>Winter Web</title></head><body><h1>Hello</h1></body></html>", Charsets.UTF_8)))
			);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> a() {
		return (request, response) -> response
			.headers(headers -> headers
				.status(200)
				.contentType("text/plain; charset=\"UTF-8\"")
			)
			.body().data().data(
				Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("= A =", Charsets.UTF_8)))
			);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> b() {
		return (request, response) -> response
			.headers(headers -> headers
				.status(200)
				.contentType("text/plain; charset=\"UTF-8\"")
			)
			.body().data().data(
				Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("= B =", Charsets.UTF_8)))
			);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> c() {
		return (request, response) -> response
			.headers(headers -> headers
				.status(200)
				.contentType("text/plain; charset=\"UTF-8\"")
			)
			.body().data().data(
				Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("= C =", Charsets.UTF_8)))
			);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> simple() {
		return (request, response) -> response
			.headers(headers -> headers
				.status(200)
				.contentType("text/plain; charset=\"UTF-8\"")
				.add("test", "1235")
			)
			.body().data().data(
				Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Server Response : Version - HTTP/2", Charsets.UTF_8)))
			);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> interval() {
		return (request, response) -> {
			response.headers(headers -> {
				headers.status(200)
					.contentType("text/plain; charset=\"UTF-8\"")
					.add("test", "1235");
			});
			
			request.body().ifPresentOrElse(
				body ->	body.data().data().subscribe(
					buffer -> {
						System.out.println("=================================");
				        System.out.println(buffer.toString(Optional.ofNullable(request.headers().getCharset()).orElse(Charsets.UTF_8)));
				        System.out.println("=================================");
					},
					ex -> {
						ex.printStackTrace();
					},
					() -> {
						System.out.println("Body complete");
						
							//response.body().empty();						
							//response.body().data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Server Response : Version - HTTP/2", Charsets.UTF_8))));
						
						response.body().data().data(Flux.interval(Duration.ofMillis(500)).map(index -> Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response " + index + ", \r\n", Charsets.UTF_8))));
						
						/*response.body().data().data(Flux.just(
								Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response A, \r\n", Charsets.UTF_8)),
								Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response B, \r\n", Charsets.UTF_8)),
								Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response C, \r\n", Charsets.UTF_8)),
								Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response D, \r\n", Charsets.UTF_8)),
								Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response E \r\n", Charsets.UTF_8))
							)
							.delayElements(Duration.ofMillis(500)) 
						);*/
						
						// Why do I loose first message? 
						// We are using one single thread, when next is invoked the chunk is submit to the eventloopgroup whose thread is already busy right here so the chunk is placed in a queue until the thread gets back to the eventloopgroup 
						// This is not good for several reasons:
						// - it blocks the event loop group
						// - potentially we can loose messages if the data flux replay buffer in the response gets full => at least we should have a clear error explaining what went terribly wrong 
						/*response.body().data(Flux.create(emitter -> {
							try {
								emitter.next(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response A, \r\n", Charsets.UTF_8)));
								System.out.println("Emit A");
								Thread.sleep(1000);
								emitter.next(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response B, \r\n", Charsets.UTF_8)));
								System.out.println("Emit B");
								Thread.sleep(1000);
								emitter.next(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response C, \r\n", Charsets.UTF_8)));
								System.out.println("Emit C");
								Thread.sleep(1000);
								emitter.next(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response D \r\n", Charsets.UTF_8)));
								System.out.println("Emit D");
								emitter.complete();
							} 
							catch (InterruptedException e) {
								e.printStackTrace();
							}
						}));*/
						
						/*response.body().data(Flux.create(emitter -> {
							new Thread(() -> {
								try {
									for(int i=0;i<100;i++) {
										emitter.next(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response A, \r\n", Charsets.UTF_8)));
										Thread.sleep(100);
									}
									emitter.complete();
								} 
								catch (InterruptedException e) {
									e.printStackTrace();
								}
							}).start();
						}));*/
					}),
				() -> response.body().data().data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("=== Empty ===", Charsets.UTF_8))))
			);
		};
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> urlEncoded() {
		return (request, response) -> response
			.headers(headers -> headers
				.status(200)
				.contentType("text/plain")
				.charset(Charsets.UTF_8)
			)
			.body().data()
			.data(
				request.body()
					.map(body -> body.urlEncoded()
						.parameters()
						.collectList()
						.map(parameters -> "Received parameters: " + parameters.stream().map(param -> param.getName() + " = " + param.getValue()).collect(Collectors.joining(", ")))
						.map(result -> Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(result, Charsets.UTF_8)))
					)
					.orElse(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("=== Empty ===", Charsets.UTF_8))))
			);
	}
	
	
		
	private RequestHandler<RequestBody, Void, ResponseBody> echo() {
		return (request, response) -> {
			response
				.headers(headers -> headers.status(200).contentType("text/plain"))
				.body().data().data(request.body()
					.map(body -> body.data().data().doOnNext(chunk -> chunk.retain()))	
					.orElse(Flux.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("=== Empty ===", Charsets.UTF_8))))
				);
		};
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> multipartEcho() {
		return (request, response) -> {
			Flux<ByteBuf> responseData = request.body()
				.map(body -> body.multipart().parts().flatMapSequential(part -> {
					ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.buffer(256));
					
					buf.writeCharSequence("================================================================================\n", Charsets.UTF_8);
					buf.writeCharSequence("name: " + part.getName() + "\n", Charsets.UTF_8);
					part.getFilename().ifPresent(filename -> buf.writeCharSequence("filename: " + filename + "\n", Charsets.UTF_8));
					buf.writeCharSequence("content-type: " + part.headers().getContentType() + "\n", Charsets.UTF_8);
					buf.writeCharSequence("charset: " + part.headers().getCharset() + "\n", Charsets.UTF_8);
					buf.writeCharSequence("size: " + part.headers().getSize() + "\n", Charsets.UTF_8);
					String headers = "headers:\n";
					headers += part.headers().getAll().entrySet().stream()
						.flatMap(e -> {
							return e.getValue().stream();
						})
						.map(h -> "  - " + headerService.encode(h))
						.collect(Collectors.joining("\n"));
					buf.writeCharSequence(headers + "\n", Charsets.UTF_8);
					buf.writeCharSequence("data: \n[", Charsets.UTF_8);
					
					return Flux.concat(
						Mono.<ByteBuf>just(buf),
						part.data().doOnNext(chunk -> chunk.retain()),
						Mono.<ByteBuf>just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("]\n================================================================================\r\n", Charsets.UTF_8)))
					);
				}))
				.orElse(Flux.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("=== Empty ===", Charsets.UTF_8))));
			
			response
				.headers(headers -> headers
					.status(200)
					.contentType("text/plain")
					.charset(Charsets.UTF_8)
				)
				.body().data().data(responseData);
		};
	}
	
	// TODO FileRegion
	// TODO we should provide a specific FilePart with adhoc methods to manipulate the File data flux or some utilities to make this simpler (especially regarding error handling, size limits...): a Part to Mono<File> mapper would be interesting as it would allow to chain the flux to the response data
	// TODO progressive upload can also be done: sse can do the trick but we should see other client side tricks for this as well
	// TODO it seems the size of the resulting file doesn't match the source why?
	private RequestHandler<RequestBody, Void, ResponseBody> multipartSaveFile() {
		return (request, response) -> 
			request.body().ifPresentOrElse(
				body -> body.multipart().parts().filter(p -> p.getFilename().isPresent()).subscribe(
					filePart -> {
						try {
							SeekableByteChannel byteChannel = Files.newByteChannel(Paths.get("uploads/" + filePart.getFilename().get()), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
							
							filePart.data().subscribe(
								chunk -> {
									System.out.println("File chunk: " + chunk.readableBytes());
									try {
										byteChannel.write(chunk.nioBuffer());
									} catch (IOException e) {
										e.printStackTrace();
									}
								},
								ex -> {
									
								},
								() -> {
									System.out.println("Saved file: " + filePart.getFilename().get());
									try {
										byteChannel.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							);
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					},
					ex -> {
						response
						.headers(headers -> headers
							.status(500)
							.contentType("text/plain")
							.charset(Charsets.UTF_8)
						)
						.body().empty();
					},
					() -> {
						response
						.headers(headers -> headers
							.status(200)
							.contentType("text/plain")
							.charset(Charsets.UTF_8)
						)
						.body().empty();
					}
				),
			() -> response.body().data().data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("=== Empty ===", Charsets.UTF_8))))
		);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> echoParameters() {
		return (request, response) -> response
			.headers(headers -> headers
				.status(200)
				.contentType("text/plain; charset=UTF-8")
				.add("test", "1235")
			)
			.body().data().data(
				//Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Received parameters: " + request.parameters().getAll().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().stream().map(Parameter::getValue).collect(Collectors.joining(", "))).collect(Collectors.joining(", ")), Charsets.UTF_8)))
				Mono.just(Unpooled.copiedBuffer("Received parameters: " + request.parameters().getAll().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().stream().map(Parameter::getValue).collect(Collectors.joining(", "))).collect(Collectors.joining(", ")), Charsets.UTF_8))
			);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> stream() {
		return (request, response) -> response
			.headers(headers -> headers
				.status(200)
				.contentType("text/plain; charset=\"UTF-8\"")
				.add("test", "1235")
			)
			.body().data().data(
				Flux.just(
					Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response A, \r\n", Charsets.UTF_8)),
					Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response B, \r\n", Charsets.UTF_8)),
					Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response C, \r\n", Charsets.UTF_8)),
					Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response D, \r\n", Charsets.UTF_8)),
					Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Response E \r\n", Charsets.UTF_8))
				)
				.delayElements(Duration.ofMillis(500)) 
			);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> sse() {
		return (request, response) -> response
			.body().sse().events(
				Flux.interval(Duration.ofSeconds(1))
					.map(sequence -> response.body().sse().create(configurator -> configurator
							.id(Long.toString(sequence))
							.event("periodic-event")
							.comment("some comment \n on mutliple lines")
							.data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("SSE - " + LocalTime.now().toString() + "\r\n", Charsets.UTF_8))))
						)
					)
					.doOnNext(evt -> System.out.println("Emit sse"))
			);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> sse2() {
		return (request, response) -> response
			.body().sse().events(
				Flux.range(0, 10)
					.map(sequence -> response.body().sse().create(configurator -> configurator
							.id(Long.toString(sequence))
							.event("periodic-event")
							.comment("some comment")
							.data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("SSE - " + LocalTime.now().toString() + "\r\n", Charsets.UTF_8))))
						)
					)
					.doOnNext(evt -> System.out.println("Emit sse"))
					.delayElements(Duration.ofSeconds(1))
			);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> printRequest() {
		return (request, response) -> {
			ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.buffer(256));
			
			buf.writeCharSequence("authority: " + request.headers().getAuthority() + "\n", Charsets.UTF_8);
			buf.writeCharSequence("path: " + request.headers().getPath() + "\n", Charsets.UTF_8);
			buf.writeCharSequence("method: " + request.headers().getMethod() + "\n", Charsets.UTF_8);
			buf.writeCharSequence("scheme: " + request.headers().getScheme() + "\n", Charsets.UTF_8);
			buf.writeCharSequence("content-type: " + request.headers().getContentType() + "\n", Charsets.UTF_8);
			buf.writeCharSequence("charset: " + request.headers().getCharset() + "\n", Charsets.UTF_8);
			buf.writeCharSequence("size: " + request.headers().getSize() + "\n", Charsets.UTF_8);
			String headers = "headers:\n";
			headers += request.headers().getAll().entrySet().stream()
				.flatMap(e -> {
					return e.getValue().stream();
				})
				.map(h -> "  - " + headerService.encode(h))
				.collect(Collectors.joining("\n"));
			buf.writeCharSequence(headers + "\n", Charsets.UTF_8);
			
			String cookies = "cookies:\n";
			cookies += request.cookies().getAll().entrySet().stream()
				.flatMap(e -> e.getValue().stream())
				.map(c -> "  - " + c.getName() + "=" + c.getValue())
				.collect(Collectors.joining("\n"));
			buf.writeCharSequence(cookies + "\n", Charsets.UTF_8);
			
			String parameters = "parameters:\n";
			parameters += request.parameters().getAll().entrySet().stream()
				.map(e -> "  - " + e.getKey() + "=" + e.getValue().stream().map(Parameter::getValue).collect(Collectors.joining(", ")))
				.collect(Collectors.joining("\n"));
			buf.writeCharSequence(parameters + "\n", Charsets.UTF_8);
			
			response.headers(configurator -> configurator
					.status(200)
					.contentType("text/plain")
					.charset(Charsets.DEFAULT)
				)
				.body().data().data(Mono.just(buf));
		};
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> printRequestSetCookie() {
		return printRequest().map(handler -> (request, response) -> {
				response.cookies(cookies -> cookies.addCookie("test-cookie", "123465"));
				handler.handle(request, response);
			}
		);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> printRequestSetCookie2() {
		return printRequest().doAfter(
				(request, response) -> response.cookies(cookies -> cookies.addCookie("test-cookie", "123465"))
		);
	}
	
	private RequestHandler<RequestBody, Void, ResponseBody> setCookie() {
		return (request, response) -> response
			.headers(headers -> headers.status(200).contentType("text/plain"))
			.cookies(cookies -> cookies.addCookie("test-cookie", "123465"))
			.body().data().data(Mono.just(Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Set cookie", Charsets.DEFAULT))));
	}
}