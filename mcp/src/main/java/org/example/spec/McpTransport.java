package org.example.spec;

import reactor.core.publisher.Mono;

public interface McpTransport {
    default void close() {
        this.closeGracefully().subscribe();
    }

    Mono<Void> closeGracefully();

    Mono<Void> sendMessage(McpSchema.JSONRPCMessage message);
}
