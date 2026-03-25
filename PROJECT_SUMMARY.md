# Tasks - IntelliJ IDEA 任务管理插件 项目总结

## 📋 项目概述

这是一个 **IntelliJ IDEA 插件**，提供在 IDE 内部直接进行任务管理的功能。开发者可以在不离开 IDE 的情况下管理项目待办事项，提高开发效率。

**项目信息：**

- 名称：TodoTasks (Tasks)
- 版本：1.0.0
- 开发者：shenyuanlaolarou
- 许可证：MIT
- 编程语言：Kotlin (主要) + Java
- 框架：IntelliJ Platform 2025.3

---

## 🏗️ 项目架构

### 目录结构

```
Tasks/
├── src/main/kotlin/com/example/tasks/
│   ├── model/           # 数据模型层
│   ├── storage/         # 数据存储层
│   └── ui/              # 用户界面层
├── src/main/resources/META-INF/
│   └── plugin.xml       # 插件配置
├── build.gradle.kts     # Gradle 构建配置
├── settings.gradle.kts  # Gradle 项目设置
└── README.md            # 项目说明
```

### 分层架构设计

```
┌─────────────────────────────────────────┐
│  UI Layer                               │  ← 用户界面交互
│  (工具窗口、任务列表、任务卡片、对话框)  │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│  Model Layer                            │  ← 数据模型定义
│  (Task 数据类, TaskStatus 枚举)         │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│  Storage Layer                          │  ← 数据持久化
│  (JsonTaskStorage, Gson 序列化)         │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│  File System                            │  ← 物理存储
│  (.idea/tasks.json, ~/.todoTasks/)      │
└─────────────────────────────────────────┘
```

---

## 📦 核心模块详解

### 1. 数据模型层 (`model/`)

#### `Task.kt`

任务核心数据类，包含所有任务属性：

```kotlin
data class Task(
    val id: String,                // 唯一标识符 (UUID)
    val title: String,             // 任务标题
    val description: String,       // 任务描述
    val status: TaskStatus,        // 任务状态
    val createdAt: Instant,        // 创建时间戳
    val updatedAt: Instant,        // 最后更新时间戳
    val isProjectLevel: Boolean = true  // 存储级别标识
)
```

#### `TaskStatus.kt`

任务状态枚举，支持状态循环切换：

```kotlin
enum class TaskStatus(val displayName: String) {
    TODO("待办"),
    IN_PROGRESS("进行中"),
    DONE("已完成");

    // 循环切换到下一个状态
    fun next(): TaskStatus
}
```

状态流转：`TODO → IN_PROGRESS → DONE → TODO`

---

### 2. 数据存储层 (`storage/`)

#### `TaskStorage.kt`

存储接口定义，提供抽象存储契约，支持未来扩展不同存储方式：

```kotlin
interface TaskStorage {
    fun loadTasks(): List<Task>
    fun saveTasks(tasks: List<Task>)
    fun addTask(task: Task)
    fun updateTask(task: Task)
    fun deleteTask(taskId: String)
    fun getTaskById(taskId: String): Task?
}
```

#### `JsonTaskStorage.kt`

**核心实现类**，基于 JSON 文件存储：

**两级存储机制：**
| 存储级别 | 存储位置 | 适用场景 |
|---------|---------|---------|
| 项目级别 | `[项目根目录]/.idea/tasks.json` | 与项目关联，支持 Git 版本控制，团队共享 |
| 用户级别 | `~/.todoTasks/tasks.json` | 个人跨项目通用任务，本地私有 |

**技术特点：**

- 使用 **Gson 2.10.1** 进行 JSON 序列化/反序列化
- 支持 `Instant` 时间类型的自定义序列化
- 目录自动创建，文件IO异常处理
- 线程安全：每次保存完整重写文件

---

### 3. 用户界面层 (`ui/`)

#### `TasksToolWindowFactory.kt`

