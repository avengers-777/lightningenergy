
# LightningEnergy

**LightningEnergy** 是一个基于TRON区块链的能量租赁与自动回收平台。此平台采用Java、Spring Boot WebFlux，以及MongoDB和Redis数据库，提供高性能和响应式的Web应用程序环境，旨在为用户提供一种有效、透明的能量管理解决方案。

## 相关项目

- **网站前端**：[Lightning Energy - Frontend](https://github.com/avengers-777/lightning_energy) 使用Next.js, TypeScript和React技术栈构建。
- **管理后台**：[Lightning Control Center - Admin Panel](https://github.com/avengers-777/lightning_control_center) 也基于Next.js, TypeScript和React技术栈。

## 技术栈

- **Java SDK 21**
- **Spring Boot WebFlux**
- **MongoDB**
- **Redis**
- **Next.js**
- **TypeScript**
- **React**

## 开始使用

### 环境准备

确保你的开发环境中已安装以下软件：
- Java JDK 21
- MongoDB
- Redis
- Git

### 安装和配置MongoDB

1. **安装MongoDB**：访问 [MongoDB官方下载页面](https://www.mongodb.com/try/download/community) 下载并安装适合你操作系统的MongoDB版本。

2. **启动MongoDB服务**：
    - 对于Mac和Linux系统，通常可以通过以下命令启动：
      ```bash
      mongod
      ```
    - 对于Windows，你可能需要指向安装目录下的 `mongod.exe` 文件。

3. **配置副本集**：
    - 启动MongoDB实例时，需要启用副本集功能。可以通过修改MongoDB的配置文件或在启动时指定参数来实现。
    - 在MongoDB配置文件中（通常名为 `mongod.conf`），添加副本集设置：
      ```yaml
      replication:
        replSetName: "rs0"
      ```
    - 重启MongoDB服务，并通过MongoDB shell初始化副本集：
      ```bash
      mongo
      > rs.initiate()
      ```

### 安装Redis

1. **安装Redis**：
    - 对于Mac用户，可以使用Homebrew：
      ```bash
      brew install redis
      ```
    - 对于Linux用户，可以使用包管理器，例如：
      ```bash
      sudo apt-get install redis-server
      ```
    - Windows用户可以下载并安装从 [Redis官方下载页面](https://redis.io/download)。

2. **启动Redis服务**：
    - 通常在安装后Redis会自动启动。
    - 可以通过运行 `redis-cli ping` 检查Redis服务器是否运行，期待的回应是 `PONG`。

### 配置项目

1. 克隆项目仓库：
   ```bash
   git clone https://github.com/yourusername/LightningEnergy.git
   ```
2. 进入项目目录：
   ```bash
   cd LightningEnergy
   ```
3. 构建项目：
   ```bash
   ./mvnw clean install
   ```
4. 运行项目：
   ```bash
   ./mvnw spring-boot:run
   ```



## 配置指南

为了确保 **LightningEnergy** 平台可以成功连接和交互TRON区块链，你需要配置 `application.yml` 文件中的几个关键属性。以下是配置步骤：

### 修改 `application.yml`

找到项目中的 `application.yml` 文件，通常位于 `src/main/resources/` 目录下，然后填写以下属性：

```yaml
tron:
  api-key: "你的API键"
  default-private-key: "你的私钥"
  default-address: "你的TRON地址"
```

### 获取 API Key

1. 访问 [TRON Grid](https://www.trongrid.io/) 官方网站。
2. 注册或登录你的账户。
3. 在管理控制台中创建一个新的API Key。
4. 将生成的API Key复制并粘贴到 `application.yml` 文件的 `api-key` 字段。

### 设置私钥和地址

1. 使用你的TRON钱包或通过TRON网络的其他可靠工具生成一个新的私钥和地址。
2. 将私钥复制到 `default-private-key` 字段。
3. 将相应的TRON地址复制到 `default-address` 字段。

### 注意

- **保密**：API键和私钥包含敏感信息，不应公开或分享给未经授权的个人。
- **安全存储**：确保这些信息在安全的环境中存储和使用，避免潜在的安全风险。

确保完成上述配置后，项目应能够成功连接到TRON区块链，并执行所需的操作。

## 如何贡献

我们欢迎所有形式的贡献，无论是功能性改进、bug修复还是文档更新。请遵循以下步骤进行贡献：

1. Fork 仓库。
2. 创建新的分支 (`git checkout -b feature/AmazingFeature`)。
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)。
4. 推送到分支 (`git push origin feature/AmazingFeature`)。
5. 打开一个Pull Request。

## 许可证

本项目采用 [MIT 许可证](LICENSE.md)。请查看 `LICENSE.md` 文件了解详细信息。

## 联系方式

- **项目链接**: [https://github.com/avengers-777/LightningEnergy](https://github.com/avengers-777/LightningEnergy)
- **Telegram**: [@bitcyber](https://t.me/bitcyber)
