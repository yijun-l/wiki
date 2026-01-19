<!-- components/SidebarMenu.vue -->

<template>
    <a-menu v-model:selectedKeys="selectedKeys" mode="inline" @select="onSelect" style="height: 100%">
        <a-sub-menu key="cm" title="CM">
            <a-menu-item key="103">10.2.x</a-menu-item>
            <a-menu-item key="102">10.1.x</a-menu-item>
            <a-menu-item key="101">8.1.x</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="asm" title="SM">
            <a-menu-item key="203">10.2.x</a-menu-item>
            <a-menu-item key="202">10.1.x</a-menu-item>
            <a-menu-item key="201">8.1.x</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="smgr" title="SMGR">
            <a-menu-item key="303">10.2.x</a-menu-item>
            <a-menu-item key="302">10.1.x</a-menu-item>
            <a-menu-item key="301">8.1.x</a-menu-item>
        </a-sub-menu>
    </a-menu>
</template>

<script lang="ts" setup>
import { ref } from 'vue';


const props = defineProps<{ selectedKeys: string[] }>()
const emit = defineEmits<{ (e: 'update:selectedKeys', value: string[]): void }>()

const selectedKeys = ref(props.selectedKeys)

const onSelect = ({ key }: { key: string }) => {
    selectedKeys.value = [key]
    emit('update:selectedKeys', selectedKeys.value)
}

</script>