package org.example.spec;

import java.util.function.Consumer;
import java.util.function.Function;

import reactor.core.publisher.Mono;

/**
 * {@link McpTransport}的客户端接口。它允许为从MCP服务器传入的消息设置处理器，为传输层上抛出的异常设置异常处理器
 */
public interface McpClientTransport extends McpTransport {

    /**
     * 用于注册入站消息的处理器，并连接到服务器
     * <p>{@link Function} 是函数式接口，定义如何处理从服务器收到的消息</p>
     * <p>{@link Mono<>}意味着 {@link McpSchema.JSONRPCMessage}是一个异步事件，可能现在收到，也可能未来某个时候收到</p>
     *
     * @param handler 入站消息的处理器
     * @return 返回一个 {@link Mono} 当handler都注册完成后，表示客户端已准备就绪，可以接收消息了
     */
    Mono<Void> connect(Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> handler);

    /**
     * 为传输层上抛出的异常设置异常处理器
     * <p>{@link Consumer} 是函数式接口，接收一个{@link Throwable}类型的对象，定义如何处理捕获到的异常</p>
     *
     * @param handler 异常处理器
     */
    default void setExceptionHandler(Consumer<Throwable> handler) {
    }

}