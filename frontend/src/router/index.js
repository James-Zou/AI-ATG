import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'atgbot',
          name: 'ATGBot',
          component: () => import('@/views/atgbot/ATGBot.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'skills/list',
          name: 'SkillsList',
          component: () => import('@/views/skills/SkillsList.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'user-manage',
          name: 'UserManage',
          component: () => import('@/views/UserManage.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'requirement/list',
          name: 'RequirementList',
          component: () => import('@/views/requirement/RequirementList.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'requirement/create',
          name: 'RequirementCreate',
          component: () => import('@/views/requirement/RequirementForm.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'requirement/edit/:id',
          name: 'RequirementEdit',
          component: () => import('@/views/requirement/RequirementForm.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'requirement/detail/:id',
          name: 'RequirementDetail',
          component: () => import('@/views/requirement/RequirementDetail.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'testcase/list',
          name: 'TestCaseList',
          component: () => import('@/views/testcase/TestCaseList.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'testcase/create',
          name: 'TestCaseCreate',
          component: () => import('@/views/testcase/TestCaseForm.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'testcase/edit/:id',
          name: 'TestCaseEdit',
          component: () => import('@/views/testcase/TestCaseForm.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'testcase/detail/:id',
          name: 'TestCaseDetail',
          component: () => import('@/views/testcase/TestCaseDetail.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'testsuite/manage',
          name: 'TestSuiteManage',
          component: () => import('@/views/testcase/TestSuiteManage.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'interface/list',
          name: 'InterfaceList',
          component: () => import('@/views/interface/InterfaceList.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'interface/create',
          name: 'InterfaceCreate',
          component: () => import('@/views/interface/InterfaceForm.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'interface/edit/:id',
          name: 'InterfaceEdit',
          component: () => import('@/views/interface/InterfaceForm.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'ai/generate',
          name: 'AiGenerate',
          component: () => import('@/views/ai/AiGenerate.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'ai/config',
          name: 'AiConfig',
          component: () => import('@/views/ai/AiConfig.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'execution/list',
          name: 'ExecutionList',
          component: () => import('@/views/execution/ExecutionList.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'execution/detail/:id',
          name: 'ExecutionDetail',
          component: () => import('@/views/execution/ExecutionDetail.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'environment/list',
          name: 'EnvironmentList',
          component: () => import('@/views/environment/EnvironmentList.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'report/list',
          name: 'ReportList',
          component: () => import('@/views/report/ReportList.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'report/detail/:id',
          name: 'ReportDetail',
          component: () => import('@/views/report/ReportDetail.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'report/statistics',
          name: 'ReportStatistics',
          component: () => import('@/views/report/ReportStatistics.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'gitlab/config',
          name: 'GitlabConfig',
          component: () => import('@/views/gitlab/GitlabConfig.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'gitlab/webhook-records',
          name: 'WebhookRecords',
          component: () => import('@/views/gitlab/WebhookRecords.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'project/list',
          name: 'ProjectList',
          component: () => import('@/views/project/ProjectList.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'product-guide',
          name: 'ProductGuide',
          component: () => import('@/views/ProductGuide.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'help/ui-test',
          name: 'UITestHelp',
          component: () => import('@/views/help/UITestHelp.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'help/downloads',
          name: 'DownloadCenter',
          component: () => import('@/views/help/DownloadCenter.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'api-key/manage',
          name: 'ApiKeyManage',
          component: () => import('@/views/ApiKeyManage.vue'),
          meta: { requiresAuth: true }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 如果页面需要认证
  if (to.meta.requiresAuth) {
    // 检查是否已登录
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
  }
  
  // 如果已登录，访问登录页则跳转到首页
  if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
