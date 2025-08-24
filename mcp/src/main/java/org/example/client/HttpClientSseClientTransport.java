package org.example.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.example.enums.HttpResponseStatusCode;
import org.example.spec.McpClientTransport;
import org.example.spec.McpSchema;
import org.example.spec.McpTransportException;
import org.example.spec.ProtocolVersions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * 基于SSE的{@link org.example.spec.McpTransport}实现，
 * 遵循MCP的HTTP与SSE传输规范，使用Java的HttpClient。
 *
 * <p>
 * 此传输实现通过以下方式在客户端与服务器之间建立双向通信通道：
 * 使用SSE接收服务器到客户端的消息，使用HTTP POST请求发送客户端到服务器的消息。
 * 该传输具有以下功能：
 * <ul>
 * <li>建立SSE连接以接收服务器消息</li>
 * <li>通过SSE事件处理端点发现</li>
 * <li>使用Jackson处理消息的序列化/反序列化</li>
 * <li>提供优雅的连接终止机制</li>
 * </ul>
 *
 * <p>
 * 此传输支持两种类型的SSE事件：
 * <ul>
 * <li>'endpoint' - 包含用于发送客户端消息的URL</li>
 * <li>'message' - 包含JSON-RPC消息 payload</li>
 * </ul>
 *
 * @see org.example.spec.McpTransport
 * @see org.example.spec.McpClientTransport
 */
public class HttpClientSseClientTransport implements McpClientTransport {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientSseClientTransport.class);

    /**
     * TODO: add annotation
     */
    private final URI baseUri;

    /**
     * TODO: add annotation
     */
    private final String sseEndpoint;

    /**
     * TODO: add annotation
     */
    private final HttpClient httpClient;

    /**
     * TODO: add annotation
     */
    private final HttpRequest.Builder requestBuilder;

    /**
     * TODO: add annotation
     */
    protected ObjectMapper objectMapper;

    /**
     * TODO: add annotation
     */
    private volatile boolean isClosing = false;

    /**
     * TODO: add annotation
     */
    protected final Sinks.One<String> messageEndpointSink = Sinks.one();

    /**
     * TODO: add annotation
     */
    private final AsyncHttpRequestCustomizer httpRequestCustomizer;


    /**
     * sse订阅的原子引用
     */
    private final AtomicReference<Disposable> sseSubscription = new AtomicReference<>();


    @Override
    public Mono<Void> connect(Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> handler) {
        return null;
    }

    @Override
    public void setExceptionHandler(Consumer<Throwable> handler) {
        McpClientTransport.super.setExceptionHandler(handler);
    }

    @Override
    public void close() {
        McpClientTransport.super.close();
    }

    /**
     * 优雅关闭传输连接
     *
     * <p>
     * 设置关闭标志并关闭sse连接，这会阻止发送新的消息并允许正在进行的操作完成
     * <p/>
     * @return 当关闭操作启动时，返回一个正常完成状态的 {@link Mono<Void>}
     */
    @Override
    public Mono<Void> closeGracefully() {

        return Mono.fromRunnable(() -> {
            // 设置关闭标志，阻止新的操作
            isClosing = true;
            // 从原子引用中获取sse订阅对象
            Disposable subscription = sseSubscription.get();
            if (subscription != null && !subscription.isDisposed()) {
                // 取消订阅以关闭SSE连接
                subscription.dispose();
            }
        });
    }

    /**
     *
     * @param message JSON-RPC 格式的消息
     * @return 当消息成功发送时，返回一个正常完成状态的 {@link Mono<Void>}
     * @throws McpError 如果endpoint不可用或超时，抛出 McpError
     */
    @Override
    public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
        return messageEndpointSink.asMono().flatMap(messageEndpointUri -> {
            if (isClosing) {
                return Mono.empty();
            }
            return serializeMessage(message)
                .flatMap(body -> sendHttpPost(messageEndpointUri, body).handle(((httpResponse, sink) -> {
                    if (httpResponse.statusCode() != HttpResponseStatusCode.SUCCESS_200_OK &&
                        httpResponse.statusCode() != HttpResponseStatusCode.SUCCESS_201_CREATED &&
                        httpResponse.statusCode() != HttpResponseStatusCode.SUCCESS_202_ACCEPTED &&
                        httpResponse.statusCode() != HttpResponseStatusCode.SUCCESS_206_PARTIAL_CONTENT) {

                        sink.error(new McpTransportException("Sending message failed with a non-OK HTTP code: "
                            + httpResponse.statusCode() + " - " + httpResponse.body()));
                    }
                }))
                .doOnError(error -> {
                    if (!isClosing) {
                        logger.error("Error sending message: {}", error.getMessage());
                    }
                }));
        }).then();
    }

    /**
     * 将Java对象序列化为JSON字符串
     *
     * @param message 要序列化的消息对象
     * @return 一个包含序列化结果的 {@link Mono<String>}，如果序列化失败则返回错误
     */
    private Mono<String> serializeMessage(final McpSchema.JSONRPCMessage message) {
        return Mono.defer(() -> {
            try {
                return Mono.just(objectMapper.writeValueAsString(message));
            }catch (IOException e) {
                return Mono.error(new McpTransportException("Failed to serialize message", e));
            }
        });
    }

    private Mono<HttpResponse<String>> sendHttpPost(final String endpoint, final String body) {

    }

    /**
     * 反序列化，将原始数据（如 JSON 字符串、字节流）转换为指定类型的 Java 对象
     *
     * @param data 原始数据
     * @param typeRef 指定的类型引用，用于指示目标类型
     * @param <T> 目标类型
     * @return 转换后的Java对象
     */
    @Override
    public <T> T unmarshalFrom(Object data, TypeReference<T> typeRef) {
        return objectMapper.convertValue(data, typeRef);
    }

    @Override
    public List<String> protocolVersions() {
        return List.of(ProtocolVersions.MCP_2024_11_05);
    }
}
