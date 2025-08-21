# MCP Java SDK 学习复现项目

## 项目目的

本仓库用于学习和复现 Model Context Protocol (MCP) Java SDK，旨在通过对官方 Java SDK 的实现复现，深入理解 MCP 协议的核心原理与实现机制。

## 关于 MCP

Model Context Protocol (MCP) 是一种标准化协议，旨在实现语言模型与 AI 工具之间的无缝集成。它定义了一套规范的接口和通信模式，使应用程序能够通过标准化的方式与 AI 模型和工具进行交互。

## 开发日志

### MCP文档总结
| 日期         | 章节                         | 文档状态   | 今日完成内容              |
|------------|----------------------------|--------|---------------------|
| 2025.08.17 | specification              | 🟢 已完成 | 完成Specification重要部分 |
|            | Base Protocol - Transports | 🟢 已完成 | 完成传输层-流式HTTP细节      |
| 2025.08.18 | Client Features            | 🟢 已完成 | 客户端能力总结             |
| 2025.08.19 | Server Features            | 🟡 开发中 | 服务端能力总结             |
| 2025.08.21 | None                       | 🟡 开发中 | 修改文档结构              |

### 模块开发日志
<details open>
<summary>
<span style="font-size:1.0em;color:#222222"><strong>McpSchema</strong></span>
<span style="color:#586069; margin: 0 12px">│</span>
<strong>代码</strong>：🟡 开发中
<span style="color:#586069; margin: 0 12px">│</span>
<strong>文档</strong>：🟡 撰写中
<span style="color:#586069; margin: 0 12px">│</span>
<strong>最后更新</strong>：2025.08.21
</summary>

| 日期         | 代码状态   | 今日代码进度                  | 文档总结   | 今日文档进度                                                          |
|------------|--------|-------------------------|--------|-----------------------------------------------------------------|
| 2025.08.17 | 🟡 开发中 | 定义McpSchema             | ⚪ 未开始  | None                                                            |
| 2025.08.18 | 🟡 开发中 | None                    | 🟡 开发中 | 整理Request、Result、Notification、ClientCapacities、ServerCapacities |
| 2025.08.19 | 🟡 开发中 | None                    | 🟡 开发中 | None                                                            |
| 2025.08.21 | 🟡 开发中 | 完成McpSchema定义-TODO：修改注释 | 🟡 开发中 | TODO：数据结构文档待整理                                                  |

</details>

<details open>
<summary>
<span style="font-size:1.0em;color:#222222"><strong>McpTransport</strong></span>
<span style="color:#586069; margin: 0 12px">│</span>
<strong>代码</strong>：🟡 开发中
<span style="color:#586069; margin: 0 12px">│</span>
<strong>文档</strong>：🟡 撰写中
<span style="color:#586069; margin: 0 12px">│</span>
<strong>最后更新</strong>：2025.08.21
</summary>

| 日期         | 代码状态   | 今日代码进度                                                 | 文档总结   | 今日文档进度                                                          |
|------------|--------|--------------------------------------------------------|--------|-----------------------------------------------------------------|
| 2025.08.21 | 🟡 开发中 | 定义McpTransport、McpClientTransport、McpServerTransport接口 | ⚪ 未开始  | None                                                            |

</details>

## 参考资源

- [MCP 官方介绍](https://modelcontextprotocol.io/docs/getting-started/intro)
- [MCP 协议规范](https://modelcontextprotocol.io/specification/2025-06-18)
- [官方 Java SDK 源码](https://github.com/modelcontextprotocol/java-sdk)