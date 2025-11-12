# 居家广场舞平台 - 依赖配置说明

## 项目信息
- **JDK版本**: 1.8
- **Spring Boot版本**: 2.6.13
- **Maven版本**: 3.8.1

## 核心依赖

### 1. Spring Boot Web Starter
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
**用途**: 提供Web开发的基础功能，包括Spring MVC、Tomcat服务器等

### 2. MySQL数据库连接
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```
**用途**: MySQL数据库驱动，用于连接MySQL数据库

### 3. MyBatis持久层框架
```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.2.2</version>
</dependency>
```
**用途**: 数据持久层框架，简化数据库操作

### 4. Thymeleaf模板引擎
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```
**用途**: 服务端模板引擎，用于渲染HTML页面

## 增强依赖

### 5. 文件上传支持
```xml
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.4</version>
</dependency>
```
**用途**: 支持文件上传功能，用于视频文件上传

### 6. 数据验证
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
**用途**: 提供数据验证注解，如@Valid、@NotNull等

### 7. 开发工具
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```
**用途**: 开发时自动重启、热部署等功能

### 8. 配置处理器
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```
**用途**: 处理自定义配置属性，提供IDE智能提示

### 9. Lombok (可选)
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```
**用途**: 简化Java代码，自动生成getter/setter等方法

### 10. JSON处理
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```
**用途**: JSON序列化和反序列化

### 11. Apache Commons工具类
```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
```
**用途**: 提供常用的工具类方法

### 12. 测试依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
**用途**: 单元测试和集成测试支持

## 版本兼容性

所有依赖都与以下版本兼容：
- **JDK 1.8**: ✅ 完全兼容
- **Spring Boot 2.6.13**: ✅ 完全兼容
- **MySQL 8.0+**: ✅ 完全兼容

## 注意事项

1. **MyBatis版本**: 使用2.2.2版本，与Spring Boot 2.6.13完全兼容
2. **文件上传**: 已配置最大文件大小为100MB
3. **开发工具**: DevTools仅在开发环境生效
4. **Lombok**: 可选依赖，如需使用请确保IDE安装了Lombok插件

## 下一步建议

根据项目需求，可以考虑添加以下依赖：
- **Spring Security**: 安全认证和授权
- **Redis**: 缓存和Session存储
- **Swagger**: API文档生成
- **Logback**: 日志管理
