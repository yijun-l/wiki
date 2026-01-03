<template>

    <a-space style="margin-bottom: 16px">
        <a-input-search v-model:value="keyword" placeholder="Search ebooks" enter-button @search="onSearch" allowClear/>
        <a-button type="primary" @click="openCreateModal" style="width: 70px;">New</a-button>
    </a-space>

    <!-- 
    a-table - Ant Design Vue table component
        :columns - Table column definitions (reactive binding with v-bind shorthand)
        :row-key - Unique key for each row (function returns record.id)
        :data-source - Data array for table rows (reactive binding)
        :pagination - Pagination config object (reactive binding)
        :loading - Boolean loading state (reactive binding)
        @change - Event handler for table changes (v-on shorthand)
    -->
    <a-table :columns="columns" :row-key="rowKey" :data-source="ebooks" :pagination="pagination" :loading="loading"
        @change="handleTableChange">

        <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'actions'">
                <a-space size="small">
                    <!-- Primary button for edit action -->
                    <a-button type="primary" @click="openEditModal(record)" style="width: 70px;">Edit</a-button>
                    <!-- Danger button for delete action -->
                    <a-popconfirm title="Are you sure you want to delete this ebook?"
                        description="This action cannot be undone." ok-text="Yes" cancel-text="No"
                        @confirm="confirm(record)">
                        <a-button type="primary" danger style="width: 70px;">Delete</a-button>
                    </a-popconfirm>
                </a-space>
            </template>
        </template>
    </a-table>

    <a-modal v-model:open="modalOpen" :title="modalMode === 'create' ? 'Create Ebook' : 'Edit Ebook'" @ok="submitModal"
        @cancel="modalOpen = false" :width="520" centered :maskClosable="false" okText="Confirm" cancelText="Cancel"
        :bodyStyle="{ padding: '24px 32px' }">
        <a-form :model="formModel" layout="vertical">
            <a-form-item label="Name">
                <a-input v-model:value="formModel.name" />
            </a-form-item>
            <a-row :gutter="16">
                <a-col :span="12">
                    <a-form-item label="Category 1 ID">
                        <a-input v-model:value="formModel.cat1Id" />
                    </a-form-item>
                </a-col>
                <a-col :span="12">
                    <a-form-item label="Category 2 ID">
                        <a-input v-model:value="formModel.cat2Id" />
                    </a-form-item>
                </a-col>
            </a-row>
            <a-form-item label="Description">
                <a-input v-model:value="formModel.descText" />
            </a-form-item>
            <a-form-item label="Cover URL">
                <a-input v-model:value="formModel.coverUrl" />
            </a-form-item>
            <a-form-item label="Document URL">
                <a-input v-model:value="formModel.docUrl" />
            </a-form-item>

            <a-row :gutter="16">
                <a-col :span="12">
                    <a-form-item label="Document Type">
                        <a-input v-model:value="formModel.docType" />
                    </a-form-item>
                </a-col>
                <a-col :span="12">
                    <a-form-item label="Version">
                        <a-input v-model:value="formModel.version" />
                    </a-form-item>
                </a-col>
            </a-row>
        </a-form>
    </a-modal>

</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { TablePaginationConfig, TableColumnType } from 'ant-design-vue'
import { message } from 'ant-design-vue';
import type { Ebook, EbookQueryParams } from '@/types/ebook'
import { listEbook, updateEbook, deleteEbook, createEbook } from '@/api/ebooks'

// ============================================================
// Data fetching and pagination state
// ============================================================
const loading = ref<boolean>(false)
const ebooks = ref<Ebook[]>([])

// Pagination configuration object for table component
// Uses reactive() to make the object reactive - changes automatically trigger UI updates
const pagination = reactive({
    current: 1,      // Current page number (starts from 1)
    pageSize: 10,    // Number of items per page
    total: 0         // Total number of items (will be updated from API response)
})

// Fetch data from backend
const fetchEbooks = async (name?:string) => {
    loading.value = true;
    try {
        const params: EbookQueryParams = {
            page: pagination.current,
            size: pagination.pageSize,
        }
        if (name) {
            params.name = name
        }
        const { data } = await listEbook(params)
        ebooks.value = data.records
        pagination.total = data.total
    } catch (err: any) {
        message.error(err?.response?.data?.message || 'Failed to fetch books')
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
    fetchEbooks(keyword.value)
}

// ============================================================
// Table Interaction Logic
// ============================================================
const columns: TableColumnType<Ebook>[] = [
    { title: 'Name', dataIndex: 'name' },
    { title: 'Version', dataIndex: 'version' },
    { title: 'Views', dataIndex: 'views' },
    { title: 'Likes', dataIndex: 'likes' },
    { title: 'Actions', key: 'actions' }
]


const rowKey = (record: Ebook) => record.id

const handleTableChange = (pager: TablePaginationConfig) => {
    pagination.current = pager.current ?? 1
    pagination.pageSize = pager.pageSize ?? 10
    fetchEbooks()
}

// ============================================================
// POP Confirm Logic
// ============================================================

const confirm = async (record: Ebook) => {
    try {
        await deleteEbook(record.id)
        message.success('Ebook deleted successfully')
        fetchEbooks()
    } catch (err) {
        message.error('Failed to delete ebook')
    }
};


// ============================================================
// Modal and Form Logic
// ============================================================

type ModalMode = 'create' | 'edit'
const modalOpen = ref<boolean>(false)
const modalMode = ref<ModalMode>('create')

const defaultFormModel = (): Ebook => ({
    id: 0,
    name: '',
    cat1Id: 0,
    cat2Id: 0,
    descText: '',
    coverUrl: '',
    docUrl: '',
    docType: '',
    version: '',
    views: 0,
    likes: 0
})

const formModel = reactive<Ebook>(defaultFormModel())

const openCreateModal = () => {
    Object.assign(formModel, defaultFormModel())
    modalMode.value = 'create'
    modalOpen.value = true
}

const openEditModal = (record: Ebook) => {
    Object.assign(formModel, record)
    modalMode.value = 'edit'
    modalOpen.value = true
}

const submitModal = async () => {
    try {
        if (modalMode.value == 'create') {
            await createEbook(formModel)
            message.success('Ebook created successfully')
        } else if (modalMode.value == 'edit') {
            await updateEbook(formModel.id, formModel)
            message.success('Ebook updated successfully')
        }
        modalOpen.value = false
        fetchEbooks()
    } catch (err: any) {
        message.error(err?.response?.data?.message || 'Operation failed')
    }
}

// ============================================================
// Lifecycle Hooks
// ============================================================
onMounted(() => {
    fetchEbooks()
})
</script>