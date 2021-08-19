# hi

hi 是一个简单, 实用, 安全的代理程序. 目前仅支持代理 TCP 连接, 未来将添加更多支持的协议类型.

由于当前 master 分支上的程序仅仅是一个可用的原型, 其架构设计和程序实现均十分粗糙. 因此, 我计划将 需要实现的内容分成数个特性包, 分期实现全部计划中的特性.

## 特性包开发计划

基础架构更新：

- 实现 hi 私有透明代理协议
- 使用 YAML 配置文件
- 新增动态代理端口机制和配套的 publisher 模块
- 新增对 gzip, deflate 等压缩算法的支持
- 新增对 AES-256-GCM 等加密算法的支持
- 新增对 TLS 加密链路的支持
- 更新为 io node 体系

管理模块更新：

- 新增黑白名单模块
- 新增 WebUI 管理界面
- 新增 HTTP Rest 管理接口

## hi 协议简介

### Session 建立

client 发送 AuthenticationRequest 帧, 服务器对其中的内容进行校验, 并返回 AuthenticationReply 帧。

AuthenticationRequest (0x01) 帧结构:

| 长度 | 名称 | 备注 |
| ---- | ---- | ---- |
| 1Byte | Frame Type | 帧类型 |
| 1Byte | Authentication | 认证方法, 目前只支持用户名密码认证 |
| 1Byte | UserNameLength | 用户名字段长度 | 
| 由 UserNameLength 决定 | UserName | 用户名 |
| 1Byte | PasswordLength | 密码长度 |
| 由 PasswordLength 决定 | Password | 密码 |

AuthenticationReply (0x02) 帧结构

| 长度 | 名称 | 备注 |
| ---- | ---- | ---- |
| 1Byte | Frame Type | 帧类型 |
| 1Byte | Status | 0x0 认证成功, 大于 0x0 失败 |

### Stream 创建

client 发送 StreamOpenRequest 帧, 请求 server 建立到下一跳的连接. server 根据操作结果, 返回 StreamOpenReply 帧. 客户端或者服务器均维护一个 AtomicInteger ID
池, 每一 ID 在 2Byte 范围内不重复. 客户端使用单数, 服务端使用双数, 以避免 ID 冲突.

StreamOpenRequest (0x03) 帧结构

| 长度 | 名称 | 备注 |
| ---- | ---- | ---- |
| 1Byte | Frame Type | 帧类型 |
| 2Byte | Issued ID | 分配的 Stream ID |

StreamOpenReply (0x04) 帧结构

| 长度 | 名称 | 备注 |
| ---- | ---- | ---- |
| 1Byte | Frame Type | 帧类型 |
| 1Byte | Status | 0x0 下一跳连接成功, 大于 0x0 连接失败 | 

### Session 重连

当任意一方的 Stream ID 满了之后, 或者到达配置的 MAX_STREAM_COUNT 之后, 都应发出 SessionResetRequest 帧. 双方收到 SessionResetRequest 帧之后, 不再接受新来的
Stream. 其接受者在处理完所有数据传输任务后, 返回 SessionResetReply 帧.

SessionResetRequest (0x05) 和 SessionResetReply (0x06) 均没有 Payload, 帧结构为:

| 长度 | 名称 | 备注 |
| ---- | ---- | ---- |
| 1Byte | Frame Type | 帧类型 |

### KeepAlive 机制

通过在客户端发送 Ping (0x07) 帧实现 KeepAlive 机制. 每当发送队列和接受队列为空时, 客户端开始计时, 默认为 30s (1 个 KEEP_ALIVE_TIMEOUT 的时间). Ping 帧没有 Payload.
Session 层面有 3 倍 KEEP_ALIVE_TIME_OUT 的超时时间. 当第 2 个 PING 帧发出去之后, 客户端等待 30s, 若没有收到相应则断开连接.

### Session 关闭

采用 GoAway (0x08) 帧关闭 Session, 帧结构如下:

| 长度 | 名称 | 备注 |
| ---- | ---- | ---- |
| 1Byte | Frame Type | 帧类型 |
| 1Byte | Reason | 为何关闭 |

Reason 字段可有如下取值:

1. 0x0, 正常关闭
2. 0x1, 协议错误
3. 0x3, 内部错误