实现 IntelliJ Platform 的 `ToolWindowFactory` 接口，负责创建并注册工具窗口到 IDE。

#### `TasksToolWindowContent.kt`

主工具窗口内容容器：

- 初始化存储系统
- 创建根面板布局
- 提供刷新任务列表入口

#### `TaskListPanel.kt`

**核心界面组件** - 任务列表面板：

- 顶部：左侧筛选下拉框 + 右侧统计信息
- 中部：垂直布局的任务卡片列表
- 底部："添加任务"按钮居中
- 支持：按状态筛选、实时统计、任务排序（按创建时间倒序）

#### `TaskItemComponent.kt`

单个任务卡片组件：

- 显示任务标题、状态标签
- 操作按钮：状态切换、编辑、删除
- 点击卡片弹出详情查看/编辑对话框
- 根据状态应用不同样式（透明度、边框颜色）

#### `AddTaskDialog.kt`

添加/编辑任务对话框：

- 标题输入框 + 描述文本区域
- 存储级别选择（项目级/用户级）
- 表单验证：不允许空标题
- 复用：新建任务和编辑任务共用同一个对话框

---

## 🎨 界面布局

```
┌─────────────────────────────────────────────────────────────┐
│ 筛选: [全部 ▼]        待办: 2 | 进行中: 1 | 完成: 0           │  ← 顶部栏
├─────────────────────────────────────────────────────────────┤
│                                                             │
├─────────────────────────────────────────────────────────────┤
│ 完成用户认证模块               [完成] [编辑] [🗑️删除]          │  ← 任务卡片
│ 实现登录和注册功能流程                                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 编写数据库访问层                   [进行中] [编辑] [🗑️删除]  │
│ 封装CRUD操作和事务管理                                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ （空白区域自动填充）                                        │
├─────────────────────────────────────────────────────────────┤
│                    [ + 添加任务 ]                            │  ← 底部按钮居中
└─────────────────────────────────────────────────────────────┘
```

---

## 🧩 页面布局核心实现

### 整体布局层级

**文件位置：** `src/main/kotlin/com/example/tasks/ui/TasksToolWindowContent.kt`
**核心函数：** `createUI()` 第 26-47 行

```kotlin
// 根容器使用 BorderLayout
class TasksToolWindowContent(private val project: Project) : JPanel(BorderLayout()) {
    private fun createUI() {
        // 主面板使用 BoxLayout.Y_AXIS 垂直布局
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)

        // 1. 任务列表放在 JBScrollPane 中支持滚动
        taskListPanel = TaskListPanel(storage) { refreshTasks() }
        val scrollPane = JBScrollPane(taskListPanel)
        mainPanel.add(scrollPane)

        // 2. 底部添加任务按钮居中
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        val addButton = JButton("+ 添加任务")
        buttonPanel.add(addButton)
        mainPanel.add(buttonPanel)

        add(mainPanel, BorderLayout.CENTER)
    }
}
```

**布局结构：**

```
BorderLayout (CENTER)
└── BoxLayout.Y_AXIS
    ├── JBScrollPane (scrollable)
    │   └── TaskListPanel (all tasks)
    └── Button Panel (FlowLayout.CENTER)
        └── "添加任务" button
```

---

### 任务列表布局

**文件位置：** `src/main/kotlin/com/example/tasks/ui/TaskListPanel.kt`
**核心函数：** `refresh()` 第 41-105 行，`createTopBar()` 第 115-175 行

**整体结构：**

- 继承 `JBBox(BoxLayout.Y_AXIS)` 垂直排列
- `refresh()` 方法每次刷新都会重新构建整个列表（数据量小，简单可靠）
- 任务按创建时间**倒序排列**，最新任务在顶部
- 任务之间添加 8px 间距（使用空 JPanel 设置 `preferredSize`）
- 最后添加 `JBBox.createVerticalGlue()` 填充剩余空白空间

