# Library 图书馆管理系统

这是一个用于学习 Java 后端开发的图书馆管理 REST API。项目基于 Spring Boot、Spring MVC、JdbcTemplate、MySQL 和 Flyway，实现了图书、读者以及借阅记录的基础管理，并围绕参数校验、事务、异常处理、分页、库存一致性和数据库迁移进行练习。

> 当前项目以学习为目的，并非可直接用于生产环境的完整图书馆系统。README 只描述代码中已经实现的行为；尚未实现的权限、严格并发控制等内容会单独列在“已知限制”中。

## 主要功能

- 图书创建、查询、修改、搜索、状态修改和库存调整
- 图书按价格、作者、标题和状态查询
- 可借图书查询与图书分页
- 图书软下架：删除接口将状态改为 `OFF_SALE`，不会物理删除记录
- 读者创建、查询、资料修改和状态修改
- 借书与还书
- 每位读者最多同时借阅 5 本图书
- 禁止停用读者借书
- 禁止借阅已下架或库存不足的图书
- 禁止同一读者重复借阅尚未归还的同一本书
- 借书与还书使用事务
- 库存通过条件更新进行原子增减，避免库存变成负数
- 当前逾期记录查询
- 带读者名、书名和分页元数据的借阅历史查询
- Bean Validation 参数校验和统一异常处理
- Flyway 自动管理数据库表结构

## 技术栈

| 技术 | 当前用途 |
|---|---|
| Java 17 | 项目运行语言 |
| Spring Boot 4.1.1 | 应用启动与自动配置 |
| Spring Web MVC | REST API |
| Spring JDBC / JdbcTemplate | 数据库访问 |
| Spring Validation | 请求 DTO 参数校验 |
| Spring Transaction | 借书、还书等事务管理 |
| MySQL | 业务数据存储 |
| Flyway | 数据库版本迁移 |
| HikariCP | 数据库连接池，由 Spring JDBC 间接引入 |
| JUnit 5、Mockito、MockMvc | 单元测试和接口层测试 |
| Maven Wrapper | 构建与测试 |

项目使用 JdbcTemplate 直接编写 SQL，没有使用 JPA 或 Hibernate。

## 项目结构

```text
src
├── main
│   ├── java/org/example/library
│   │   ├── controller   # HTTP 路由、请求解析和响应状态
│   │   ├── dto          # 请求与响应数据结构
│   │   ├── entity       # 图书、读者、借阅记录及状态枚举
│   │   ├── exception    # 业务异常和全局异常处理
│   │   ├── repository   # JdbcTemplate SQL 与结果映射
│   │   └── service      # 业务规则与事务边界
│   └── resources
│       ├── application.yml
│       └── db/migration # Flyway 数据库迁移
└── test
    └── java/org/example/library
        ├── controller   # MockMvc 控制器测试
        ├── repository   # 真实数据库 Repository 测试
        └── service      # Service 单元测试与事务集成测试
```

## 运行要求

- JDK 17
- MySQL 8.x
- 项目自带 Maven Wrapper，无需单独安装 Maven
- 默认服务端口：`8080`
- 默认数据库地址：`localhost:3306/library`

## 数据库准备

Flyway 会自动创建 `book`、`reader` 和 `borrow_record` 表，但 MySQL 中需要先存在名为 `library` 的数据库。

登录 MySQL 后执行：

```sql
CREATE DATABASE IF NOT EXISTS library
    CHARACTER SET utf8mb4;
```

项目启动时会依次执行：

```text
V1__create_book_table.sql
V2__create_borrow_tables.sql
```

已经在共享数据库中执行过的迁移文件不应直接修改。后续结构变更应新增更高版本的迁移，例如 `V3__description.sql`。

## 配置数据库账号

`application.yml` 从以下环境变量读取数据库账号：

```text
DB_USERNAME
DB_PASSWORD
```

在 macOS 或 Linux 终端中可以这样设置：

```bash
export DB_USERNAME='root'
export DB_PASSWORD='你的数据库密码'
```

然后在同一个终端中启动应用。不要把真实数据库密码写入 README、提交到 Git，或发送到公开环境。

在 IntelliJ IDEA 中，也可以在运行配置的“环境变量”中填写：

```text
DB_USERNAME=root;DB_PASSWORD=你的数据库密码
```

## 启动项目

macOS 或 Linux：

```bash
./mvnw spring-boot:run
```

也可以直接在 IntelliJ IDEA 中运行：

```text
org.example.library.LibraryApplication
```

启动成功后，接口基础地址是：

```text
http://localhost:8080
```

项目已加入 Spring Boot DevTools。应用启动后，修改 Java 代码并在 IDEA 中执行 `Build -> Build Project`，DevTools 会在重新编译后快速重启应用。不要重复点击“运行”启动第二个实例，否则可能出现端口 `8080` 被占用的问题。

## API 概览

