package org.example.spec;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public final class McpSchema {

    private static final Logger logger = LoggerFactory.getLogger(McpSchema.class);

    private McpSchema() {
    }

    public static final String JSONRPC_VERSION = "2.0";

    public static final String FIRST_PAGE = null;

    // ---------------------------
    // Method Names
    // ---------------------------

    // Lifecycle Methods
    public static final String METHOD_INITIALIZE = "initialize";

    public static final String METHOD_NOTIFICATION_INITIALIZED = "notifications/initialized";

    public static final String METHOD_PING = "ping";

    public static final String METHOD_NOTIFICATION_PROGRESS = "notifications/progress";

    // Tool Methods
    public static final String METHOD_TOOLS_LIST = "tools/list";

    public static final String METHOD_TOOLS_CALL = "tools/call";

    public static final String METHOD_NOTIFICATION_TOOLS_LIST_CHANGED = "notifications/tools/list_changed";

    // Resources Methods
    public static final String METHOD_RESOURCES_LIST = "resources/list";

    public static final String METHOD_RESOURCES_READ = "resources/read";

    public static final String METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED = "notifications/resources/list_changed";

    public static final String METHOD_NOTIFICATION_RESOURCES_UPDATED = "notifications/resources/updated";

    public static final String METHOD_RESOURCES_TEMPLATES_LIST = "resources/templates/list";

    public static final String METHOD_RESOURCES_SUBSCRIBE = "resources/subscribe";

    public static final String METHOD_RESOURCES_UNSUBSCRIBE = "resources/unsubscribe";

    // Prompt Methods
    public static final String METHOD_PROMPT_LIST = "prompts/list";

    public static final String METHOD_PROMPT_GET = "prompts/get";

    public static final String METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED = "notifications/prompts/list_changed";

    public static final String METHOD_COMPLETION_COMPLETE = "completion/complete";

    // Logging Methods
    public static final String METHOD_LOGGING_SET_LEVEL = "logging/setLevel";

    public static final String METHOD_NOTIFICATION_MESSAGE = "notifications/message";

    // Roots Methods
    public static final String METHOD_ROOTS_LIST = "roots/list";

    public static final String METHOD_NOTIFICATION_ROOTS_LIST_CHANGED = "notifications/roots/list_changed";

    // Sampling Methods
    public static final String METHOD_SAMPLING_CREATE_MESSAGE = "sampling/createMessage";

    // Elicitation Methods
    public static final String METHOD_ELICITATION_CREATE = "elicitation/create";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final class ErrorCodes {

        /**
         * 解析JSON格式错误-服务器收到了无效的JSON格式的请求
         */
        public static final int PARSE_ERROR = -32700;

        /**
         * 无效的请求-请求格式不符合JSON-RPC规范
         */
        public static final int INVALID_REQUEST = -32600;

        /**
         * 方法未找到-请求中指定的方法不存在或不可用
         */
        public static final int METHOD_NOT_FOUND = -32601;

        /**
         * 无效的参数-请求中提供的参数不符合方法的要求
         */
        public static final int INVALID_PARAMS = -32602;

        /**
         * 内部 JSON-RPC 错误
         */
        public static final int INTERNAL_ERROR = -32603;
    }

    public sealed interface Request
            permits InitializeRequest, CallToolRequest, CreateMessageRequest, ElicitRequest,
            CompleteRequest, GetPromptRequest, ReadResourceRequest, SubscribeRequest, UnsubscribeRequest, PaginatedRequest {

        Map<String, Object> meta();

        default String progressToken() {
            if (meta() != null && meta().containsKey("progressToken")) {
                return meta().get("progressToken").toString();
            }
            return null;
        }
    }

    public sealed interface Result permits InitializeResult, ListResourcesResult, ListResourceTemplatesResult,
            ReadResourceResult, ListPromptsResult, GetPromptResult, ListToolsResult, CallToolResult,
            CreateMessageResult, ElicitResult, CompleteResult, ListRootsResult {

        Map<String, Object> meta();

    }

    public sealed interface Notification
            permits ProgressNotification, LoggingMessageNotification, ResourcesUpdatedNotification {

        Map<String, Object> meta();

    }

    /**
     * MAP_TYPE_REF 是一个 TypeReference 对象，用于在使用 Jackson 库进行 JSON 数据的序列化或反序列化时，
     * 指定目标类型为 HashMap<String, Object>
     */
    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REF = new TypeReference<>() {
    };

    /**
     * 反序列化 JSON-RPC 消息
     *
     * @param objectMapper Jackson ObjectMapper 实例，用于反序列化
     * @param jsonText JSON 字符串，表示 JSON-RPC 消息
     * @return 反序列化后的 JSONRPCMessage 对象, 可能是{@link JSONRPCRequest}, {@link JSONRPCNotification}, {@link JSONRPCResponse}
     * @throws IOException 如果反序列化发生错误，抛出 IOException
     * @throws IllegalAccessException 如果 JSON 字符串无法转换为任何已知的 JSONRPCMessage 类型，抛出 IllegalArgumentException
     */
    public static JSONRPCMessage deserializeJsonRpcMessage(ObjectMapper objectMapper, String jsonText)
            throws IOException {

        logger.debug("Received JSON message: {}", jsonText);

        var map = objectMapper.readValue(jsonText, MAP_TYPE_REF);

        if (map.containsKey("method") && map.containsKey("id")) {
            return objectMapper.convertValue(map, JSONRPCRequest.class);
        }
        else if (map.containsKey("method") && !map.containsKey("id")) {
            return objectMapper.convertValue(map, JSONRPCNotification.class);
        }
        else if (map.containsKey("result") || map.containsKey("error")) {
            return objectMapper.convertValue(map, JSONRPCResponse.class);
        }

        throw new IllegalArgumentException("Cannot deserialize JSONRPCMessage: " + jsonText);
    }

    // ---------------------------
    // JSON-RPC Message Types
    // ---------------------------
    public sealed interface JSONRPCMessage permits JSONRPCRequest, JSONRPCNotification, JSONRPCResponse {

        String jsonrpc();

    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JSONRPCRequest( // @formatter:off
        @JsonProperty("jsonrpc") String jsonrpc,
        @JsonProperty("method") String method,
        @JsonProperty("id") Object id,
        @JsonProperty("params") Object params) implements JSONRPCMessage { // @formatter:on

        public JSONRPCRequest {
            Assert.notNull(id, "MCP requests MUST include an ID - null IDs are not allowed");
            Assert.isTrue(id instanceof String || id instanceof Integer || id instanceof Long,
                    "MCP requests MUST have an ID that is either a string or integer");
        }
    }

    /**
     *
     * @param jsonrpc
     * @param method
     * @param params
     */
    public record JSONRPCNotification( // @formatter:off
        @JsonProperty("jsonrpc") String jsonrpc,
        @JsonProperty("method") String method,
        @JsonProperty("params") Object params) implements JSONRPCMessage { // @formatter:on
    }

    public record JSONRPCResponse( // @formatter:off
        @JsonProperty("jsonrpc") String jsonrpc,
        @JsonProperty("id") Object id,
        @JsonProperty("result") Object result,
        @JsonProperty("error") JSONRPCError error) implements JSONRPCMessage { // @formatter:on

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record JSONRPCError( // @formatter:off
            @JsonProperty("code") int code,
            @JsonProperty("message") String message,
            @JsonProperty("data") Object data) { // @formatter:on
        }

    }

    // ---------------------------
    // Initialization
    // ---------------------------

    @JsonInclude(JsonInclude.Include.NON_ABSENT) // 仅在字段值不为null或Optional.empty时才会将该字段包含在生成的JSON中
    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中存在但Java类中未定义的字段
    public record InitializeRequest( // @formatter:off
        @JsonProperty("protocolVersion") String protocolVersion,
        @JsonProperty("capabilities") ClientCapabilities capabilities,
        @JsonProperty("clientInfo") Implementation clientInfo,
        @JsonProperty("_meta") Map<String, Object> meta) implements Request { // @formatter:on

        public InitializeRequest(String protocolVersion, ClientCapabilities capabilities, Implementation clientInfo) {
            this(protocolVersion, capabilities, clientInfo, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InitializeResult( // @formatter:off
        @JsonProperty("protocolVersion") String protocolVersion,
        @JsonProperty("capabilities") ServerCapabilities capabilities,
        @JsonProperty("serverInfo") Implementation serverInfo,
        @JsonProperty("instructions") String instructions,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result { // @formatter:on

        public InitializeResult(String protocolVersion, ServerCapabilities capabilities, Implementation serverInfo,
                                String instructions) {
            this(protocolVersion, capabilities, serverInfo, instructions, null);
        }
    }

    /**
     * 客户端可能支持的能力
     *
     * @param experimental 客户端支持的非标准实验性能力
     * @param roots 如果存在，则表示客户端支持列出根节点
     * @param sampling 如果存在，则表示客户端支持采样
     * @param elicitation 如果存在，则表示客户端支持向客户端发起征询请求
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClientCapabilities( // @formatter:off
        @JsonProperty("experimental") Map<String, Object> experimental,
        @JsonProperty("roots") RootCapabilities roots,
        @JsonProperty("sampling") Sampling sampling,
        @JsonProperty("elicitation") Elicitation elicitation) { // @formatter:on

        /**
         *
         * @param listChanged
         */
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record RootCapabilities(@JsonProperty("listChanged") Boolean listChanged) {
        }

        /**
         * 采样协议
         * 让服务端能通过客户端安全地调用大语言模型，无需直接管理API密钥
         * // TODO：时序图
         * sequenceDiagram
         *     服务端->>客户端: 发送Sampling请求（含提示词）
         *     客户端->>LLM: 转发请求（附加权限/模型选择）
         *     LLM->>客户端: 返回生成结果
         *     客户端->>服务端: 返回采样结果
         */
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public record Sampling() {
        }

        /**
         * 动态征询协议
         * 让服务端能通过客户端动态收集用户输入
         * // TODO：时序图
         * sequenceDiagram
         *     服务端->>客户端: 发送Elicitation请求（含数据格式要求）
         *     客户端->>用户: 展示输入表单/对话框
         *     用户->>客户端: 提交结构化数据
         *     客户端->>服务端: 返回验证后的用户输入
         */
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public record Elicitation() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private Map<String, Object> experimental = new HashMap<>();

            private RootCapabilities roots;

            private Sampling sampling;

            private Elicitation elicitation;

            public Builder experimental(Map<String, Object> experimental) {
                this.experimental = experimental;
                return this;
            }

            public Builder roots(Boolean listChanged) {
                this.roots = new RootCapabilities(listChanged);
                return this;
            }

            public Builder sampling() {
                this.sampling = new Sampling();
                return this;
            }

            public Builder elicitation() {
                this.elicitation = new Elicitation();
                return this;
            }

            public ClientCapabilities build() {
                return new ClientCapabilities(experimental, roots, sampling, elicitation);
            }
        }
    }

    /**
     * 服务器可能支持的能力
     *
     * @param completions  如果存在，则表示服务器支持参数自动补全
     * @param experimental 服务端支持的非标准实验性能力
     * @param logging      如果存在，则表示服务器支持向客户端发送日志
     * @param prompts      如果存在，则表示服务器提供提示词模板
     * @param resources    如果存在，则表示服务器提供可读取的资源
     * @param tools        如果存在，则表示服务器提供可调用的工具
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ServerCapabilities( // @formatter:off
        @JsonProperty("completions") CompletionCapabilities completions,
        @JsonProperty("experimental") Map<String, Object> experimental,
        @JsonProperty("logging") LoggingCapabilities logging,
        @JsonProperty("prompts") PromptCapabilities prompts,
        @JsonProperty("resources") ResourceCapabilities resources,
        @JsonProperty("tools") ToolCapabilities tools) { // @formatter:on)

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public record CompletionCapabilities() {
        }

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public record LoggingCapabilities() {
        }

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public record PromptCapabilities(@JsonProperty("listChanged") Boolean listChanged) {
        }

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public record ResourceCapabilities(@JsonProperty("subscribe") Boolean subscribe,
                                           @JsonProperty("listChanged") Boolean listChanged) {
        }

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public record ToolCapabilities(@JsonProperty("listChanged") Boolean listChanged) {
        }

        public Builder mutate() {
            var builder = new Builder();
            builder.completions = this.completions;
            builder.experimental = this.experimental;
            builder.logging = this.logging;
            builder.prompts = this.prompts;
            builder.resources = this.resources;
            builder.tools = this.tools;
            return builder;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private CompletionCapabilities completions;

            private Map<String, Object> experimental;

            private LoggingCapabilities logging;

            private PromptCapabilities prompts;

            private ResourceCapabilities resources;

            private ToolCapabilities tools;

            public Builder completions() {
                this.completions = new CompletionCapabilities();
                return this;
            }

            public Builder experimental(Map<String, Object> experimental) {
                this.experimental = experimental;
                return this;
            }

            public Builder logging() {
                this.logging = new LoggingCapabilities();
                return this;
            }

            public Builder prompts(Boolean listChanged) {
                this.prompts = new PromptCapabilities(listChanged);
                return this;
            }

            public Builder resources(Boolean subscribe, Boolean listChanged) {
                this.resources = new ResourceCapabilities(subscribe, listChanged);
                return this;
            }

            public Builder tools(Boolean listChanged) {
                this.tools = new ToolCapabilities(listChanged);
                return this;
            }

            public ServerCapabilities build() {
                return new ServerCapabilities(completions, experimental, logging, prompts, resources, tools);
            }

    }
    }

    /**
     * 描述MCP的名称和版本信息，title可用于UI展示
     *
     * @param name 设计用于编程或逻辑场景，但在历史规范中或作为回退方案（当title不存在时）也用作显示名称
     * @param title 专为UI界面和终端用户场景设计
     * @param version 版本号
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Implementation( // @formatter:off
        @JsonProperty("name") String name,
        @JsonProperty("title") String title,
        @JsonProperty("version") String version) implements BaseMetadata { // @formatter:on

        public Implementation(String name, String version) {
            this(name, null, version);
        }
    }

    public enum Role {
        @JsonProperty("user") USER,
        @JsonProperty("assistant") ASSISTANT;
    }

    /**
     *
     */
    public interface Annotated {

        Annotations annotations();

    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Annotations( // @formatter:off
        @JsonProperty("audience") List<Role> audience,
        @JsonProperty("priority") Double priority) { // @formatter:on
    }

    /**
     * 资源内容接口，资源内容包括原数据和uri...
     */
    public interface ResourceContent extends BaseMetadata {

        String uri();

        String description();

        String mimeType();

        Long size();

        Annotations annotations();

    }

    /**
     *
     */
    public interface BaseMetadata {

        /**
         * 设计用于编程或逻辑场景，但在历史规范中或作为回退方案
         * （当title不存在时）也用作显示名称
         */
        String name();

        /**
         * 专为UI界面和终端用户场景设计——经过优化确保人类可读
         * 且易于理解，即使不熟悉领域术语的用户也能理解
         *
         * <p>如未提供，则应使用name作为显示内容</p>
         */
        String title();

    }

    /**
     * 服务器提供的可读资源
     *
     * @param uri
     * @param name
     * @param title
     * @param description
     * @param mimeType
     * @param size
     * @param annotations
     * @param meta
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resource( // @formatter:off
        @JsonProperty("uri") String uri,
        @JsonProperty("name") String name,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("mimeType") String mimeType,
        @JsonProperty("size") Long size,
        @JsonProperty("annotations") Annotations annotations,
        @JsonProperty("_meta") Map<String, Object> meta) implements Annotated, ResourceContent{ // @formatter:on

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private String name;

            private String title;

            private String uri;

            private String description;

            private String mimeType;

            private Annotations annotations;

            private Long size;

            private Map<String, Object> meta;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder title(String title) {
                this.title = title;
                return this;
            }

            public Builder uri(String uri) {
                this.uri = uri;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder mimeType(String mimeType) {
                this.mimeType = mimeType;
                return this;
            }

            public Builder annotations(Annotations annotations) {
                this.annotations = annotations;
                return this;
            }

            public Builder size(Long size) {
                this.size = size;
                return this;
            }

            public Builder meta(Map<String, Object> meta) {
                this.meta = meta;
                return this;
            }

            public ResourceLink build() {
                Assert.hasText(uri, "uri must not be empty");
                Assert.hasText(name, "name must not be empty");

                return new ResourceLink(uri, name, title, description, mimeType, size, annotations, meta);
            }

        }
    }

    /**
     * 资源模板允许服务端通过URI暴露参数化资源
     *
     * @param uriTemplate
     * @param name
     * @param title
     * @param description
     * @param mimeType
     * @param annotations
     * @param meta
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceTemplate( // @formatter:off
        @JsonProperty("uriTemplate") String uriTemplate,
        @JsonProperty("name") String name,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("mimeType") String mimeType,
        @JsonProperty("annotations") Annotations annotations,
        @JsonProperty("_meta") Map<String, Object> meta) implements Annotated, BaseMetadata { // @formatter:on

        public ResourceTemplate(String uriTemplate, String name, String title, String description, String mimeType,
                                Annotations annotations) {
            this(uriTemplate, name, title, description, mimeType, annotations, null);
        }

        public ResourceTemplate(String uriTemplate, String name, String description, String mimeType,
                                Annotations annotations) {
            this(uriTemplate, name, null, description, mimeType, annotations);
        }
    }

    /**
     *
     * @param resources
     * @param nextCursor
     * @param meta
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListResourcesResult( // @formatter:off
        @JsonProperty("resources") List<Resource> resources,
        @JsonProperty("nextCursor") String nextCursor,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result { // @formatter:on

        public ListResourcesResult(List<Resource> resources, String nextCursor) {
            this(resources, nextCursor, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListResourceTemplatesResult( // @formatter:off
        @JsonProperty("resourceTemplates") List<ResourceTemplate> resourceTemplates,
        @JsonProperty("nextCursor") String nextCursor,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result { // @formatter:on

        public ListResourceTemplatesResult(List<ResourceTemplate> resourceTemplates, String nextCursor) {
            this(resourceTemplates, nextCursor, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReadResourceRequest( // @formatter:off
        @JsonProperty("uri") String uri,
        @JsonProperty("_meta") Map<String, Object> meta) implements Request { // @formatter:on

        public ReadResourceRequest(String uri) {
            this(uri, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReadResourceResult( // @formatter:off
        @JsonProperty("contents") List<ResourceContents> contents,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result { // @formatter:on

        public ReadResourceResult(List<ResourceContents> contents) {
            this(contents, null);
        }
    }

    /**
     * 由客户端发出，用于订阅特定资源变更时接收服务器推送的资源更新通知
     *
     * @param uri 要订阅的资源URI，可使用任意协议
     * @param meta
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubscribeRequest( // @formatter:off
        @JsonProperty("uri") String uri,
        @JsonProperty("_meta") Map<String, Object> meta) implements Request { // @formatter:on

        public SubscribeRequest(String uri) {
            this(uri, null);
        }
    }

    /**
     * 由客户端发出，用于取消之前通过resources/subscribe请求注册的资源变更通知订阅
     *
     * @param uri
     * @param meta
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UnsubscribeRequest( // @formatter:off
        @JsonProperty("uri") String uri,
        @JsonProperty("_meta") Map<String, Object> meta) implements Request { // @formatter:on

        public UnsubscribeRequest(String uri) {
            this(uri, null);
        }
    }


    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, include = JsonTypeInfo.As.PROPERTY)
    @JsonSubTypes({@JsonSubTypes.Type(value = TextResourceContents.class, name = "text"),
            @JsonSubTypes.Type(value = BlobResourceContents.class, name = "blob") })
    public sealed interface ResourceContents permits TextResourceContents, BlobResourceContents {

        /**
         * 资源内容的URI
         * @return URI
         */
        String uri();

        /**
         * 资源内容的MIME类型
         * @return MIME类型
         */
        String mimeType();

        Map<String, Object> meta();
    }

    /**
     * 文本资源内容
     *
     * @param uri 资源的URI标识
     * @param mimeType 资源MIME类型
     * @param text 资源文本内容（仅当资源可表示为文本而非二进制数据时设置）
     * @param meta
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TextResourceContents( // @formatter:off
        @JsonProperty("uri") String uri,
        @JsonProperty("mimeType") String mimeType,
        @JsonProperty("text") String text,
        @JsonProperty("_meta") Map<String, Object> meta) implements ResourceContents { // @formatter:on

        public TextResourceContents(String uri, String mimeType, String text) {
            this(uri, mimeType, text, null);
        }
    }

    /**
     * 二进制资源内容
     * @param uri 资源的URI标识
     * @param mimeType 资源MIME类型
     * @param blob 表示资源二进制数据的Base64编码字符串（仅当资源可表示为二进制数据而非文本时设置）
     * @param meta
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BlobResourceContents( // @formatter:off
        @JsonProperty("uri") String uri,
        @JsonProperty("mimeType") String mimeType,
        @JsonProperty("blob") String blob,
        @JsonProperty("_meta") Map<String, Object> meta) implements ResourceContents { // @formatter:on

        public BlobResourceContents(String uri, String mimeType, String blob) {
            this(uri, mimeType, blob, null);
        }
    }

    // ---------------------------
    // Prompt Interfaces
    // ---------------------------

    /**
     *
     * @param name
     * @param title
     * @param description
     * @param arguments
     * @param meta
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Prompt( // @formatter:off
        @JsonProperty("name") String name,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("arguments") List<PromptArgument> arguments,
        @JsonProperty("_meta") Map<String, Object> meta) implements BaseMetadata { // @formatter:on

        public Prompt(String name, String description, List<PromptArgument> arguments) {
            this(name, null, description, arguments != null ? arguments : new ArrayList<>());
        }

        public Prompt(String name, String title, String description, List<PromptArgument> arguments) {
            this(name, title, description, arguments != null ? arguments : new ArrayList<>(), null);
        }
    }

    /**
     * prompt可接受的参数定义
     *
     * @param name 参数名称
     * @param title 参数标题，专为UI界面和终端用户场景设计——经过优化确保人类可读
     * @param description 参数的人类可读描述
     * @param required 标识该参数是否必须提供
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PromptArgument( // @formatter:off
        @JsonProperty("name") String name,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("required") Boolean required) implements BaseMetadata { // @formatter:on

        public PromptArgument(String name, String description, Boolean required) {
            this(name, null, description, required);
        }
    }

    /**
     *
     * @param role
     * @param content
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PromptMessage( // @formatter:off
        @JsonProperty("role") Role role,
        @JsonProperty("content") Content content) { // @formatter:on
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListPromptsResult( // @formatter:off
        @JsonProperty("prompts") List<Prompt> prompts,
        @JsonProperty("nextCursor") String nextCursor,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result  { // @formatter:on

        public ListPromptsResult(List<Prompt> prompts, String nextCursor) {
            this(prompts, nextCursor, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GetPromptRequest( // @formatter:off
        @JsonProperty("name") String name,
        @JsonProperty("arguments") Map<String, Object> arguments,
        @JsonProperty("_meta") Map<String, Object> meta) implements Request { // @formatter:on

        public GetPromptRequest(String name, Map<String, Object> arguments) {
            this(name, arguments, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GetPromptResult( // @formatter:off
        @JsonProperty("description") String description,
        @JsonProperty("messages") List<PromptMessage> messages,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result { // @formatter:on

        public GetPromptResult(String description, List<PromptMessage> messages) {
            this(description, messages, null);
        }
    }

    // ---------------------------
    // Tool Interfaces
    // ---------------------------
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListToolsResult( // @formatter:off
        @JsonProperty("tools") List<Tool> tools,
        @JsonProperty("nextCursor") String nextCursor,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result { // @formatter:on

        public ListToolsResult(List<Tool> tools, String nextCursor) {
            this(tools, nextCursor, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JsonSchema( // @formatter:off
        @JsonProperty("type") String type,
        @JsonProperty("properties") Map<String, Object> properties,
        @JsonProperty("required") List<String> required,
        @JsonProperty("additionalProperties") Boolean additionalProperties,
        @JsonProperty("$defs") Map<String, Object> defs,
        @JsonProperty("definitions") Map<String, Object> definitions) { // @formatter:on
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ToolAnnotations( // @formatter:off
        @JsonProperty("title")  String title,
        @JsonProperty("readOnlyHint")   Boolean readOnlyHint,
        @JsonProperty("destructiveHint") Boolean destructiveHint,
        @JsonProperty("idempotentHint") Boolean idempotentHint,
        @JsonProperty("openWorldHint") Boolean openWorldHint,
        @JsonProperty("returnDirect") Boolean returnDirect) { // @formatter:on
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Tool( // @formatter:off
        @JsonProperty("name") String name,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("inputSchema") JsonSchema inputSchema,
        @JsonProperty("outputSchema") Map<String, Object> outputSchema,
        @JsonProperty("annotations") ToolAnnotations annotations,
        @JsonProperty("_meta") Map<String, Object> meta) { // @formatter:on

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private String name;

            private String title;

            private String description;

            private JsonSchema inputSchema;

            private Map<String, Object> outputSchema;

            private ToolAnnotations annotations;

            private Map<String, Object> meta;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder title(String title) {
                this.title = title;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder inputSchema(JsonSchema inputSchema) {
                this.inputSchema = inputSchema;
                return this;
            }

            public Builder inputSchema(String inputSchema) {
                this.inputSchema = parseSchema(inputSchema);
                return this;
            }

            public Builder outputSchema(Map<String, Object> outputSchema) {
                this.outputSchema = outputSchema;
                return this;
            }

            public Builder outputSchema(String outputSchema) {
                this.outputSchema = schemaToMap(outputSchema);
                return this;
            }

            public Builder annotations(ToolAnnotations annotations) {
                this.annotations = annotations;
                return this;
            }

            public Builder meta(Map<String, Object> meta) {
                this.meta = meta;
                return this;
            }

            public Tool build() {
                Assert.hasText(name, "name must not be empty");
                return new Tool(name, title, description, inputSchema, outputSchema, annotations, meta);
            }
        }
    }

    private static Map<String, Object> schemaToMap(String schema) {
        try {
            return OBJECT_MAPPER.readValue(schema, MAP_TYPE_REF);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Invalid schema: " + schema, e);
        }
    }

    private static JsonSchema parseSchema(String schema) {
        try {
            return OBJECT_MAPPER.readValue(schema, JsonSchema.class);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Invalid schema: " + schema, e);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CallToolRequest( // @formatter:off
        @JsonProperty("name") String name,
        @JsonProperty("arguments") Map<String, Object> arguments,
        @JsonProperty("_meta") Map<String, Object> meta) implements Request { // @formatter:on

        public CallToolRequest(String name, String jsonArguments) {
            this(name, parseJsonArguments(jsonArguments), null);
        }

        public CallToolRequest(String name, Map<String, Object> arguments) {
            this(name, arguments, null);
        }

        private static Map<String, Object> parseJsonArguments(String jsonArguments) {
            try {
                return OBJECT_MAPPER.readValue(jsonArguments, MAP_TYPE_REF);
            }
            catch (IOException e) {
                throw new IllegalArgumentException("Invalid arguments: " + jsonArguments, e);
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private String name;

            private Map<String, Object> arguments;

            private Map<String, Object> meta;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder arguments(Map<String, Object> arguments) {
                this.arguments = arguments;
                return this;
            }

            public Builder arguments(String jsonArguments) {
                this.arguments = parseJsonArguments(jsonArguments);
                return this;
            }

            public Builder meta(Map<String, Object> meta) {
                this.meta = meta;
                return this;
            }

            public Builder progressToken(String progressToken) {
                if (this.meta == null) {
                    this.meta = new HashMap<>();
                }
                this.meta.put("progressToken", progressToken);
                return this;
            }

            public CallToolRequest build() {
                Assert.hasText(name, "name must not be empty");
                return new CallToolRequest(name, arguments, meta);
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CallToolResult( // @formatter:off
        @JsonProperty("content") List<Content> content,
        @JsonProperty("isError") Boolean isError,
        @JsonProperty("structuredContent") Map<String, Object> structuredContent,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result { // @formatter:on

        // backwards compatibility constructor
        public CallToolResult(List<Content> content, Boolean isError) {
            this(content, isError, null, null);
        }

        // backwards compatibility constructor
        public CallToolResult(List<Content> content, Boolean isError, Map<String, Object> structuredContent) {
            this(content, isError, structuredContent, null);
        }

        public CallToolResult(String content, Boolean isError) {
            this(List.of(new TextContent(content)), isError, null);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private List<Content> content = new ArrayList<>();

            private Boolean isError = false;

            private Map<String, Object> structuredContent;

            private Map<String, Object> meta;

            public Builder content(List<Content> content) {
                Assert.notNull(content, "content must not be null");
                this.content = content;
                return this;
            }

            public Builder structuredContent(Map<String, Object> structuredContent) {
                Assert.notNull(structuredContent, "structuredContent must not be null");
                this.structuredContent = structuredContent;
                return this;
            }

            public Builder structuredContent(String structuredContent) {
                Assert.hasText(structuredContent, "structuredContent must not be empty");
                try {
                    this.structuredContent = OBJECT_MAPPER.readValue(structuredContent, MAP_TYPE_REF);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Invalid structured content: " + structuredContent, e);
                }
                return this;
            }

            public Builder textContent(List<String> textContent) {
                Assert.notNull(textContent, "textContent must not be null");
                textContent.stream().map(TextContent::new).forEach(this.content::add);
                return this;
            }

            public Builder addContent(Content contentItem) {
                Assert.notNull(contentItem, "contentItem must not be null");
                if (this.content == null) {
                    this.content = new ArrayList<>();
                }
                this.content.add(contentItem);
                return this;
            }

            public Builder addTextContent(String text) {
                Assert.notNull(text, "text must not be null");
                return addContent(new TextContent(text));
            }

            public Builder isError(Boolean isError) {
                Assert.notNull(isError, "isError must not be null");
                this.isError = isError;
                return this;
            }

            public Builder meta(Map<String, Object> meta) {
                this.meta = meta;
                return this;
            }

            public CallToolResult build() {
                return new CallToolResult(content, isError, structuredContent, meta);
            }
        }
    }

    // ---------------------------
    // Sampling Interfaces
    // ---------------------------
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModelPreferences( // @formatter:off
        @JsonProperty("hints") List<ModelHint> hints,
        @JsonProperty("costPriority") Double costPriority,
        @JsonProperty("speedPriority") Double speedPriority,
        @JsonProperty("intelligencePriority") Double intelligencePriority) { // @formatter:on

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private List<ModelHint> hints;

            private Double costPriority;

            private Double speedPriority;

            private Double intelligencePriority;

            public Builder hints(List<ModelHint> hints) {
                this.hints = hints;
                return this;
            }

            public Builder addHint(String name) {
                if (this.hints == null) {
                    this.hints = new ArrayList<>();
                }
                this.hints.add(new ModelHint(name));
                return this;
            }

            public Builder costPriority(Double costPriority) {
                this.costPriority = costPriority;
                return this;
            }

            public Builder speedPriority(Double speedPriority) {
                this.speedPriority = speedPriority;
                return this;
            }

            public Builder intelligencePriority(Double intelligencePriority) {
                this.intelligencePriority = intelligencePriority;
                return this;
            }

            public ModelPreferences build() {
                return new ModelPreferences(hints, costPriority, speedPriority, intelligencePriority);
            }

        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModelHint(@JsonProperty("name") String name) {
        public static ModelHint of(String name) {
            return new ModelHint(name);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SamplingMessage( // @formatter:off
        @JsonProperty("role") Role role,
        @JsonProperty("content") Content content) { // @formatter:on
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateMessageRequest( // @formatter:off
        @JsonProperty("messages") List<SamplingMessage> messages,
        @JsonProperty("modelPreferences") ModelPreferences modelPreferences,
        @JsonProperty("systemPrompt") String systemPrompt,
        @JsonProperty("includeContext") ContextInclusionStrategy includeContext,
        @JsonProperty("temperature") Double temperature,
        @JsonProperty("maxTokens") int maxTokens,
        @JsonProperty("stopSequences") List<String> stopSequences,
        @JsonProperty("metadata") Map<String, Object> metadata,
        @JsonProperty("_meta") Map<String, Object> meta) implements Request { // @formatter:on

        // backwards compatibility constructor
        public CreateMessageRequest(List<SamplingMessage> messages, ModelPreferences modelPreferences,
                                    String systemPrompt, ContextInclusionStrategy includeContext, Double temperature, int maxTokens,
                                    List<String> stopSequences, Map<String, Object> metadata) {
            this(messages, modelPreferences, systemPrompt, includeContext, temperature, maxTokens, stopSequences,
                    metadata, null);
        }

        public enum ContextInclusionStrategy { // @formatter:off
            @JsonProperty("none") NONE,
            @JsonProperty("thisServer") THIS_SERVER,
            @JsonProperty("allServers")ALL_SERVERS
        } // @formatter:on

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private List<SamplingMessage> messages;

            private ModelPreferences modelPreferences;

            private String systemPrompt;

            private ContextInclusionStrategy includeContext;

            private Double temperature;

            private int maxTokens;

            private List<String> stopSequences;

            private Map<String, Object> metadata;

            private Map<String, Object> meta;

            public Builder messages(List<SamplingMessage> messages) {
                this.messages = messages;
                return this;
            }

            public Builder modelPreferences(ModelPreferences modelPreferences) {
                this.modelPreferences = modelPreferences;
                return this;
            }

            public Builder systemPrompt(String systemPrompt) {
                this.systemPrompt = systemPrompt;
                return this;
            }

            public Builder includeContext(ContextInclusionStrategy includeContext) {
                this.includeContext = includeContext;
                return this;
            }

            public Builder temperature(Double temperature) {
                this.temperature = temperature;
                return this;
            }

            public Builder maxTokens(int maxTokens) {
                this.maxTokens = maxTokens;
                return this;
            }

            public Builder stopSequences(List<String> stopSequences) {
                this.stopSequences = stopSequences;
                return this;
            }

            public Builder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }

            public Builder meta(Map<String, Object> meta) {
                this.meta = meta;
                return this;
            }

            public Builder progressToken(String progressToken) {
                if (this.meta == null) {
                    this.meta = new HashMap<>();
                }
                this.meta.put("progressToken", progressToken);
                return this;
            }

            public CreateMessageRequest build() {
                return new CreateMessageRequest(messages, modelPreferences, systemPrompt, includeContext, temperature,
                        maxTokens, stopSequences, metadata, meta);
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateMessageResult( // @formatter:off
        @JsonProperty("role") Role role,
        @JsonProperty("content") Content content,
        @JsonProperty("model") String model,
        @JsonProperty("stopReason") StopReason stopReason,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result { // @formatter:on

        public enum StopReason { // @formatter:off
            @JsonProperty("endTurn") END_TURN("endTurn"),
            @JsonProperty("stopSequence") STOP_SEQUENCE("stopSequence"),
            @JsonProperty("maxTokens") MAX_TOKENS("maxTokens"),
            @JsonProperty("unknown") UNKNOWN("unknown");
            // @formatter:on

            private final String value;

            StopReason(String value) {
                this.value = value;
            }

            @JsonCreator
            private static StopReason of(String value) {
                return Arrays.stream(StopReason.values())
                    .filter(stopReason -> stopReason.value.equals(value))
                    .findFirst()
                    .orElse(StopReason.UNKNOWN);
            }

        }

        public CreateMessageResult(Role role, Content content, String model, StopReason stopReason) {
            this(role, content, model, stopReason, null);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private Role role = Role.ASSISTANT;

            private Content content;

            private String model;

            private StopReason stopReason = StopReason.END_TURN;

            private Map<String, Object> meta;

            public Builder role(Role role) {
                this.role = role;
                return this;
            }

            public Builder content(Content content) {
                this.content = content;
                return this;
            }

            public Builder model(String model) {
                this.model = model;
                return this;
            }

            public Builder stopReason(StopReason stopReason) {
                this.stopReason = stopReason;
                return this;
            }

            public Builder message(String message) {
                this.content = new TextContent(message);
                return this;
            }

            public Builder meta(Map<String, Object> meta) {
                this.meta = meta;
                return this;
            }

            public CreateMessageResult build() {
                return new CreateMessageResult(role, content, model, stopReason, meta);
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ElicitRequest( // @formatter:off
        @JsonProperty("message") String message,
        @JsonProperty("requestedSchema") Map<String, Object> requestedSchema,
        @JsonProperty("_meta") Map<String, Object> meta) implements Request { // @formatter:on

        // backwards compatibility constructor
        public ElicitRequest(String message, Map<String, Object> requestedSchema) {
            this(message, requestedSchema, null);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private String message;

            private Map<String, Object> requestedSchema;

            private Map<String, Object> meta;

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder requestedSchema(Map<String, Object> requestedSchema) {
                this.requestedSchema = requestedSchema;
                return this;
            }

            public Builder meta(Map<String, Object> meta) {
                this.meta = meta;
                return this;
            }

            public Builder progressToken(String progressToken) {
                if (this.meta == null) {
                    this.meta = new HashMap<>();
                }
                this.meta.put("progressToken", progressToken);
                return this;
            }

            public ElicitRequest build() {
                return new ElicitRequest(message, requestedSchema, meta);
            }

        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ElicitResult( // @formatter:off
        @JsonProperty("action") Action action,
        @JsonProperty("content") Map<String, Object> content,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result { // @formatter:on

        public enum Action {
            // @formatter:off
            @JsonProperty("accept") ACCEPT,
            @JsonProperty("decline") DECLINE,
            @JsonProperty("cancel") CANCEL
        } // @formatter:on

        // backwards compatibility constructor
        public ElicitResult(Action action, Map<String, Object> content) {
            this(action, content, null);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private Action action;

            private Map<String, Object> content;

            private Map<String, Object> meta;

            public Builder message(Action action) {
                this.action = action;
                return this;
            }

            public Builder content(Map<String, Object> content) {
                this.content = content;
                return this;
            }

            public Builder meta(Map<String, Object> meta) {
                this.meta = meta;
                return this;
            }

            public ElicitResult build() {
                return new ElicitResult(action, content, meta);
            }

        }
    }

    // ---------------------------
    // Pagination Interfaces
    // ---------------------------
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaginatedRequest( // @formatter:off
        @JsonProperty("cursor") String cursor,
        @JsonProperty("_meta") Map<String, Object> meta) implements Request { // @formatter:on

        public PaginatedRequest(String cursor) {
            this(cursor, null);
        }

        public PaginatedRequest() {
            this(null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaginatedResult(@JsonProperty("nextCursor") String nextCursor) {
    }

    // ---------------------------
    // Progress and Logging
    // ---------------------------
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProgressNotification( // @formatter:off
        @JsonProperty("progressToken") String progressToken,
        @JsonProperty("progress") Double progress,
        @JsonProperty("total") Double total,
        @JsonProperty("message") String message,
        @JsonProperty("_meta") Map<String, Object> meta) implements Notification { // @formatter:on

        public ProgressNotification(String progressToken, double progress, Double total, String message) {
            this(progressToken, progress, total, message, null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourcesUpdatedNotification(// @formatter:off
        @JsonProperty("uri") String uri,
        @JsonProperty("_meta") Map<String, Object> meta) implements Notification { // @formatter:on

        public ResourcesUpdatedNotification(String uri) {
            this(uri, null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LoggingMessageNotification( // @formatter:off
        @JsonProperty("level") LoggingLevel level,
        @JsonProperty("logger") String logger,
        @JsonProperty("data") String data,
        @JsonProperty("_meta") Map<String, Object> meta) implements Notification { // @formatter:on

        // backwards compatibility constructor
        public LoggingMessageNotification(LoggingLevel level, String logger, String data) {
            this(level, logger, data, null);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private LoggingLevel level = LoggingLevel.INFO;

            private String logger = "server";

            private String data;

            private Map<String, Object> meta;

            public Builder level(LoggingLevel level) {
                this.level = level;
                return this;
            }

            public Builder logger(String logger) {
                this.logger = logger;
                return this;
            }

            public Builder data(String data) {
                this.data = data;
                return this;
            }

            public Builder meta(Map<String, Object> meta) {
                this.meta = meta;
                return this;
            }

            public LoggingMessageNotification build() {
                return new LoggingMessageNotification(level, logger, data, meta);
            }

        }
    }

    public enum LoggingLevel { // @formatter:off
        @JsonProperty("debug") DEBUG(0),
        @JsonProperty("info") INFO(1),
        @JsonProperty("notice") NOTICE(2),
        @JsonProperty("warning") WARNING(3),
        @JsonProperty("error") ERROR(4),
        @JsonProperty("critical") CRITICAL(5),
        @JsonProperty("alert") ALERT(6),
        @JsonProperty("emergency") EMERGENCY(7);
        // @formatter:on

        private final int level;

        LoggingLevel(int level) {
            this.level = level;
        }

        public int level() {
            return level;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SetLevelRequest(@JsonProperty("level") LoggingLevel level) {
    }

    // ---------------------------
    // Autocomplete
    // ---------------------------
    public sealed interface CompleteReference permits PromptReference, ResourceReference {

        String type();

        String identifier();

    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PromptReference( // @formatter:off
        @JsonProperty("type") String type,
        @JsonProperty("name") String name,
        @JsonProperty("title") String title ) implements McpSchema.CompleteReference, BaseMetadata { // @formatter:on

        public PromptReference(String type, String name) {
            this(type, name, null);
        }

        public PromptReference(String name) {
            this("ref/prompt", name, null);
        }

        @Override
        public String identifier() {
            return name();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceReference( // @formatter:off
        @JsonProperty("type") String type,
        @JsonProperty("uri") String uri) implements McpSchema.CompleteReference { // @formatter:on

        public ResourceReference(String uri) {
            this("ref/resource", uri);
        }

        @Override
        public String identifier() {
            return uri();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompleteRequest( // @formatter:off
        @JsonProperty("ref") McpSchema.CompleteReference ref,
        @JsonProperty("argument") CompleteArgument argument,
        @JsonProperty("_meta") Map<String, Object> meta,
        @JsonProperty("context") CompleteContext context) implements Request { // @formatter:on

        public CompleteRequest(McpSchema.CompleteReference ref, CompleteArgument argument, Map<String, Object> meta) {
            this(ref, argument, meta, null);
        }

        public CompleteRequest(McpSchema.CompleteReference ref, CompleteArgument argument, CompleteContext context) {
            this(ref, argument, null, context);
        }

        public CompleteRequest(McpSchema.CompleteReference ref, CompleteArgument argument) {
            this(ref, argument, null, null);
        }

        public record CompleteArgument(@JsonProperty("name") String name, @JsonProperty("value") String value) {
        }

        public record CompleteContext(@JsonProperty("arguments") Map<String, String> arguments) {
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompleteResult(@JsonProperty("completion") CompleteCompletion completion,
                                 @JsonProperty("_meta") Map<String, Object> meta) implements Result {

        // backwards compatibility constructor
        public CompleteResult(CompleteCompletion completion) {
            this(completion, null);
        }

        public record CompleteCompletion( // @formatter:off
            @JsonProperty("values") List<String> values,
            @JsonProperty("total") Integer total,
            @JsonProperty("hasMore") Boolean hasMore) { // @formatter:on
        }
    }

    // ---------------------------
    // Content Types
    // ---------------------------
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({ @JsonSubTypes.Type(value = TextContent.class, name = "text"),
        @JsonSubTypes.Type(value = ImageContent.class, name = "image"),
        @JsonSubTypes.Type(value = AudioContent.class, name = "audio"),
        @JsonSubTypes.Type(value = EmbeddedResource.class, name = "resource"),
        @JsonSubTypes.Type(value = ResourceLink.class, name = "resource_link") })
    public sealed interface Content permits TextContent, ImageContent, AudioContent, EmbeddedResource, ResourceLink {

        Map<String, Object> meta();

        default String type() {
            if (this instanceof TextContent) {
                return "text";
            }
            else if (this instanceof ImageContent) {
                return "image";
            }
            else if (this instanceof AudioContent) {
                return "audio";
            }
            else if (this instanceof EmbeddedResource) {
                return "resource";
            }
            else if (this instanceof ResourceLink) {
                return "resource_link";
            }
            throw new IllegalArgumentException("Unknown content type: " + this);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TextContent( // @formatter:off
                               @JsonProperty("annotations") Annotations annotations,
                               @JsonProperty("text") String text,
                               @JsonProperty("_meta") Map<String, Object> meta) implements Annotated, Content { // @formatter:on

        public TextContent(Annotations annotations, String text) {
            this(annotations, text, null);
        }

        public TextContent(String content) {
            this(null, content, null);
        }

    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageContent( // @formatter:off
        @JsonProperty("annotations") Annotations annotations,
        @JsonProperty("data") String data,
        @JsonProperty("mimeType") String mimeType,
        @JsonProperty("_meta") Map<String, Object> meta) implements Annotated, Content { // @formatter:on

        public ImageContent(Annotations annotations, String data, String mimeType) {
            this(annotations, data, mimeType, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AudioContent( // @formatter:off
        @JsonProperty("annotations") Annotations annotations,
        @JsonProperty("data") String data,
        @JsonProperty("mimeType") String mimeType,
        @JsonProperty("_meta") Map<String, Object> meta) implements Annotated, Content { // @formatter:on

        // backwards compatibility constructor
        public AudioContent(Annotations annotations, String data, String mimeType) {
            this(annotations, data, mimeType, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmbeddedResource( // @formatter:off
        @JsonProperty("annotations") Annotations annotations,
        @JsonProperty("resource") ResourceContents resource,
        @JsonProperty("_meta") Map<String, Object> meta) implements Annotated, Content { // @formatter:on

        // backwards compatibility constructor
        public EmbeddedResource(Annotations annotations, ResourceContents resource) {
            this(annotations, resource, null);
        }
    }


    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceLink( // @formatter:off
        @JsonProperty("name") String name,
        @JsonProperty("title") String title,
        @JsonProperty("uri") String uri,
        @JsonProperty("description") String description,
        @JsonProperty("mimeType") String mimeType,
        @JsonProperty("size") Long size,
        @JsonProperty("annotations") Annotations annotations,
        @JsonProperty("_meta") Map<String, Object> meta) implements Annotated, Content, ResourceContent { // @formatter:on

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private String name;

            private String title;

            private String uri;

            private String description;

            private String mimeType;

            private Annotations annotations;

            private Long size;

            private Map<String, Object> meta;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder title(String title) {
                this.title = title;
                return this;
            }

            public Builder uri(String uri) {
                this.uri = uri;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder mimeType(String mimeType) {
                this.mimeType = mimeType;
                return this;
            }

            public Builder annotations(Annotations annotations) {
                this.annotations = annotations;
                return this;
            }

            public Builder size(Long size) {
                this.size = size;
                return this;
            }

            public Builder meta(Map<String, Object> meta) {
                this.meta = meta;
                return this;
            }

            public ResourceLink build() {
                Assert.hasText(uri, "uri must not be empty");
                Assert.hasText(name, "name must not be empty");

                return new ResourceLink(name, title, uri, description, mimeType, size, annotations, meta);
            }

        }
    }

    // ---------------------------
    // Roots
    // ---------------------------
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Root( // @formatter:off
        @JsonProperty("uri") String uri,
        @JsonProperty("name") String name,
        @JsonProperty("_meta") Map<String, Object> meta) { // @formatter:on

        public Root(String uri, String name) {
            this(uri, name, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListRootsResult( // @formatter:off
        @JsonProperty("roots") List<Root> roots,
        @JsonProperty("nextCursor") String nextCursor,
        @JsonProperty("_meta") Map<String, Object> meta) implements Result { // @formatter:on

        public ListRootsResult(List<Root> roots) {
            this(roots, null);
        }

        public ListRootsResult(List<Root> roots, String nextCursor) {
            this(roots, nextCursor, null);
        }
    }
}
