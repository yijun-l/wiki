<!-- views/Users.vue -->

<template>
    <a-space style="margin-bottom: 16px">
        <a-input-search v-model:value="keyword" placeholder="Search users" enter-button @search="onSearch" allowClear/>
        <a-button type="primary" @click="openCreateModal" style="width: 70px;">New</a-button>
    </a-space>

    <a-table :columns="columns" :row-key="rowKey" :data-source="users" :pagination="pagination" :loading="loading"
        @change="handleTableChange">

        <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'actions'">
                <a-space size="small">
                    <!-- Primary button for edit action -->
                    <a-button type="primary" @click="openEditModal(record)" style="width: 70px;">Edit</a-button>
                    <!-- Danger button for delete action -->
                    <a-popconfirm title="Are you sure you want to delete this user?"
                        description="This action cannot be undone." ok-text="Yes" cancel-text="No"
                        @confirm="confirm(record)">
                        <a-button type="primary" danger style="width: 70px;">Delete</a-button>
                    </a-popconfirm>
                </a-space>
            </template>
        </template>
    </a-table>

    <a-modal v-model:open="modalOpen" :title="modalMode === 'create' ? 'Create User' : 'Edit User'" @ok="submitModal"
        @cancel="modalOpen = false" :width="520" centered :maskClosable="false" okText="Confirm" cancelText="Cancel"
        :bodyStyle="{ padding: '24px 32px' }">
        <a-form :model="formModel" layout="vertical">
            <a-form-item label="Username">
                <a-input v-model:value="formModel.username" />
            </a-form-item>
            <a-form-item label="Email">
                <a-input v-model:value="formModel.email" />
            </a-form-item>
            <a-form-item label="Nickname">
                <a-input v-model:value="formModel.nickname" />
            </a-form-item>
            <a-form-item label="Avatar URL">
                <a-input v-model:value="formModel.avatarUrl" />
            </a-form-item>
            <a-form-item label="Status">
                <a-input v-model:value="formModel.status" />
            </a-form-item>
        </a-form>
    </a-modal>

</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { TablePaginationConfig, TableColumnType } from 'ant-design-vue'
import { message } from 'ant-design-vue';
import type { User, UserQueryParams } from '@/types/user'
import { listUser, updateUser, deleteUser, createUser } from '@/api/users'

// ============================================================
// Data fetching and pagination state
// ============================================================
const loading = ref<boolean>(false)
const users = ref<User[]>([])

// Pagination configuration object for table component
// Uses reactive() to make the object reactive - changes automatically trigger UI updates
const pagination = reactive({
    current: 1,      // Current page number (starts from 1)
    pageSize: 10,    // Number of items per page
    total: 0         // Total number of items (will be updated from API response)
})

// Fetch data from backend
const fetchUsers = async (username?:string) => {
    loading.value = true;
    try {
        const params: UserQueryParams = {
            page: pagination.current,
            size: pagination.pageSize,
        }
        if (username) {
            params.username = username
        }
        const { data } = await listUser(params)
        users.value = data.records
        pagination.total = data.total
    } catch (err: any) {
        message.error(err?.response?.data?.message || 'Failed to fetch users')
    } finally {
        loading.value = false
    }
}


// ============================================================
// Search the keyword
// ============================================================

const keyword = ref<string>('')
const onSearch = async () => {
    pagination.current = 1
    fetchUsers(keyword.value)
}

// ============================================================
// Table Interaction Logic
// ============================================================
const columns: TableColumnType<User>[] = [
    { title: '', dataIndex: 'avatarUrl' },
    { title: 'Username', dataIndex: 'username' },
    { title: 'Email', dataIndex: 'email' },
    { title: 'Nickname', dataIndex: 'nickname' },
    { title: 'Status', dataIndex: 'status' },
    { title: 'Actions', key: 'actions' }
]


const rowKey = (record: User) => record.id

const handleTableChange = (pager: TablePaginationConfig) => {
    pagination.current = pager.current ?? 1
    pagination.pageSize = pager.pageSize ?? 10
    fetchUsers()
}

// ============================================================
// POP Confirm Logic
// ============================================================

const confirm = async (record: User) => {
    try {
        await deleteUser(record.id)
        message.success('User deleted successfully')
        fetchUsers()
    } catch (err) {
        message.error('Failed to delete user')
    }
};


// ============================================================
// Modal and Form Logic
// ============================================================

type ModalMode = 'create' | 'edit'
const modalOpen = ref<boolean>(false)
const modalMode = ref<ModalMode>('create')

const defaultFormModel = (): User => ({
    id: 0,
    username: '',
    email: '',
    nickname: '',
    avatarUrl: '',
    status: '',
})

const formModel = reactive<User>(defaultFormModel())

const openCreateModal = () => {
    Object.assign(formModel, defaultFormModel())
    modalMode.value = 'create'
    modalOpen.value = true
}

const openEditModal = (record: User) => {
    Object.assign(formModel, record)
    modalMode.value = 'edit'
    modalOpen.value = true
}

const submitModal = async () => {
    try {
        if (modalMode.value == 'create') {
            await createUser(formModel)
            message.success('User created successfully')
        } else if (modalMode.value == 'edit') {
            await updateUser(formModel.id, formModel)
            message.success('User updated successfully')
        }
        modalOpen.value = false
        fetchUsers()
    } catch (err: any) {
        message.error(err?.response?.data?.message || 'Operation failed')
    }
}

// ============================================================
// Lifecycle Hooks
// ============================================================
onMounted(() => {
    fetchUsers()
})
</script>