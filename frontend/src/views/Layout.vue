<template>
  <el-container class="layout">
    <el-header>
      <div class="header-left">
        <h2>📚 图书馆管理系统</h2>
      </div>
      <div class="header-right">
        <span>欢迎，{{ userStore.userInfo?.name }}</span>
        <el-button type="text" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu :default-active="activeMenu" router>
          <el-menu-item index="/books">
            <el-icon><Reading /></el-icon>
            <span>书籍管理</span>
          </el-menu-item>
          <el-menu-item index="/borrow">
            <el-icon><Tickets /></el-icon>
            <span>借阅记录</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Reading, Tickets, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.el-aside {
  background: #304156;
}

.el-menu {
  border-right: none;
}

.el-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