### 图书接口

基础路径：`/books`

| 方法 | 路径 | 说明 | 成功状态 |
|---|---|---|---|
| `GET` | `/books` | 查询全部图书，包括当前实现中的下架图书 | `200` |
| `GET` | `/books/{id}` | 按 ID 查询；当前实现会隐藏下架图书 | `200` / `404` |
| `GET` | `/books/count` | 查询图书总数 | `200` |
| `POST` | `/books` | 创建图书，状态由服务端设为 `ON_SALE` | `201` |
| `PUT` | `/books/{id}` | 修改图书资料，不修改状态 | `204` / `404` |
| `DELETE` | `/books/{id}` | 软下架图书，将状态改为 `OFF_SALE` | `204` |
| `PATCH` | `/books/{id}/status?status=ON_SALE` | 修改图书状态 | `204` |
| `PATCH` | `/books/{id}/stock?quantity=1` | 原子增加或减少库存 | `204` / `400` |
| `GET` | `/books/available` | 查询 `ON_SALE` 且库存大于 0 的图书 | `200` |
| `GET` | `/books/status?status=ON_SALE` | 按状态查询 | `200` |
| `GET` | `/books/search?keyword=Java` | 按书名模糊查询 | `200` |
| `GET` | `/books/search/author?keyword=作者` | 按作者模糊查询 | `200` |
| `GET` | `/books/price-range?minPrice=10&maxPrice=100` | 按价格范围查询 | `200` |
| `GET` | `/books/page?page=1&size=10` | 图书分页查询 | `200` |

图书状态：

```text
ON_SALE
OFF_SALE
```

创建图书示例：

```bash
curl -i -X POST 'http://localhost:8080/books' \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "isbn": "9780134685991",
    "price": 88.00,
    "stock": 3,
    "publishedDate": "2018-01-06"
  }'
```

图书分页响应示例：

```json
{
  "content": [],
  "page": 1,
  "size": 10,
  "total": 0,
  "totalPages": 0
}
```

### 读者接口

基础路径：`/readers`

| 方法 | 路径 | 说明 | 成功状态 |
|---|---|---|---|
| `GET` | `/readers` | 查询全部读者 | `200` |
| `GET` | `/readers/{id}` | 按 ID 查询读者 | `200` / `404` |
| `POST` | `/readers` | 创建读者，状态由服务端设为 `ACTIVE` | `201` |
| `PUT` | `/readers/{id}` | 修改姓名和手机号，不修改状态 | `204` / `404` |
| `PATCH` | `/readers/{id}/status?status=DISABLED` | 修改读者状态 | `204` |

读者状态：

```text
ACTIVE
DISABLED
```

创建读者示例：

```bash
curl -i -X POST 'http://localhost:8080/readers' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "张三",
    "phone": "13800000000"
  }'
```

### 借阅接口

基础路径：`/borrows`

| 方法 | 路径 | 说明 | 成功状态 |
|---|---|---|---|
| `POST` | `/borrows` | 借书 | `201` |
| `PATCH` | `/borrows/{borrowId}/return` | 归还图书 | `204` |
| `GET` | `/borrows?readerId=1` | 查询某位读者的原始借阅记录 | `200` |
| `GET` | `/borrows/details?readerId=1` | 查询包含读者名和书名的借阅详情 | `200` |
| `GET` | `/borrows/page?readerId=1&page=1&size=20` | 查询带分页元数据的借阅详情 | `200` |
| `GET` | `/borrows/overdue` | 查询当前未归还且已过到期时间的记录 | `200` |

借阅状态：

```text
BORROWED
RETURNED
```

借书请求示例：

```bash
curl -i -X POST 'http://localhost:8080/borrows' \
  -H 'Content-Type: application/json' \
  -d '{
    "readerId": 1,
    "bookId": 5
  }'
```

归还示例：

```bash
curl -i -X PATCH 'http://localhost:8080/borrows/1/return'
```

借阅分页响应示例：

```json
{
  "content": [
    {
      "borrowId": 1,
      "readerId": 1,
      "readerName": "张三",
      "bookId": 5,
      "bookTitle": "Effective Java",
      "borrowedAt": "2026-09-17T20:44:23",
      "dueAt": "2026-10-17T20:44:23",
      "returnedAt": null,
      "status": "BORROWED"
    }
  ],
  "page": 1,
  "size": 20,
  "total": 1,
  "totalPages": 1
}
```

## 借阅业务规则

借书操作由 `BorrowService.borrow()` 的事务保护，当前规则如下：

1. 读者和图书 ID 必须存在且大于 0。
2. 读者必须存在并处于 `ACTIVE` 状态。
3. 每位读者最多同时借阅 5 本尚未归还的图书。
4. 图书必须存在并处于 `ON_SALE` 状态。
5. 同一读者不能重复借阅尚未归还的同一本书。
6. 库存通过带条件的原子 SQL 扣减；库存不足时更新影响 0 行，借阅失败。
7. 借阅成功后生成记录，到期时间为数据库当前时间加 30 天。

