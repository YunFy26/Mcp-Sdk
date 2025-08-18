# 元数据
| Interface: JSONRPCMessage | 内部类          |
|---------------------------|--------------|
| JSONRPCRequest            |              |
| JSONRPCResponse           | JSONRPCError |
| JSONRPCNotification       |              |

# 接口定义

| Interface: Request    |                     | Interface: Result           |     | Interface: Notification      | |
|-----------------------|---------------------|-----------------------------|-----|------------------------------|-|
| [InitializationRequest](#initializationrequest) | 初始化Request | [InitializationResponse](#initializationresponse) | 初始化 | [ProgressNotification](#progressnotification) | |
| [CallToolRequest](#calltoolrequest)       |                     | [CallToolResult](#calltoolresult)              |     | [LoggingMessageNotification](#loggingmessagenotification)   | |
|                       |                     | [ListToolsResult](#listtoolsresult)             |     | [ResourcesUpdatedNotification](#resourcesupdatednotification) | |
| [CreateMessageRequest](#createmessagerequest)  |                     | [CreateMessageResult](#createmessageresult)         |
| [ElicitRequest](#elicitrequest)         |                     | [ElicitResult](#elicitresult)                |
| [CompleteRequest](#completerequest)       |                     | [CompleteResult](#completeresult)              |
| [GetPromptRequest](#getpromptrequest)      |                     | [GetPromptResult](#getpromptresult)             |
|                       |                     | [ListPromptsResult](#listpromptsresult)           |
| [ReadResourceRequest](#readresourcerequest)   |                     | [ReadResourceResult](#readresourceresult)          |
| [SubscribeRequest](#subscriberequest)      |                     |                            |
| [UnsubscribeRequest](#unsubscriberequest)    |                     |                            |
| [PaginateRequest](#paginaterequest)       |                     |                            |
|                       |                     | [ListResourcesResult](#listresourcesresult)         |
|                       |                     | [ListResourceTemplatesResult](#listresourcetemplatesresult) |
|                       |                     | [ListRootsResult](#listrootsresult)             |

---

### InitializationRequest
- String protocolVersion : 客户端使用的协议版本
- [ClientCapabilities](#clientcapabilities) clientCapabilities  : 客户端能力
- [Implementation](#implementation) clientInfo : 客户端信息
- Map<String, Object> meta : 元数据

    ### ClientCapabilities
    - Map<String, Object> experimental
    - [RootCapabilities](#rootcapabilities) roots 根访问能力
    - [Sampling](#sampling) sampling 采样能力
    - [Elicitation](#elicitation) elicitation 引导能力
      ### RootCapabilities
      ### Sampling
      ### Elicitation

    ### Implementation
    - String name : MCP实现的名称
    - String title : 为UI设计的标题（可选）
    - String version : MCP实现的版本
---
### InitializationResult
- String protocolVersion MCP版本
- [ServerCapabilities](#servercapabilities) serverCapabilities 服务器能力
- [Implementation](#implementation) serverInfo 服务器信息
- String instructions
- Map<String, Object> meta
    ### ServerCapabilities
    - Map<String, Object> experimental
    - [PromptCapabilities](#promptcapabilities) promptCapabilities 提示词能力
    - [ResourceCapabilities](#resourcecapabilities) resourceCapabilities 资源能力
    - [ToolCapabilities](#toolcapabilities) toolCapabilities 工具能力
    - [CompletionCapabilities](#completioncapabilities) completionCapabilities 自动补全能力
    - [LoggingCapabilities](#loggingcapabilities) loggingCapabilities 日志记录能力
    ### PromptCapabilities
    ### ResourceCapabilities
    ### ToolCapabilities
    ### CompletionCapabilities
    ### LoggingCapabilities
