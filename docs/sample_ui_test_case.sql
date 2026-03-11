-- 示例UI测试用例

-- 插入一个简单的UI测试用例：百度搜索测试
INSERT INTO test_case (
    project_id,
    title,
    type,
    priority,
    level,
    preconditions,
    steps,
    expected_result,
    status,
    source,
    created_by,
    created_time
) VALUES (
    1,
    'UI测试-百度搜索功能',
    'ui',
    'P2',
    'medium',
    '浏览器已安装并可正常访问百度',
    '[
        {
            "action": "open",
            "input": "https://www.baidu.com"
        },
        {
            "action": "wait",
            "timeout": 2
        },
        {
            "action": "assertTitle",
            "input": "百度"
        },
        {
            "action": "input",
            "locator": "id",
            "value": "kw",
            "input": "Selenium自动化测试"
        },
        {
            "action": "click",
            "locator": "id",
            "value": "su"
        },
        {
            "action": "wait",
            "timeout": 3
        },
        {
            "action": "assertUrl",
            "input": "wd=Selenium"
        }
    ]',
    '能够成功搜索并跳转到搜索结果页',
    'approved',
    'manual',
    1,
    NOW()
);

-- 插入一个登录测试用例（需要替换为实际的登录页面URL）
INSERT INTO test_case (
    project_id,
    title,
    type,
    priority,
    level,
    preconditions,
    steps,
    expected_result,
    status,
    source,
    created_by,
    created_time
) VALUES (
    1,
    'UI测试-用户登录功能',
    'ui',
    'P1',
    'high',
    '系统已部署，用户账号已创建',
    '[
        {
            "action": "open",
            "input": "http://localhost:3000/login"
        },
        {
            "action": "wait",
            "timeout": 2
        },
        {
            "action": "input",
            "locator": "id",
            "value": "username",
            "input": "admin"
        },
        {
            "action": "input",
            "locator": "id",
            "value": "password",
            "input": "Admin@123"
        },
        {
            "action": "click",
            "locator": "css",
            "value": "button[type=submit]"
        },
        {
            "action": "wait",
            "timeout": 3
        },
        {
            "action": "assertUrl",
            "input": "/dashboard"
        }
    ]',
    '能够成功登录并跳转到首页',
    'approved',
    'manual',
    1,
    NOW()
);

-- 插入一个表单填写测试用例
INSERT INTO test_case (
    project_id,
    title,
    type,
    priority,
    level,
    preconditions,
    steps,
    expected_result,
    status,
    source,
    created_by,
    created_time
) VALUES (
    1,
    'UI测试-注册表单填写',
    'ui',
    'P2',
    'medium',
    '访问注册页面',
    '[
        {
            "action": "open",
            "input": "http://localhost:3000/register"
        },
        {
            "action": "wait",
            "timeout": 1
        },
        {
            "action": "input",
            "locator": "name",
            "value": "username",
            "input": "testuser"
        },
        {
            "action": "input",
            "locator": "name",
            "value": "email",
            "input": "test@example.com"
        },
        {
            "action": "input",
            "locator": "name",
            "value": "password",
            "input": "Test@123"
        },
        {
            "action": "input",
            "locator": "name",
            "value": "confirmPassword",
            "input": "Test@123"
        },
        {
            "action": "click",
            "locator": "xpath",
            "value": "//button[contains(text(), \"注册\")]"
        },
        {
            "action": "wait",
            "timeout": 2
        },
        {
            "action": "assertText",
            "locator": "css",
            "value": ".message",
            "input": "注册成功"
        }
    ]',
    '能够成功填写并提交注册表单',
    'draft',
    'manual',
    1,
    NOW()
);

SELECT '✅ 示例UI测试用例已创建！' AS message;
SELECT '📝 请根据实际情况修改URL和元素定位器' AS note;
