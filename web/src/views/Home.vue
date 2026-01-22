<!-- views/Home.vue -->

<template>
    <!-- Loading indicator
        a-spin: Loading indicator component from Ant Design Vue
        tip="Loading...": Display text next to the spinner
    -->
    <a-spin :spinning="loading" tip="Loading...">

        <!-- Error message
            a-alert: Alert component from Ant Design Vue
            v-else-if="error": Show when 'error' has a value (truthy) and previous conditions are false
            message="Loading Failed": Main title of the alert
            :description="error": Display error details from 'error' variable
            type="error": Set alert style to error type (red color)
            show-icon: Display warning icon automatically
            closable: Allow user to close the alert with 'X' button
            @close="error = ''": Clear error message when alert is closed
        -->
        <a-alert v-if="error" message="Loading Failed" :description="error" type="error" show-icon closable
            @close="error = ''" />

        <!-- Empty data 
            a-empty: Empty state component from Ant Design Vue
            v-else-if="!loading && (!ebooks || ebooks.length === 0)": 
                Show when:
                1. Not loading (!loading)
                2. AND ebooks is undefined/null OR has no items
            description="No Data Available": Text to display in empty state
        -->
        <a-empty v-else-if="!ebooks || ebooks.length === 0" description="No Data Available" />

        <template v-else>
            <a-row :gutter="20">
                <a-col :span="8" v-for="item in ebooks" :key="item.id">
                    <a-card :hoverable="true" :title="item.descText">
                        <template #extra>
                            <star-outlined class="action-icon" @click="handleFavorite(item)" />
                        </template>
                        <p>{{ item.name }}</p>

                    </a-card>
                </a-col>
            </a-row>

            <a-pagination :current="pagination.current" :page-size="pagination.pageSize" :total="pagination.total"
                @change="onPageChange" style="margin-top: 16px; text-align: center;" />
        </template>

    </a-spin>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue';
import { StarOutlined } from '@ant-design/icons-vue';
import { listEbook } from '@/api/ebooks'
import type { Ebook, EbookQueryParams } from '@/types/ebook'

const ebooks = ref<Ebook[]>([])
const loading = ref(true)
const error = ref('')

// ============================================================
// Data fetching and pagination state
// ============================================================

// Uses reactive() to make the object reactive - changes automatically trigger UI updates
const pagination = reactive({
    current: 1,      // Current page number (starts from 1)
    pageSize: 12,    // Number of items per page
    total: 0         // Total number of items (will be updated from API response)
})

const handleFavorite = (item: Ebook) => {
    message.info(`Added to favorites: ${item.name}`)
};

// Fetch data from backend
const fetchEbooks = async (name?:string, cat1Id?:number, cat2Id?:number) => {
    loading.value = true;
    try {
        const params: EbookQueryParams = {
            page: pagination.current,
            size: pagination.pageSize,
        }
        if (name) {
            params.name = name
        }
        if (cat1Id){
            params.cat1Id = cat1Id
        }
        if (cat2Id){
            params.cat2Id = cat2Id
        }
        const { data } = await listEbook(params)
        ebooks.value = data.records
        pagination.total = data.total
    } catch (err: any) {
        error.value = `Loading failed: ${err.message || 'Unknown error'}`
    } finally {
        loading.value = false
    }
}

const onPageChange = (page: number, pageSize: number) => {
    pagination.current = page
    pagination.pageSize = pageSize
    fetchEbooks()
}

// ============================================================
// Watch Siderbar Selection Changes
// ============================================================

const props = defineProps<{
    selectedKeys: string[]
}>()

watch(
    () => props.selectedKeys,
    (val) => {
        if (val?.length && val[0] != null) {
            const num = val[0]
            const str = num.toString().padStart(3, '0')
            const cat1 = Number(str[0])
            const cat2 = Number(str.slice(1, 3))
            fetchEbooks(undefined,cat1, cat2)
        }
    },
    { immediate: true }
)

// ============================================================
// Lifecycle Hooks
// ============================================================
onMounted(async () => {
    await fetchEbooks()
})
</script>

<style scoped>
.action-icon {
    font-size: 16px;
    color: #8c8c8c;
    cursor: pointer;
    transition: color 0.3s;
    margin-right: 4px;
    display: inline-flex;
    align-items: center;
}

.action-icon:hover {
    color: #faad14;
}
</style>