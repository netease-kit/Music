

# 网易云信音乐教学解决方案 Android 端实现说明

## 一、终端整体业务逻辑简介

1. 解决方案概述

音乐教学解决方案旨在展现音乐教学场景下的音视频 SDK 能力，并为开发者提供可借鉴和集成的音乐教学 Demo。

该解决方案集成了网易云信互动直播和互动白板等多种能力，结合网易云信最新推出的高清音乐模式，最大限度地满足音乐教学场景中对音质的追求，同时实现了师生实时互动、曲谱标注、视频互动等功能，提升了课程质量。

2. 解决方案角色

音乐教学 Demo 的教学场景里分为两个角色`学生`和`老师`。学生可以主动预约课程，通过应用服务器请求分配可用教室，在课程中可看到老师同步在乐谱白板上的内容，并与老师进行语音或视频交流；老师可进入平台创建的课程房间，在上课过程中可在白板上翻页和标注，并选择单向观看学生上课视频或与学生进行双向语音或者视频交流。

3. 注意事项

该解决方案使用了云信即时通信、音视频通话和互动白板全套 SDK，因此在使用本解决方案之前请务必了解

* [IM即时通讯](http://dev.netease.im/docs/product/IM%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AF/SDK%E5%BC%80%E5%8F%91%E9%9B%86%E6%88%90/Android%E5%BC%80%E5%8F%91%E9%9B%86%E6%88%90/%E7%B3%BB%E7%BB%9F%E9%80%9A%E7%9F%A5?#%E8%87%AA%E5%AE%9A%E4%B9%89%E7%B3%BB%E7%BB%9F%E9%80%9A%E7%9F%A5) 的自定义通知能力
* [音视频通话](http://dev.netease.im/docs/product/%E9%9F%B3%E8%A7%86%E9%A2%91%E9%80%9A%E8%AF%9D/SDK%E5%BC%80%E5%8F%91%E9%9B%86%E6%88%90/Android%E5%BC%80%E5%8F%91%E9%9B%86%E6%88%90/%E5%A4%9A%E4%BA%BA%E9%9F%B3%E8%A7%86%E9%A2%91%E9%80%9A%E8%AF%9D) 的多人会议能力
* [互动白板](http://dev.netease.im/docs/product/%E4%BA%92%E5%8A%A8%E7%99%BD%E6%9D%BF/SDK%E5%BC%80%E5%8F%91%E9%9B%86%E6%88%90/Android%E5%BC%80%E5%8F%91%E9%9B%86%E6%88%90/%E5%A4%9A%E4%BA%BA%E4%BA%92%E5%8A%A8%E7%99%BD%E6%9D%BF) 的多人实时互动能力

## 二、音乐教学解决方案重难点逻辑实现

1. 老师与学生加入互动房间流程

   1）学生预约课程

   学生调用 DemoServerController#bookingRoom 向应用服务器发起预约课程请求。服务器将返回预约成功的房间信息以及老师的账号密码信息，用于老师端的登陆。

   2）老师创建音视频通道并加入

   老师通过应用服务器的房间信息中包含的房间 id，作为音视频通道的 roomName，调用 AVChatManager#createRoom 创建音视频通道，加入并等待学生进入。

   3）老师创建白板通道并加入

   老师通过应用服务器的房间信息中包含的房间 id，作为白板通道的 sessionId，调用 RTSManager2#createSession 创建白板通道，加入并等待学生进入。

   4）学生加入房间

   学生通过应用服务器给的房间 id，作为音视频通道的 roomName，调用 AVChatManager#joinRoom2 加入音视频通道。同时，使用房间 id，作为白板通道的 sessionId，调用 RTSManager2#joinSession，加入白板通道。老师和学生即可进行互动。

2. 白板互动协议

   基本协议与多人白板 Demo 协议一致。与多人白板 Demo 协议的区别如下：

   1. type 类型为 1，2，3 的起始点，移动点，结束点新增 `pageIndex` 信息。
   2. type 类型为 6 的清空，作为按页清空而存在。需要全部清空时，可发送 type 类型为 10 的同步准备。

3. 师生语音/视频通话

解决方案提供三种不同的师生互动模式：

1）双向语音，老师和学生仅进行语音通话（语音与白板互相独立，不影响对方功能）

2）单向视频，老师可以看到学生视频，学生仅可以进行语音与老师通话

3）双向视频，老师学生可以进行双向视频通话，隐藏白板界面



## 三、源码导读

### 工程说明

Android 音乐教学 Demo 工程基于以下开发

1. 网易云信 basesdk，avchat，nrtc 和 rts，版本为 5.0.0
2. 日志库 nim_log，版本为 1.3.0
3. View 注入 Butter Knife，版本为 8.8.1
4. JDK 使用 1.8.0

### 工程结构

```
com.netease.nim.musiceducation

|———— app			# app相关配置
|———— business		# 界面层业务逻辑实现
|———— common		# 通用ui组件、utils、第三方支持库
|———— doodle		# 多人白板封装
|———— protocol		# 与服务器交互的协议
```

### 详细说明

1. 模块说明

   1）app 模块：定义了 App 相关配置，提供了用户登录信息以及崩溃日志等。

   2）business 模块：是主要的界面层实现。activity 包含了界面实现，constant 包含互动过程中状态常量，helper 包含了帮助类。

   3）common 模块：定义了通用 UI 组件和工具类，包含界面的基类定义，使用到的 widget，dialog 以及其他通用工具类。

   4）doodle 模块：是多人白板封装的主要模块

   5）protocol 模块：定义了与服务器交互的协议。主要有应用服务器客户端，其他使用到的协议封装。

2. 主要类说明

   1）MainActivity：主界面，学生可以预约课程，老师和学生可以查询课程信息。

   2）RoomActivity：音乐教学界面。老师和学生的互动均在这个界面进行处理。

   3）DemoServerController：网易云信 Demo Http 客户端，提供了注册接口，学生相关的预约课程，查询课程接口；老师相关的查询课程等接口。

   4）CommandController：需要通过 im 点对点通知发送的协议，主要包括下课通知协议。

### 主要接口说明

#### 应用服务器接口

##### 1. 注册

```java
/**
 * 
 * @param account  账号
 * @param nickName 昵称
 * @param password 密码
 * @param callback 回调
 */
public void register(String account, String nickName, String password, final IHttpCallback<Void> callback) 
```

- 参数说明

| 参数       | 说明   |
| -------- | ---- |
| account  | 账号   |
| nickName | 昵称   |
| password | 密码   |
| callback | 回调   |

##### 2. 学生预约课程

```java
/**
 * 预约课程
 * @param account 学生账号
 * @param callback 回调
 */
public void bookingRoom(String account, IHttpCallback<RoomInfo> callback)
```

- 参数说明

  RoomInfo：

  | RoomInfo 接口          | 说明        |
  | -------------------- | --------- |
  | getRoomId()          | 获取房间 id   |
  | getTeacherAccount()  | 获取老师账号    |
  | getTeacherName()     | 获取老师名称    |
  | getTeacherPassword() | 获取老师登陆加密码 |
  | getStudentAccount()  | 获取学生账号    |
  | getStudentName()     | 获取学生名称    |

##### 3. 学生查询课程信息

```java
/**
 * 学生查询课程信息
 * @param account 学生账号
 * @param callback 回调
 */
public void studentQueryClass(String account, IHttpCallback<ClassInfo> callback)
```

- 参数说明

  ClassInfo 接口

| ClassInfo 接口 | 说明        |
| ------------ | --------- |
| getTotal()   | 总共预约的课程数  |
| getList()    | 获取预约的课程列表 |

##### 4. 老师查询课程信息

```java
/**
 * 老师查询课程信息
 * @param account 老师账号
 * @param callback 回调
 */
public void teacherQueryClass(String account, IHttpCallback<ClassInfo> callback)
```

- 参数说明

| 参数       | 说明   |
| -------- | ---- |
| account  | 老师账号 |
| callback | 回调   |

##### 5. 下课

```java
/**
 * 下课
 * @param account 老师账号
 * @param roomId    房间id
 * @param callback  回调
 */
public void closeClass(String account, String roomId, IHttpCallback<Void> callback)
```

| 参数       | 说明    |
| -------- | ----- |
| account  | 老师账号  |
| roomId   | 房间 id |
| callback | 回调    |

##### 6. 账号校验

```jav
/**
 * 账号校验
 * @param account 账号
 * @param callback 回调
 */
public void checkUser(String account, IHttpCallback<UserTypeInfo> callback) 
```

- 参数说明

  UserTypeInfo 接口

  | UserTypeInfo 接口 | 说明              |
  | --------------- | --------------- |
  | getAccount()    | 获取账号信息          |
  | getUserType()   | 角色类型。 0-学生，1-老师 |

#### 点对点通知接口

##### 1. 下课通知

```java
/**
 * 发送下课协议
 * @param roomId 房间id
 */
public void sendCloseCommand(String roomId)
```

- 点对点通知具体协议格式：

```java
{"command":1, "data":{"roomId":1024}}
```



