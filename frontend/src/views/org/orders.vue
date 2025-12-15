<template>
  <div class="org-orders-page">
    <div class="container">
      <div class="header">
        <div>
          <h2>机构订单</h2>
          <p class="sub">查看支付订单与分成</p>
        </div>
        <div class="filters">
          <el-select v-model="query.teacherId" placeholder="讲师" size="small" class="w140" clearable @change="onTeacherChange">
            <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" size="small" class="w140" clearable @change="onStatusChange">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已取消" :value="2" />
            <el-option label="已退款" :value="3" />
          </el-select>
        </div>
      </div>

      <el-table :data="orders" size="small" v-loading="loading" border class="table">
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="courseTitle" label="课程" min-width="140" />
        <el-table-column prop="teacherName" label="讲师" width="100" />
        <el-table-column prop="payAmount" label="实付" width="90" />
        <el-table-column prop="orgIncome" label="机构分成" width="100" />
        <el-table-column prop="platformIncome" label="平台分成" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="pager" v-if="page.total">
        <el-pagination
          v-model:current-page="query.pageNum"
          :page-size="query.pageSize"
          :total="page.total"
          layout="prev, pager, next"
          @current-change="load"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { getOrgOrders, getOrgTeachers } from '@/api/org'
import type { OrderInfo, OrderListResult } from '@/types/order'
type TeacherOption = { id: number | string; name: string }

const loading = ref(false)
const orders = ref<OrderInfo[]>([])
const page = reactive({ total: 0 })

const query = reactive({
  status: undefined as number | undefined,
  teacherId: undefined as string | number | undefined,
  pageNum: 1,
  pageSize: 10
})
const teachers = ref<TeacherOption[]>([])

const statusText = (s?: number) => ({ 0: '待支付', 1: '已支付', 2: '已取消', 3: '已退款' }[s || 0] || '')
const statusType = (s?: number) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s || 0] as any)

function formatDate(time?: string) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

async function load() {
  loading.value = true
  try {
    const res = await getOrgOrders(query)
    const data = res.data as OrderListResult
    orders.value = data.records || []
    page.total = data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function onStatusChange() {
  query.pageNum = 1
  load()
}

function onTeacherChange() {
  query.pageNum = 1
  load()
}

async function loadTeachers() {
  try {
    const res = await getOrgTeachers()
    teachers.value = res.data || []
  } catch (e) { console.error(e) }
}

onMounted(() => { loadTeachers(); load() })
</script>

<style scoped lang="scss">
.org-orders-page { background: var(--bg-color); min-height: calc(100vh - 90px); padding: 20px 0; }
.container { max-width: 1000px; margin: 0 auto; padding: 16px; background: #fff; border-radius: 12px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
  h2 { margin: 0; }
  .sub { color: var(--text-muted); font-size: 12px; }
  .w140 { width: 140px; }
}
.table { border-radius: 10px; overflow: hidden; }
.pager { margin-top: 16px; display: flex; justify-content: center; }
</style>