**顶部栏 - 筛选 + 统计 对齐技巧：**

| 层级    | 布局管理器                     | 目的                                  |
|-------|---------------------------|-------------------------------------|
| 外层    | `GridLayout(1, 2, 10, 0)` | 强制分成左右两格，**宽度完全相等**                 |
| 左/右格子 | `GridBagLayout`           | 使用 `anchor = BASELINE` 实现**垂直基线对齐** |
| 水平居中  | `weightx = 1.0`           | 内容在格子内水平居中                          |

这个设计确保左侧"筛选"文字和右侧统计文字**永远在同一水平线上完美对齐**，这是一个经典的 Java Swing 布局技巧。

**统计信息使用 HTML 染色：**

```kotlin
// 不同状态使用不同颜色，统计数字加粗
val label = JLabel(
    "<html>" +
            "<span style='color:#CCCCCC;padding: 0 12px'>待办: <b>$todoCount</b></span> | " +
            "<span style='color:#4285F4;padding: 0 12px'>进行中: <b>$inProgressCount</b></span> | " +
            "<span style='color:#34A853;padding: 0 12px'>完成: <b>$doneCount</b></span>" +
            "</html>"
)
```

---

### 任务卡片布局

**文件位置：** `src/main/kotlin/com/example/tasks/ui/TaskItemComponent.kt`
**核心：** 构造器 `init` 第 32-121 行

**布局结构：**

- 继承 `JPanel(BorderLayout())`
- 固定高度 **70px**，宽度填满整个面板：`preferredSize = Dimension(0, 70), maximumSize = Dimension(Int.MAX_VALUE, 70)`
- 圆角边框：`RoundedLineBorder(JBColor.GRAY, 5, 1)` + 内边距

```
BorderLayout
├── CENTER (左半部分)
│   └── BorderLayout
│       ├── NORTH: 任务标题（加粗），已完成会添加删除线 + 灰色
│       └── CENTER: 任务描述（小号字体灰色）
└── EAST (右半部分 - 190px 宽)
    └── BoxLayout.Y_AXIS + vertical glue
        └── FlowLayout.CENTER (水平排列按钮)
            ├── 状态按钮 (70px 宽) ├── 编辑 (50px) ┇� 删除 (50px)
```

**样式处理：**

- 根据状态不同，状态按钮使用不同背景色：待办灰色、进行中蓝色、完成绿色
- 已完成任务标题：灰色 + 删除线效果
- 点击卡片任意位置（非按钮）弹出详情弹窗 (`showDetailsPopup()`)
- 详情弹窗使用 `JBPopupFactory` 创建，支持移动和调整大小

**点击弹窗详情：** 第 123-168 行 `showDetailsPopup()`

- 显示完整标题、描述、创建时间
- 使用 IntelliJ 原生 `JBPopup` 组件，体验原生

---

### 间距设计原则

整个界面统一使用以下间距规则：

- 任务卡片之间：8px 垂直间距，背景同色，不显示分割线 → 干净清爽
- 顶部栏与第一个任务：8px 间距
- 卡片内边距：5px
- 按钮之间水平间距：4px

---

## ✨ 核心功能特性

| 功能         | 描述                                   |
|------------|--------------------------------------|
| **任务增删改查** | 完整的 CRUD 操作支持                        |
| **状态循环切换** | 点击状态按钮一键流转 TODO → IN_PROGRESS → DONE |
| **按状态筛选**  | 下拉框筛选：全部/待办/进行中/完成                   |
| **实时统计**   | 顶部显示各状态任务数量统计                        |
| **两级存储**   | 项目级（版本控制）+ 用户级（本地私有）                 |
| **深色主题适配** | 原生支持 IDE 浅色/深色主题切换                   |
| **时间排序**   | 新任务显示在列表顶部                           |
| **点击查看详情** | 点击任务卡片弹窗查看/编辑                        |
| **输入验证**   | 禁止创建空标题任务                            |

