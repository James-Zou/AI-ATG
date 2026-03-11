# AI-ATG 性能优化指南

## 📊 概述

本指南介绍AI-ATG平台的性能优化策略和最佳实践。

---

## 🗄️ 数据库优化

### 1. 索引优化

**已创建的索引** (V10_Create_Indexes.sql)：

#### 用户表索引
```sql
CREATE INDEX idx_user_username ON `user`(username);
CREATE INDEX idx_user_email ON `user`(email);
CREATE INDEX idx_user_status ON `user`(status);
```

#### 项目表索引
```sql
CREATE INDEX idx_project_created_by ON project(created_by);
CREATE INDEX idx_project_status ON project(status);
CREATE INDEX idx_project_created_time ON project(created_time);
```

#### 需求表索引
```sql
CREATE INDEX idx_requirement_project_id ON requirement(project_id);
CREATE INDEX idx_requirement_status ON requirement(status);
CREATE INDEX idx_requirement_priority ON requirement(priority);
CREATE INDEX idx_requirement_created_time ON requirement(created_time);
```

#### 测试用例表索引
```sql
CREATE INDEX idx_testcase_project_id ON test_case(project_id);
CREATE INDEX idx_testcase_suite_id ON test_case(suite_id);
CREATE INDEX idx_testcase_requirement_id ON test_case(requirement_id);
CREATE INDEX idx_testcase_priority ON test_case(priority);
CREATE INDEX idx_testcase_type ON test_case(type);
```

#### 复合索引
```sql
CREATE INDEX idx_requirement_project_status ON requirement(project_id, status);
CREATE INDEX idx_testcase_project_suite ON test_case(project_id, suite_id);
CREATE INDEX idx_execution_project_status ON test_execution(project_id, status);
```

### 2. 查询优化

#### 使用分页查询
```java
// 避免全表扫描
Page<Project> page = new Page<>(pageNum, pageSize);
projectMapper.selectPage(page, wrapper);
```

#### 避免N+1查询
```java
// 批量查询
List<Long> ids = projects.stream()
    .map(Project::getId)
    .collect(Collectors.toList());
List<ProjectMember> members = memberMapper.selectBatchIds(ids);
```

#### 使用索引字段查询
```java
// 使用已建索引的字段
LambdaQueryWrapper<Requirement> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Requirement::getProjectId, projectId)  // 使用索引
       .eq(Requirement::getStatus, status);       // 使用索引
```

### 3. 数据库配置优化

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # 最大连接数
      minimum-idle: 5              # 最小空闲连接
      connection-timeout: 30000    # 连接超时
      idle-timeout: 600000         # 空闲超时
      max-lifetime: 1800000        # 最大生命周期
```

---

## 🚀 Redis 缓存优化

### 1. 缓存配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: Aiatg123456
    timeout: 5000
    lettuce:
      pool:
        max-active: 8
        max-wait: -1
        max-idle: 8
        min-idle: 0
  
  cache:
    type: redis
    redis:
      time-to-live: 3600000      # 1小时
      cache-null-values: false
```

### 2. 使用缓存注解

#### @Cacheable - 查询缓存
```java
@Cacheable(value = "user", key = "#id")
public User getUserById(Long id) {
    return userMapper.selectById(id);
}
```

#### @CacheEvict - 清除缓存
```java
@CacheEvict(value = "user", key = "#user.id")
public void updateUser(User user) {
    userMapper.updateById(user);
}
```

#### @CachePut - 更新缓存
```java
@CachePut(value = "user", key = "#result.id")
public User createUser(User user) {
    userMapper.insert(user);
    return user;
}
```

### 3. 缓存策略

**推荐缓存的数据**：
- ✅ 用户信息
- ✅ 系统配置
- ✅ AI配置
- ✅ 项目信息
- ✅ 字典数据

**不推荐缓存的数据**：
- ❌ 测试执行结果（频繁变化）
- ❌ 操作日志（实时性要求高）
- ❌ Webhook记录（实时性要求高）

---

## ⚡ 应用层优化

### 1. 异步处理

