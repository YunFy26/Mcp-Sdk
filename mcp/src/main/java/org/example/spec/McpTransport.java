package org.example.spec;

import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 定义模型上下文协议（MCP）的异步传输层。
 *
 * <p>
 * McpTransport 接口为在模型上下文协议中实现自定义传输机制奠定基础。它负责处理客户端与服务器组件之间的双向通信，
 * 支持使用 JSON-RPC 格式进行异步消息交换。
 * </p>
 *
 * <p>
 * 此接口的实现类需负责以下事项：
 * </p>
 * <ul>
 * <li>管理传输连接的生命周期</li>
 * <li>处理来自服务器的入站消息和错误</li>
 * <li>向服务器发送出站消息</li>
 * </ul>
 *
 * <p>
 * 传输层设计为与具体协议无关，支持多种实现方式，如 WebSocket、HTTP 或自定义协议。
 * </p>
 *
 */
public interface McpTransport {

    /**
     * 关闭传输连接并释放所有关联资源
     * <p>
     * 此方法确保在不再需要传输时正确清理资源，它应该能够正常关闭所有活动连接。
     * </p>
     * subscribe()用来触发closeGracefully()操作
     */
    default void close() {
        this.closeGracefully().subscribe();
    }

    /**
     * 异步关闭连接并释放资源
     *
     * @return 一个完成的 {@link Mono<Void>}，表示连接已成功关闭
     */
    Mono<Void> closeGracefully();

    /**
     * 异步发送消息
     *
     * @param message JSON-RPC 格式的消息
     * @return 一个完成的 {@link Mono<Void>}，表示消息已成功发送
     */
    Mono<Void> sendMessage(McpSchema.JSONRPCMessage message);

    /**
     * 反序列化，将原始数据（如 JSON 字符串、字节流）转换为指定类型的 Java 对象
     * @param data 原始数据
     * @param typeRef 指定的类型引用，用于指示目标类型
     * @return 转换后的Java对象
     * @param <T> 目标类型
     */
    <T> T unmarshalFrom(Object data, TypeReference<T> typeRef);

    /**
     * 获取支持的协议版本列表
     *
     * @return 支持的协议版本列表
     */
    default List<String> protocolVersions() {
        return List.of(ProtocolVersions.MCP_2024_11_05);
    }

}
