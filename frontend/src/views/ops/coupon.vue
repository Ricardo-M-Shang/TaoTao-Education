<template>
  <div class="ops-coupon-page">
    <div class="container">
      <div class="header">
        <h2>优惠券管理</h2>
        <el-button type="primary" @click="handleCreate">新建优惠券</el-button>
      </div>

      <!-- Filter -->
      <div class="filter-bar">
        <el-input 
          v-model="query.name" 
          placeholder="搜索优惠券名称" 
          clearable 
          @clear="load" 
          @keyup.enter="load" 
          style="width: 200px" 
        />
        <el-select v-model="query.status" placeholder="状态" clearable @change="load" style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-select v-model="query.type" placeholder="类型" clearable @change="load" style="width: 120px">
          <el-option label="满减券" :value="1" />
          <el-option label="折扣券" :value="2" />
        </el-select>
        <el-button @click="load">查询</el-button>
      </div>

      <!-- List -->
      <el-table :data="tableData" v-loading="loading" style="width: 100%; margin-top: 20px" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? '' : 'warning'">{{ row.type === 1 ? '满减券' : '折扣券' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="面额/折扣" width="120">
          <template #default="{ row }">
            <span v-if="row.type === 1">¥{{ row.discountAmount }}</span>
            <span v-else>{{ (row.discountRate * 10).toFixed(1) }}折</span>
          </template>
        </el-table-column>
        <el-table-column label="门槛" width="120">
          <template #default="{ row }">
            {{ row.thresholdAmount ? `满 ¥${row.thresholdAmount}` : '无门槛' }}
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="300">
          <template #default="{ row }">
            {{ formatTime(row.validFrom) }} ~ {{ formatTime(row.validTo) }}
          </template>
        </el-table-column>
        <el-table-column label="抢购时间(秒杀)" width="300">
          <template #default="{ row }">
            <div v-if="row.grabStartTime">
              {{ formatTime(row.grabStartTime) }} ~ {{ formatTime(row.grabEndTime) }}
            </div>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column label="发放/剩余" width="120">
          <template #default="{ row }">
            {{ row.total }} / {{ row.stock }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              :active-value="1" 
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该优惠券吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          layout="total, prev, pager, next"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>

      <!-- Create/Edit Dialog -->
      <el-dialog v-model="dialogVisible" title="新建优惠券" width="600px" :close-on-click-modal="false">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="优惠券名称" prop="name">
            <el-input v-model="form.name" placeholder="如：双11大促满减券" />
          </el-form-item>
          <el-form-item label="类型" prop="type">
            <el-radio-group v-model="form.type">
              <el-radio :label="1">满减券</el-radio>
              <el-radio :label="2">折扣券</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="面额" prop="discountAmount" v-if="form.type === 1">
            <el-input-number v-model="form.discountAmount" :min="1" :precision="0" placeholder="金额" />
            <span class="suffix">元</span>
          </el-form-item>
          <el-form-item label="折扣" prop="discountRate" v-if="form.type === 2">
            <el-input-number v-model="form.discountRate" :min="0.1" :max="0.99" :step="0.1" :precision="2" placeholder="0-1之间" />
            <span class="suffix">（0.8表示8折）</span>
          </el-form-item>
          <el-form-item label="使用门槛" prop="thresholdAmount">
            <el-input-number v-model="form.thresholdAmount" :min="0" :precision="0" placeholder="0表示无门槛" />
            <span class="suffix">元</span>
          </el-form-item>
          <el-form-item label="发放总量" prop="total">
            <el-input-number v-model="form.total" :min="1" :step="100" />
            <span class="suffix">张</span>
          </el-form-item>
          <el-form-item label="每人限领" prop="limitPerUser">
            <el-input-number v-model="form.limitPerUser" :min="1" :max="100" />
            <span class="suffix">张</span>
          </el-form-item>
          <el-form-item label="有效期" prop="validRange">
            <el-date-picker
              v-model="form.validRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          
          <el-divider content-position="left">秒杀设置（可选）</el-divider>
          <el-form-item label="秒杀时间" prop="grabRange">
             <el-date-picker
              v-model="form.grabRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="抢购开始"
              end-placeholder="抢购结束"
              value-format="YYYY-MM-DD HH:mm:ss"
              clearable
            />
            <div class="form-tip">设置此项后，该优惠券将出现在秒杀频道</div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定创建</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCouponList, createCoupon, updateCouponStatus, deleteCoupon } from '@/api/ops'
import { ElMessage, FormInstance } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref<FormInstance>()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: undefined,
  type: undefined
})

const form = reactive({
  name: '',
  type: 1,
  discountAmount: 10,
  discountRate: 0.8,
  thresholdAmount: 100,
  total: 1000,
  limitPerUser: 1,
  validRange: [],
  grabRange: []
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  validRange: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

function formatTime(t: string) {
  return t ? t.replace('T', ' ') : ''
}

async function load() {
  loading.value = true
  try {
    const res = await getCouponList(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(p: number) {
  query.pageNum = p
  load()
}

function handleCreate() {
  form.name = ''
  form.type = 1
  form.discountAmount = 10
  form.discountRate = 0.8
  form.thresholdAmount = 100
  form.total = 1000
  form.limitPerUser = 1
  form.validRange = []
  form.grabRange = []
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload: any = { ...form }
      if (form.validRange?.length === 2) {
        payload.validFrom = form.validRange[0]
        payload.validTo = form.validRange[1]
      }
      if (form.grabRange?.length === 2) {
        payload.grabStartTime = form.grabRange[0]
        payload.grabEndTime = form.grabRange[1]
      }
      delete payload.validRange
      delete payload.grabRange
      
      await createCoupon(payload)
      ElMessage.success('创建成功')
      dialogVisible.value = false
      load()
    } catch (e) {
      console.error(e)
    } finally {
      submitting.value = false
    }
  })
}

async function handleStatusChange(row: any) {
  try {
    await updateCouponStatus(row.id, row.status)
    ElMessage.success('状态已更新')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1 // revert
  }
}

async function handleDelete(row: any) {
  try {
    await deleteCoupon(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    console.error(e)
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.ops-coupon-page {
  background: var(--bg-color);
  min-height: calc(100vh - 90px);
  padding: 20px 0;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  padding: 24px;
  border-radius: 12px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  h2 { margin: 0; }
}
.filter-bar {
  display: flex;
  gap: 12px;
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
}
.suffix {
  margin-left: 8px;
  color: #666;
}
.pager {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.form-tip {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}
.text-gray { color: #ccc; }
</style>
