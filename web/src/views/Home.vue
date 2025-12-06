<template>
    <div>
        <!-- Loading indicator
            a-spin: Loading indicator component from Ant Design Vue
            v-if="loading": Only show the spinner when 'loading' is true
            tip="Loading...": Display text next to the spinner
        -->
        <a-spin v-if="loading" tip="Loading..." class="loading">
            <div style="height: 200px;"></div>
        </a-spin>

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
        <a-alert v-else-if="error" message="Loading Failed" :description="error" type="error" show-icon closable
            @close="error = ''" />

        <!-- Empty data 
            a-empty: Empty state component from Ant Design Vue
            v-else-if="!loading && (!ebooks || ebooks.length === 0)": 
                Show when:
                1. Not loading (!loading)
                2. AND ebooks is undefined/null OR has no items
            description="No Data Available": Text to display in empty state
        -->
        <a-empty v-else-if="!loading && (!ebooks || ebooks.length === 0)" description="No Data Available" />

        <!-- Ebook list 
            a-list: List component from Ant Design Vue for displaying data collections
            v-else: Display when all previous conditions (loading, error, empty) are false
            item-layout="vertical": Arrange list items vertically (title on top, content below)
            size="large": Use large size styling
            :pagination="pagination": Bind pagination configuration object for page controls
            :grid="{ gutter: 20, column: 3 }": Use grid layout with 20px spacing and 3 columns
            :data-source="ebooks": Bind data array to display in the list
        -->
        <a-list v-else item-layout="vertical" size="large" :pagination="pagination" :grid="{ gutter: 20, column: 3 }"
            :data-source="ebooks">
            <template #renderItem="{ item }">
                <!-- 
                    a-list-item: Individual list item component
                    :key="item.id": Unique key for Vue's virtual DOM diffing (required for lists)
                -->
                <a-list-item :key="item.id">
                    <!-- Item content -->
                    <a-list-item-meta :description="getDescription(item)">
                        <template #title>
                            <div class="title-wrapper" :title="item.name">
                                <a :href="item.docUrl" target="_blank" class="ebook-title">
                                    <span class="title-text">{{ item.name }}</span>
                                    <file-text-outlined v-if="item.docType === 'doc'" class="doc-icon doc" />
                                    <file-pdf-outlined v-else-if="item.docType === 'pdf'" class="doc-icon pdf" />
                                    <link-outlined v-else class="doc-icon link" />
                                </a>
                            </div>
                            
                            <div class="title-footer">
                                <span>
                                    <star-outlined class="action-icon" @click="handleFavorite(item)" />
                                </span>
                                <span v-if="item.version" class="version">
                                    <tag-outlined style="margin-right: 4px;" />
                                    Version {{ item.version }}
                                </span>
                                <span class="category" v-if="item.cat1Id || item.cat2Id">
                                    <folder-outlined style="margin-right: 4px;" />
                                    Category: {{ item.cat1Id || '' }} {{ item.cat2Id ? '/' + item.cat2Id : '' }}
                                </span>
                            </div>
                        </template>

                        <template #avatar>
                            <!-- Show cover image if available, otherwise show book icon -->
                            <a-avatar v-if="item.coverUrl" :src="item.coverUrl" :size="48" />
                            <a-avatar v-else :size="48">
                                <template #icon>
                                    <book-outlined style="color: #1890ff; font-size: 24px;" />
                                </template>
                            </a-avatar>
                        </template>
                    </a-list-item-meta>

                </a-list-item>
            </template>
        </a-list>
    </div>
    
</template>

<script setup>
import axios from 'axios';
import { onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import {
    StarOutlined,
    FileTextOutlined,
    FilePdfOutlined,
    LinkOutlined,
    BookOutlined,
    TagOutlined,
    FolderOutlined
} from '@ant-design/icons-vue';

console.log("[Home.vue] setup");

// Reactive data
const ebooks = ref([]);
const loading = ref(true);
const error = ref('');

// Pagination configuration
const pagination = {
    pageSize: 6,
    showSizeChanger: false,
    showTotal: (total) => `Total ${total} items`,
};

// Get description text
const getDescription = (item) => {
    if (item.descText) {
        if (item.descText.length > 100) {
            return item.descText.substring(0, 100) + '...';
        }
        return item.descText;
    }
    return 'No description available';
};

// Handle favorite action
const handleFavorite = (item) => {
    message.info(`Added to favorites: ${item.name}`);
    console.log('Favorite item:', item);
};

onMounted(() => {
    console.log("[Home.vue] onMounted");
    loading.value = true;

    axios.get("http://localhost:8080/ebook/list")
        .then((response) => {
            console.log("Response data:", response.data);
            if (response.data && Array.isArray(response.data.data)) {
                ebooks.value = response.data.data;
            } else if (Array.isArray(response.data)) {
                ebooks.value = response.data;
            } else {
                ebooks.value = [];
                console.warn("Invalid data format:", response.data);
            }
        })
        .catch((err) => {
            console.error("Request error:", err);
            error.value = `Loading failed: ${err.message || 'Unknown error'}`;
            message.error('Failed to load data');
        })
        .finally(() => {
            loading.value = false;
            console.log("Loading completed, item count:", ebooks.value.length);
        });
});
</script>

<style scoped>
.loading {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 300px;
}

.title-wrapper {
    height: 3.0em; 
    line-height: 1.5em;
    overflow: hidden;
    margin-bottom: 8px;
}

.title-footer {
    margin-top: 10px;
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.65);
}

.action-icon {
    font-size: 18px;
    color: #8c8c8c;
    cursor: pointer;
    transition: color 0.3s;
    margin-right: px;
}

.action-icon:hover {
    color: #faad14;
}

.version,
.category {
    display: flex;
    align-items: center;
}

/* Responsive design */
@media (max-width: 768px) {
    :deep(.ant-list-grid) .ant-col {
        width: 100% !important;
    }

    .title-footer {
        flex-direction: column;
        gap: 6px;
    }
}
</style>