**已实现的异步操作**：
```java
@Async
public void executeTest(Long executionId) {
    // 异步执行测试
}

@Async
public void processWebhook(WebhookPayload payload) {
    // 异步处理Webhook
}

@Async
public void logOperation(Long userId, String operation) {
    // 异步记录日志
}
```

### 2. 批量操作

```java
// 批量插入
testCaseMapper.insertBatchSomeColumn(testCases);

// 批量查询
List<TestCase> cases = testCaseMapper.selectBatchIds(ids);

// 批量更新
UpdateWrapper<TestCase> wrapper = new UpdateWrapper<>();
wrapper.in("id", ids).set("status", newStatus);
testCaseMapper.update(null, wrapper);
```

### 3. 连接池优化

**Tomcat线程池**：
```yaml
server:
  tomcat:
    threads:
      max: 200           # 最大线程数
      min-spare: 10      # 最小空闲线程
    accept-count: 100    # 等待队列长度
```

---

## 🎨 前端性能优化

### 1. 路由懒加载

```javascript
const routes = [
  {
    path: '/requirement',
    component: () => import('../views/requirement/RequirementList.vue')
  }
]
```

### 2. 组件优化

#### 使用v-show代替v-if
```vue
<!-- 频繁切换使用v-show -->
<div v-show="isVisible">内容</div>
```

#### 使用computed缓存
```javascript
const filteredList = computed(() => {
  return list.value.filter(item => item.status === 1)
})
```

#### 列表虚拟滚动
```vue
<!-- 大列表使用虚拟滚动 -->
<el-table :data="largeList" height="500">
  <!-- ... -->
</el-table>
```

### 3. 图表优化

```javascript
// ECharts按需引入
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'

use([CanvasRenderer, PieChart, LineChart])
```

### 4. 打包优化

```javascript
// vite.config.js
export default {
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'echarts': ['echarts']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  }
}
```

---

## 📊 监控和调优

### 1. 慢查询监控

```yaml
# MySQL配置
slow_query_log = ON
slow_query_log_file = /var/log/mysql/slow-query.log
long_query_time = 2  # 2秒以上的查询
```

### 2. 接口性能监控

**使用AOP记录接口耗时**：
```java
@Aspect
@Component
public class PerformanceAspect {
    @Around("@annotation(Operation)")
    public Object logPerformance(ProceedingJoinPoint joinPoint) {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;
        
        if (duration > 1000) {
            log.warn("慢接口: {} 耗时: {}ms", 
                joinPoint.getSignature(), duration);
        }
        return result;
    }
}
```

### 3. JVM调优

```bash
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -jar aiatg-backend.jar
```

---

## 📈 性能指标

### 目标性能指标

| 操作 | 目标响应时间 | 优化方案 |
|------|-------------|---------|
| 用户登录 | < 500ms | JWT缓存 + 索引 |
| 列表查询 | < 300ms | 分页 + 索引 + 缓存 |
| 详情查询 | < 200ms | 缓存 + 索引 |
| 创建操作 | < 500ms | 异步日志 |
| AI生成 | < 10s | 异步处理 + 超时控制 |
| 测试执行 | 异步 | 后台队列 + 进度推送 |
| 报告生成 | < 3s | 数据聚合优化 |
| 文件上传 | < 5s | MinIO + 分片上传 |

### 并发支持

- **预期并发**: 100+ 用户
- **峰值并发**: 500+ 用户
- **数据库连接池**: 20个连接
- **Redis连接池**: 8个连接

---

## 🔧 故障排查

### 数据库性能问题

**问题现象**：
- 查询缓慢
- 接口超时

**排查步骤**：
1. 查看慢查询日志
2. 使用EXPLAIN分析查询
3. 检查索引使用情况
4. 查看数据库连接数

**优化方案**：
1. 添加缺失的索引
2. 优化查询语句
3. 增加数据库连接池
4. 定期清理历史数据

### Redis缓存问题

**问题现象**：
- 缓存命中率低
- 内存占用过高

**排查步骤**：
1. 查看缓存命中率
2. 检查缓存过期时间
3. 查看Redis内存使用

**优化方案**：
1. 调整缓存过期时间
2. 优化缓存键设计
3. 清理无用缓存
4. 增加Redis内存

### 应用性能问题

