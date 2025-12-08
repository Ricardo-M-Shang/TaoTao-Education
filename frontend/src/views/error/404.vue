<template>
  <div class="not-found">
    <div class="content">
      <div class="code">404</div>
      <h2>页面走丢了</h2>
      <p>抱歉，您访问的页面不存在</p>
      <el-button type="primary" round @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #6366f1, #8b5cf6); }
.content { text-align: center; color: #fff; }
.code { font-size: 120px; font-weight: 800; opacity: 0.2; line-height: 1; }
h2 { font-size: 24px; margin: -30px 0 10px; }
p { font-size: 14px; opacity: 0.8; margin-bottom: 24px; }
</style>
