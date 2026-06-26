<template>
  <div class="books-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>书籍列表</span>
          <el-button type="primary" v-if="userStore.isAdmin" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加书籍
          </el-button>
        </div>
      </template>
      <div class="search-bar">
        <el-input v-model="searchForm.search" placeholder="搜索书名、作者或ISBN" clearable style="width: 300px" @clear="handleSearch" />
        <el-select v-model="searchForm.category" placeholder="选择分类" clearable style="width: 150px; margin-left: 10px" @change="handleSearch">
          <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
        </el-select>
        <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
      </div>
      <el-table :data="books" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="title" label="书名" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="location" label="位置" width="100" />
        <el-table-column label="库存" width="120">
          <template #default="{ row }">
            <span :class="{ 'out-of-stock': row.available_quantity === 0 }">
              {{ row.available_quantity }} / {{ row.total_quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.available_quantity > 0 ? 'success' : 'danger'">
              {{ row.available_quantity > 0 ? '可借' : '已借完' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleBorrow(row)" :disabled="row.available_quantity === 0">借书</el-button>
            <el-button type="warning" size="small" v-if="userStore.isAdmin" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" v-if="userStore.isAdmin" @click="handleDelete(row)">删除</el-button>
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
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '添加书籍' : '编辑书籍'" width="500px">
      <el-form :model="bookForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="书名" prop="title">
          <el-input v-model="bookForm.title" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="bookForm.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select 
            v-model="bookForm.category" 
            placeholder="请选择或输入分类"
            filterable 
            allow-create 
            reserve-keyword
            style="width: 100%"
          >
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="bookForm.location" placeholder="如A-01-01" />
        </el-form-item>
        <el-form-item label="库存" prop="total_quantity">
          <el-input-number v-model="bookForm.total_quantity" :min="0" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="bookForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { booksApi, borrowApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const books = ref([])
const total = ref(0)

const searchForm = ref({
  search: '',
  category: '',
  page: 1,
  pageSize: 10
})

const bookForm = ref({
  isbn: '',
  title: '',
  author: '',
  category: '',
  location: '',
  total_quantity: 0,
  description: ''
})

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }]
}

// 动态获取所有分类
const categories = computed(() => {
  const catSet = new Set()
  books.value.forEach(book => {
    if (book.category) {
      catSet.add(book.category)
    }
  })
  return Array.from(catSet)
})

const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await booksApi.getList(searchForm.value)
    books.value = res.data
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.page = 1
  fetchBooks()
}

const handleAdd = () => {
  dialogType.value = 'add'
  bookForm.value = {
    isbn: '',
    title: '',
    author: '',
    category: '',
    location: '',
    total_quantity: 0,
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = row => {
  dialogType.value = 'edit'
  bookForm.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async row => {
  try {
    await ElMessageBox.confirm(`确定要删除《${row.title}》吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await booksApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchBooks()
  } catch (error) {
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (dialogType.value === 'add') {
      await booksApi.create(bookForm.value)
      ElMessage.success('添加成功')
    } else {
      await booksApi.update(bookForm.value.id, bookForm.value)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    fetchBooks()
  } finally {
    submitLoading.value = false
  }
}

const handleBorrow = async row => {
  try {
    await ElMessageBox.confirm(`确定要借阅《${row.title}》吗？借阅期限为30天。`, '借书确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await borrowApi.borrow(row.id)
    ElMessage.success('借阅成功')
    fetchBooks()
  } catch (error) {
  }
}

onMounted(() => {
  fetchBooks()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  display: flex;
  align-items: center;
}

.out-of-stock {
  color: #f56c6c;
}
</style>