**问题现象**：
- CPU占用高
- 内存占用高
- 响应时间长

**排查步骤**：
1. 查看线程dump
2. 查看堆内存dump
3. 使用性能分析工具

**优化方案**：
1. 优化代码逻辑
2. 增加JVM内存
3. 调整线程池参数
4. 使用异步处理

---

## 💡 优化建议

### 短期优化（立即可做）

1. ✅ **启用索引**：执行V10索引脚本
2. ✅ **启用缓存**：配置Redis缓存
3. ✅ **分页查询**：所有列表使用分页
4. ✅ **异步处理**：耗时操作异步执行

### 中期优化（1-2周）

1. **慢查询优化**：分析并优化慢查询
2. **缓存策略**：细化缓存粒度
3. **代码优化**：重构性能瓶颈代码
4. **监控告警**：添加性能监控

### 长期优化（1个月+）

1. **分布式缓存**：Redis集群
2. **读写分离**：主从数据库
3. **消息队列**：RabbitMQ异步处理
4. **分布式执行**：Selenium Grid扩展

---

## 📝 性能测试

### JMeter测试脚本

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan>
  <ThreadGroup>
    <stringProp name="ThreadGroup.num_threads">100</stringProp>
    <stringProp name="ThreadGroup.ramp_time">10</stringProp>
    <stringProp name="ThreadGroup.duration">60</stringProp>
  </ThreadGroup>
  
  <HTTPSamplerProxy>
    <stringProp name="HTTPSampler.domain">localhost</stringProp>
    <stringProp name="HTTPSampler.port">8080</stringProp>
    <stringProp name="HTTPSampler.path">/api/testcase/list</stringProp>
  </HTTPSamplerProxy>
</jmeterTestPlan>
```

### 性能测试场景

1. **并发登录测试**
   - 并发用户：100
   - 目标响应时间：< 500ms

2. **列表查询测试**
   - 并发请求：200
   - 目标响应时间：< 300ms

3. **AI生成测试**
   - 并发请求：10
   - 目标响应时间：< 10s

4. **测试执行测试**
   - 并发执行：50
   - 异步处理，不阻塞

---

## 🎯 性能目标

### 响应时间目标

| 操作类型 | 目标时间 | 可接受时间 |
|---------|---------|-----------|
| 简单查询 | < 100ms | < 300ms |
| 复杂查询 | < 300ms | < 1s |
| 写入操作 | < 200ms | < 500ms |
| AI生成 | < 10s | < 30s |
| 报告生成 | < 3s | < 5s |

### 吞吐量目标

- **QPS**: 1000+ (单机)
- **并发用户**: 500+
- **数据量**: 百万级记录

### 资源使用目标

- **CPU使用率**: < 70%
- **内存使用率**: < 80%
- **磁盘IO**: < 50%

---

## 🛠️ 优化工具

### 后端监控

1. **Spring Boot Actuator**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
```

2. **Arthas** - Java诊断工具
```bash
java -jar arthas-boot.jar
```

### 数据库监控

1. **MySQL慢查询日志**
2. **EXPLAIN分析**
3. **Performance Schema**

### 前端监控

1. **Chrome DevTools**
   - Performance面板
   - Network面板
   - Lighthouse

2. **Vue DevTools**
   - 组件性能分析
   - 渲染时间分析

---

## 📚 参考资源

- [Spring Boot性能优化](https://spring.io/guides)
- [MySQL索引优化](https://dev.mysql.com/doc/)
- [Redis最佳实践](https://redis.io/docs/)
- [Vue性能优化](https://vuejs.org/guide/best-practices/performance.html)

---

## 🎊 总结

**优化要点**：

1. ✅ **数据库索引**：提升查询速度
2. ✅ **Redis缓存**：减少数据库压力
3. ✅ **异步处理**：提升用户体验
4. ✅ **分页查询**：避免大数据量问题
5. ✅ **批量操作**：减少网络开销

**持续优化**：

- 📊 定期监控性能指标
- 🔍 分析慢查询和瓶颈
- 📈 根据业务增长调整配置
- 🛠️ 持续优化代码和架构

---

**最后更新：2026-01-27**

**版本：v1.0**
