package org.example.util;

import reactor.util.annotation.Nullable;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * 通用工具方法类
 */
public final class Utils {

    /**
     * 检查给定的 {@code String} 是否包含实际的 <em>文本内容</em>
     * <p>
     * 更具体地说，当且仅当以下所有条件满足时，此方法返回 {@code true}：
     * 1. {@code String} 不为 {@code null}；
     * 2. {@code String} 的长度大于 0；
     * 3. {@code String} 至少包含一个非空白字符。
     * @param str 要检查的 {@code String}（可为 {@code null}）
     * @return 若 {@code String} 不为 {@code null}、长度大于 0 且不仅包含空白字符，则返回 {@code true}，否则返回 {@code false}
     * @see Character#isWhitespace
     */
    public static boolean hasText(@Nullable String str) {
        return (str != null && !str.isBlank());
    }

    /**
     * 若传入的 Collection 为 {@code null} 或为空集合，则返回 {@code true}；否则返回 {@code false}。
     * @param collection 要检查的 Collection
     * @return 给定的 Collection 是否为空
     */
    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    /**
     * 若传入的 Map 为 {@code null} 或为空映射，则返回 {@code true}；否则返回 {@code false}。
     * @param map 要检查的 Map
     * @return 给定的 Map 是否为空
     */
    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * 根据基础 URL（baseUrl）解析给定的端点 URL（endpointUrl）。
     * <ul>
     * <li>若端点 URL 是相对路径，则会基于基础 URL 进行解析；</li>
     * <li>若端点 URL 是绝对路径，则会验证其是否与基础 URL 的协议（scheme）、授权信息（authority）和路径前缀（path prefix）匹配；</li>
     * <li>若绝对路径的端点 URL 验证失败，则抛出 {@link IllegalArgumentException} 异常。</li>
     * </ul>
     * @param baseUrl 基础 URL（必须是绝对路径）
     * @param endpointUrl 端点 URL（可为相对路径或绝对路径）
     * @return 解析后的端点 URI
     * @throws IllegalArgumentException 若绝对路径的端点 URL 与基础 URL 不匹配，或 URI 格式非法
     */
    public static URI resolveUri(URI baseUrl, String endpointUrl) {
        if (!hasText(endpointUrl)) {
            return baseUrl;
        }
        URI endpointUri = URI.create(endpointUrl);
        if (endpointUri.isAbsolute() && !isUnderBaseUri(baseUrl, endpointUri)) {
            throw new IllegalArgumentException("绝对路径的端点 URL 与基础 URL 不匹配。");
        }
        else {
            return baseUrl.resolve(endpointUri);
        }
    }

    /**
     * 检查给定的绝对路径端点 URI 是否在基础 URI（baseUri）的层级范围内。
     * 验证内容包括：协议（scheme）、授权信息（authority，包含主机和端口），并确保基础路径（base path）是端点路径（endpoint path）的前缀。
     * @param baseUri 基础 URI
     * @param endpointUri 要检查的端点 URI
     * @return 若 endpointUri 在 baseUri 的层级范围内，则返回 {@code true}，否则返回 {@code false}
     */
    private static boolean isUnderBaseUri(URI baseUri, URI endpointUri) {
        if (!baseUri.getScheme().equals(endpointUri.getScheme())
            || !baseUri.getAuthority().equals(endpointUri.getAuthority())) {
            return false;
        }

        URI normalizedBase = baseUri.normalize();
        URI normalizedEndpoint = endpointUri.normalize();

        String basePath = normalizedBase.getPath();
        String endpointPath = normalizedEndpoint.getPath();

        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        return endpointPath.startsWith(basePath);
    }

}