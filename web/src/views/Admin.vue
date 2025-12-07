<template>
    <a-layout>
        <a-layout-content>
            <!-- 
            a-table - Ant Design Vue table component
                :columns - Table column definitions (reactive binding with v-bind shorthand)
                :row-key - Unique key for each row (function returns record.id)
                :data-source - Data array for table rows (reactive binding)
                :pagination - Pagination config object (reactive binding)
                :loading - Boolean loading state (reactive binding)
                @change - Event handler for table changes (v-on shorthand)
            -->
            <a-table :columns="columns" :row-key="(record) => record.id" :data-source="ebooks" :pagination="pagination"
                :loading="loading" @change="handleTableChange">
                <template #action="{ record }">
                    <!-- Space component to add spacing between buttons -->
                    <a-space size="small">
                        <!-- Primary button for edit action -->
                        <a-button type="primary" @click="handleEdit(record)">
                            Edit
                        </a-button>
                        <!-- Danger button for delete action -->
                        <a-button type="primary" danger @click="handleDelete(record)">
                            Delete
                        </a-button>
                    </a-space>
                </template>
            </a-table>
        </a-layout-content>
    </a-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'

const loading = ref(true)

// Define what an ebook looks like
interface Ebook {
    id: number
    name: string
    version: string
    viewCount: number
    voteCount: number
}

// Sample entry
const ebooks = ref<Ebook[]>([
    {
        id: 1,
        name: 'Deploying Avaya Aura® Communication Manager in Virtualized Environment',
        version: '10.2',
        viewCount: 100,
        voteCount: 5
    }
])

// Pagination configuration object for table component
// Uses reactive() to make the object reactive - changes automatically trigger UI updates
const pagination = reactive({
    current: 1,      // Current page number (starts from 1)
    pageSize: 10,    // Number of items per page
    total: 0         // Total number of items (will be updated from API response)
})

// Table columns definition
const columns = [
    {
        title: 'Name',
        dataIndex: 'name'
    },
    {
        title: 'Version',
        dataIndex: 'version'
    },
    {
        title: 'Views',
        dataIndex: 'viewCount'
    },
    {
        title: 'Likes',
        dataIndex: 'voteCount'
    },
    {
        title: 'Actions',
        slots: { customRender: 'action' }
    }
]

const handleTableChange = () => {
    console.log('Table changed')
}

const handleEdit = (record: Ebook) => {
    console.log('Edit:', record)
}

const handleDelete = (record: Ebook) => {
    console.log('Delete:', record)
}

onMounted(() => {
    loading.value = false;
})
</script>