---

## ⚙️ 技术栈

### 构建配置

**Gradle 插件：**

- `kotlin("jvm")` version 2.1.20
- `org.jetbrains.intellij.platform` version 2.13.1

**依赖：**

- Kotlin StdLib
- Gson 2.10.1 (JSON 处理)
- IntelliJ IDEA 2025.3 (平台依赖)

**JVM：**

- 源代码兼容：JDK 17
- 目标兼容：JDK 17

**插件兼容性：**

- sinceBuild: 253 (2025.3)
- untilBuild: 263.* (2026.3)

---

## 🛠️ 构建与安装

### 常用命令

```bash
# 构建插件包 (生成 ZIP)
./gradlew buildPlugin

# 调试运行 (启动新的 IDE 实例加载插件)
./gradlew runIde

# 签名插件 (发布到 Marketplace)
./gradlew signPlugin
```

### 构建产物

- 插件分发包：`build/distributions/Tasks-<version>.zip`
- 编译类输出：`build/classes/`

### 安装到 IDE

1. 获得构建好的 ZIP 文件
2. 打开 IntelliJ IDEA → `Settings` → `Plugins` → `⚙️` → `Install plugin from disk`
3. 选择 ZIP 文件安装，重启 IDE 生效

---

## 📐 设计模式应用

| 模式       | 应用位置                                            |
|----------|-------------------------------------------------|
| **MVC**  | Model (Task) → View (UI 组件) → Controller (事件处理) |
| **工厂模式** | `TasksToolWindowFactory` 创建工具窗口                 |
| **策略模式** | 项目级/用户级存储策略选择                                   |
| **接口抽象** | `TaskStorage` 接口支持多种存储实现                        |

---

## 📊 设计亮点

1. **清晰分层**：模型/存储/UI 三层分离，职责明确，易于维护

2. **两级存储**：巧妙区分项目级和用户级存储，兼顾团队协作和个人使用

3. **原生集成**：完全使用 IntelliJ Platform 原生组件，主题适配自然，性能优异

4. **无外部依赖**：除了 Gson 没有其他第三方运行时依赖，插件体积小，启动快

5. **健壮错误处理**：文件 IO 异常捕获，空数据处理，确保插件不会导致 IDE 崩溃

6. **用户体验**：直观的卡片布局，便捷的操作按钮，流畅的交互体验

---

## 🔧 配置文件说明

### `gradle.properties`

Gradle 构建属性配置，包括 JVM 内存参数、IDE 版本指定等。

### `settings.gradle.kts`

配置了阿里云 Maven 镜像加速，解决国内网络访问问题：

```kotlin
maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
maven { url = uri("https://maven.aliyun.com/repository/public") }
```

### `plugin.xml`

IntelliJ 插件描述符文件：

- 声明插件 ID、名称、版本、描述
- 注册工具窗口扩展点
- 声明依赖的模块

---

## 🚀 适用场景

- **个人开发**：在 IDE 内直接管理项目任务，无需切换到外部工具
- **团队协作**：项目级任务文件提交到 Git，团队成员共享任务进度
- **多项目管理**：用户级存储统一管理跨项目的个人任务
- **敏捷开发**：简单的 TODO → Doing → Done 工作流，符合敏捷开发习惯

---

## 📝 总结

这是一个**设计精良、功能完整**的 IntelliJ IDEA 插件项目：

- ✅ 架构清晰：分层合理，模块化设计，易于理解和扩展
- ✅ 功能实用：解决开发者在 IDE 内部管理任务的实际需求
- ✅ 体验优秀：原生 UI 集成，流畅交互，主题适配
- ✅ 数据灵活：两级存储机制满足不同使用场景
- ✅ 代码质量：Kotlin 空安全，异常处理完善

项目整体遵循 IntelliJ Platform 开发规范，是一个很好的 IDE 插件学习范例。
