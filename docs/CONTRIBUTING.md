# 贡献指南

感谢您对 AI-ATG 项目的关注！

## 🤝 如何贡献

### 报告问题

如果您发现了bug或有功能建议：

1. 在 GitHub Issues 中搜索是否已有类似问题
2. 如果没有，创建新的 Issue
3. 详细描述问题或建议
4. 提供复现步骤（如果是bug）

### 提交代码

1. **Fork 项目**
   ```bash
   # 在GitHub上点击Fork按钮
   ```

2. **克隆到本地**
   ```bash
   git clone https://github.com/your-username/AI-ATG.git
   cd AI-ATG
   ```

3. **创建分支**
   ```bash
   git checkout -b feature/your-feature-name
   # 或
   git checkout -b fix/your-bug-fix
   ```

4. **进行开发**
   - 遵循项目代码规范
   - 添加必要的注释
   - 确保代码可以正常运行

5. **提交更改**
   ```bash
   git add .
   git commit -m "feat: 添加XXX功能"
   # 或
   git commit -m "fix: 修复XXX问题"
   ```

6. **推送到远程**
   ```bash
   git push origin feature/your-feature-name
   ```

7. **创建 Pull Request**
   - 在GitHub上创建PR
   - 描述您的更改
   - 等待代码审查

---

## 📝 代码规范

### Java代码规范

遵循 [Alibaba Java Coding Guidelines](https://github.com/alibaba/p3c)

**命名规范**:
- 类名：PascalCase
- 方法名：camelCase
- 常量：UPPER_SNAKE_CASE
- 变量：camelCase

**代码风格**:
- 缩进：4个空格
- 行长度：≤ 150字符
- 大括号：K&R风格

**注释规范**:
```java
/**
 * 方法说明
 * 
 * @param param 参数说明
 * @return 返回值说明
 */
public ReturnType methodName(ParamType param) {
    // 实现
}
```

### JavaScript/Vue代码规范

遵循项目配置的前端规范

**命名规范**:
- 组件名：PascalCase
- 方法名：camelCase
- 变量名：camelCase
- 常量：UPPER_SNAKE_CASE

**代码风格**:
- 缩进：2个空格
- 引号：单引号
- 分号：可选

### Git提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/)

**格式**:
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type类型**:
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具链

**示例**:
```
feat(ai): 添加GPT-4支持

- 集成GPT-4 API
- 添加GPT-4配置选项
- 更新AI客户端工厂

Closes #123
```

---

## 🧪 测试要求

### 单元测试

- 新功能必须包含单元测试
- 测试覆盖率 > 80%
- 使用JUnit 5和Mockito

**示例**:
```java
@Test
void testCreateProject() {
    ProjectDTO dto = new ProjectDTO();
    dto.setName("Test Project");
    
    ProjectVO result = projectService.createProject(dto, 1L);
    
    assertNotNull(result);
    assertEquals("Test Project", result.getName());
}
```

### 集成测试

- 关键流程需要集成测试
- 使用@SpringBootTest
- 测试数据库使用H2或TestContainers

---

## 📚 文档要求

### 代码文档

- 所有公共API必须有JavaDoc
- 复杂逻辑需要注释说明
- README更新（如果涉及新功能）

### 更新文档

如果您的PR包含新功能：

1. 更新 `README.md` 功能列表
2. 更新 `API_DOCUMENTATION.md`（如果添加新接口）
3. 更新 `USER_MANUAL.md`（如果影响用户使用）
4. 更新 `CHANGELOG.md` 版本日志

---

## ✅ PR检查清单

提交PR前请确认：

- [ ] 代码遵循项目规范
- [ ] 所有测试通过
- [ ] 添加了必要的注释
- [ ] 更新了相关文档
- [ ] 提交信息遵循规范
- [ ] 没有引入新的警告
- [ ] 代码可以正常运行

---

## 🔒 许可证

### 贡献许可

通过向本项目提交代码，您同意：

1. 您的贡献将在 **Apache License 2.0** 下发布
2. 您拥有贡献代码的权利
3. 您的贡献不侵犯第三方权利

### 添加许可证头部

所有新Java文件必须添加许可证头部：

```java
/*
 * Copyright 2026 James Zou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

可以从 `LICENSE_HEADER.txt` 复制。

---

## 👥 社区准则

### 行为准则

- 尊重他人
- 建设性反馈
- 专业交流
- 包容多样性

### 沟通渠道

- **GitHub Issues**: 问题报告和功能建议
- **Pull Requests**: 代码贡献
- **Email**: 18301545237@163.com

---

## 🎯 贡献方向

我们特别欢迎以下方面的贡献：

### 功能增强
- [ ] 更多AI模型支持（GPT-4、Claude等）
- [ ] UI测试执行器完整实现
- [ ] 性能测试集成（JMeter）
- [ ] 移动端适配

### 集成增强
- [ ] Jenkins集成
- [ ] GitHub集成
- [ ] Jira集成
- [ ] 钉钉/企业微信通知

### 文档改进
- [ ] 英文文档翻译
- [ ] 视频教程
- [ ] 最佳实践案例
- [ ] 常见问题补充

### 性能优化
- [ ] 分布式测试执行
- [ ] 实时监控面板
- [ ] 缓存优化
- [ ] 查询优化

---

## 🙏 致谢

感谢所有贡献者！

您的每一个贡献都让AI-ATG变得更好！

---

## 📞 联系方式

- **作者**: James Zou
- **邮箱**: 18301545237@163.com
- **许可证**: Apache License 2.0

---

**最后更新：2026-01-27**
