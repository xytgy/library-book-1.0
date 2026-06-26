<template>
  <div class="borrow-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>借阅记录</span>
          <el-select v-model="statusFilter" placeholder="筛选状态" clearable @change="fetchRecords" style="width: 150px">
            <el-option label="全部" :value="null" />
            <el-option label="借阅中" value="borrowing" />
            <el-option label="已归还" value="returned" />
          </el-select>
        </div>
      </template>
      <el-table :data="records" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="书名" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column v-if="userStore.isAdmin" prop="user_name" label="借阅人" width="120" />
        <el-table-column prop="borrow_date" label="借阅日期" width="180">
          <template #default="{ row }">
            {{ formatDate(row.borrow_date) }}
          </template>
        </el-table-column>
        <el-table-column prop="due_date" label="应还日期" width="180">
          <template #default="{ row }">
            <span :class="{ 'overdue': row.status === 'borrowing' && new Date(row.due_date) < new Date() }">
              {{ formatDate(row.due_date) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="return_date" label="归还日期" width="180">
          <template #default="{ row }">
            {{ row.return_date ? formatDate(row.return_date) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'borrowing' ? 'warning' : 'success'">
              {{ row.status === 'borrowing' ? '借阅中' : '已归还' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" v-if="!userStore.isAdmin">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              v-if="row.status === 'borrowing'"
              @click="handleReturn(row)"
            >
              归还
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="searchForm.page"
        v-model:page-size="searchForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: center"
        @size-change="fetchRecords"
        @current-change="fetchRecords"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { borrowApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const records = ref([])
const total = ref(0)
const statusFilter = ref(null)

const searchForm = ref({
  page: 1,
  pageSize: 10
})

const formatDate = date => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const params = { ...searchForm.value, status: statusFilter.value }
    const res = userStore.isAdmin 
      ? await borrowApi.getAllRecords(params)
      : await borrowApi.getMyRecords(params)
    records.value = res.data
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleReturn = async row => {
  try {
    await ElMessageBox.confirm(`确定要归还《${row.title}》吗？`, '还书确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await borrowApi.return(row.id)
    ElMessage.success('归还成功')
    fetchRecords()
  } catch (error) {
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overdue {
  color: #f56c6c;
  font-weight: bold;
}
</style>
