<template>
  <div class="org-page">
    <div class="container">
      <h2>机构工作台</h2>
      <p class="sub">审核课程、查看订单与收益</p>
      <div class="grid">
        <div class="card" @click="router.push('/org/courses')">
          <h3>课程审核</h3>
          <p>待审核、上架、下架</p>
        </div>
        <div class="card" @click="router.push('/org/orders')">
          <h3>机构订单</h3>
          <p>查看支付订单与分成</p>
        </div>
        <div class="card" @click="router.push('/org/stats')">
          <h3>收益统计</h3>
          <p>收入概览与趋势</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped lang="scss">
.org-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 24px 0; }
.container { max-width: 960px; margin: 0 auto; background: #fff; padding: 20px; border-radius: 12px; }
h2 { margin: 0 0 6px 0; }
.sub { color: var(--text-muted); margin-bottom: 16px; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; }
.card { border: 1px solid #eee; border-radius: 10px; padding: 16px; cursor: pointer; transition: all .2s;
  &:hover { border-color: var(--primary-color); box-shadow: 0 8px 20px rgba(0,0,0,0.05); }
  h3 { margin: 0 0 6px 0; font-size: 16px; }
  p { margin: 0; color: var(--text-muted); font-size: 12px; }
}
</style>


