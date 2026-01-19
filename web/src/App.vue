<!-- App.vue -->

<template>
  <a-layout>
    <AppHeader />

    <a-layout style="padding: 24px 0; background: #fff; margin: 0 50px; margin-top: 50px;">
      <a-layout-sider v-if="showSidebar" width="200" style="background: #fff">
        <SidebarMenu v-model:selectedKeys="selectedKeys"/>
      </a-layout-sider>

      <a-layout-content style="padding: 24px;">
        <router-view :selectedKeys="selectedKeys"/>
      </a-layout-content>
    </a-layout>

    <AppFooter />
  </a-layout>
</template>

<script lang="ts" setup>
import {ref, computed } from 'vue';
import {useRoute } from "vue-router";
import AppHeader from './components/AppHeader.vue';
import AppFooter from './components/AppFooter.vue';
import SidebarMenu from './components/SidebarMenu.vue';

// Default value is CM 10.2.x (key 103)
const selectedKeys = ref<string[]>(['103'])
const route = useRoute();

const showSidebar = computed(() => ['/', '/admin'].includes(route.path));
</script>

<style scoped></style>