还书操作同样由事务保护：

1. 借阅记录必须存在并处于 `BORROWED` 状态。
2. 借阅记录以条件更新方式改为 `RETURNED`，同时记录归还时间。
3. 对应图书库存增加 1。
4. 读者即使已被停用，仍允许归还已经借出的图书。

## 参数校验与错误响应

请求 DTO 使用 Jakarta Bean Validation。常见规则包括：

- 文本字段不能为空或只包含空格
- 图书价格和库存不能为负数
- 借阅请求中的读者 ID、图书 ID 必须大于 0
- 每页数量必须在 1 到 100 之间
- ISBN 和手机号由数据库唯一约束最终兜底

常见 HTTP 状态：

| 状态码 | 当前含义 |
|---|---|
| `201 Created` | 创建图书、读者或借阅成功 |
| `204 No Content` | 修改状态、资料、库存或归还成功 |
| `400 Bad Request` | 参数格式、范围或业务输入不合法 |
| `404 Not Found` | 图书、读者或借阅记录不存在 |
| `409 Conflict` | ISBN/手机号重复、库存或状态冲突、重复借阅等 |

字段校验失败时返回结构化 JSON：

```json
{
  "status": 400,
  "message": "参数校验失败",
  "fieldErrors": {
    "bookId": "图书 ID 必须大于 0"
  }
}
```

当前其他业务异常主要返回纯文本消息，尚未统一成包含稳定错误代码的 JSON 响应。

## 运行测试

完整测试：

```bash
./mvnw test
```

完整测试套件包含 `@SpringBootTest`、Repository 测试和事务集成测试，因此需要：

- MySQL 已启动
- `library` 数据库已创建
- `DB_USERNAME`、`DB_PASSWORD` 已正确配置

只运行不依赖真实数据库的 Service 和 Controller 测试，可以指定测试类：

```bash
./mvnw \
  -Dtest=BookServiceTests,BorrowServiceTests,ReaderServiceTests,BookControllerTests,BorrowControllerTests,ReaderControllerTests \
  test
```

只检查主代码能否编译：

```bash
./mvnw -DskipTests compile
```

## 当前设计说明

### 为什么使用 DTO

创建和修改请求使用独立 DTO，避免客户端直接设置数据库 ID、状态和审计时间。借阅分页返回 `BorrowDetailResponse`，只公开页面需要的借阅信息，并补充读者名和书名。

### 为什么借还书使用事务

借书需要同时扣减库存和新增借阅记录，还书需要同时修改借阅状态和恢复库存。如果中间一步失败，事务应让整组数据库操作一起回滚。

### 为什么库存使用条件更新

库存扣减 SQL 在 `WHERE` 中检查变化后库存不能小于 0。即使两个请求同时看到相同库存，也只有满足条件的更新才能成功，从数据库层面避免负库存。

### 为什么删除图书不是物理删除

借阅记录通过外键关联图书。当前删除接口只把图书改为 `OFF_SALE`，从而保留历史关系。此策略适用于当前学习项目，但软删除并不是所有系统和所有数据的通用规则。

## 已知限制

以下内容尚未在当前代码中完整实现：

- 没有登录、身份认证和角色授权；管理员与普通读者接口尚未隔离
- `GET /readers` 会返回读者手机号，不应直接暴露到未授权的生产接口
- 借阅数量上限和“同一读者不能重复借同一本书”采用先查询后写入；极端并发下仍可能同时通过检查
- 当前没有请求幂等键，客户端重试主要依靠现有业务冲突规则防止重复操作
- 时间字段使用 `LocalDateTime`，API 时间值本身不携带时区偏移
- 部分查询接口直接返回 Entity，响应模型尚未完全统一为 DTO
- 普通 `OFFSET` 分页在非常深的页码下可能变慢
- 错误响应尚未统一为带稳定业务错误码的结构
- 当前没有 OpenAPI/Swagger 接口文档
- 当前没有为借阅分页查询显式创建 `(reader_id, borrowed_at, id)` 联合索引

这些限制是后续学习方向，不代表应该一次性全部加入项目。每项改动都应先明确问题、预测结果，并通过测试、数据库约束或执行计划验证。

## 后续可学习方向

- 使用 Spring Security 实现管理员与读者权限
- 为并发借阅上限和重复借阅设计更严格的串行化或数据库约束
- 使用统一的 `ErrorResponse` 和稳定业务错误码
- 使用 `EXPLAIN` 与足量测试数据验证联合索引效果
- 对大数据列表尝试游标分页
- 使用可控时钟测试逾期与到期边界
- 增加少量高价值集成测试，重点验证事务回滚和数据不变量
- 增加 OpenAPI 文档，但保持 README 与真实实现同步

