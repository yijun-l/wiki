<template>

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
                    <a-button type="primary" @click="handleEdit(record)">Edit</a-button>
                    <!-- Danger button for delete action -->
                    <a-button type="primary" danger @click="handleDelete(record)">Delete</a-button>
                </a-space>
            </template>
        </template>
    </a-table>

</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import type { TablePaginationConfig, TableColumnType } from 'ant-design-vue'

const loading = ref(false)

// Define what an ebook looks like
interface Ebook {
    id: number
    name: string
    version: string
    views: number
    likes: number
}

// Sample entry
const ebooks = ref<Ebook[]>([])

// Pagination configuration object for table component
// Uses reactive() to make the object reactive - changes automatically trigger UI updates
const pagination = reactive({
    current: 1,      // Current page number (starts from 1)
    pageSize: 10,    // Number of items per page
    total: 0         // Total number of items (will be updated from API response)
})

// Table columns definition
const columns: TableColumnType<Ebook>[] = [
    { title: 'Name', dataIndex: 'name' },
    { title: 'Version', dataIndex: 'version' },
    { title: 'Views', dataIndex: 'views' },
    { title: 'Likes', dataIndex: 'likes' },
    { title: 'Actions', key: 'actions' }
]

// Fetch data from backend
const fetchEbooks = async () => {
    loading.value = true;
    try {
        const { data } = await axios.get('http://localhost:8080/ebook/list', {
            params: {
                page: pagination.current,
                size: pagination.pageSize
            }
        })
        ebooks.value = data.data.records
        pagination.total = data.data.total
    } catch (err) {
        console.error('Failed to fetch books:', err)
    } finally {
        loading.value = false
    }
}

const rowKey = (record: Ebook) => record.id

const handleTableChange = (pager: TablePaginationConfig) => {
    pagination.current = pager.current ?? 1
    pagination.pageSize = pager.pageSize ?? 10
    fetchEbooks()
}

const handleEdit = (record: Ebook) => {
    console.log('Edit:', record)
}

const handleDelete = (record: Ebook) => {
    console.log('Delete:', record)
}

onMounted(() => {
    fetchEbooks()
})
</script>