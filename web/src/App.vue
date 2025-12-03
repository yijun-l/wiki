<template>
  <a-layout>
    <AppHeader />

    <a-layout style="padding: 24px 0; background: #fff; margin: 0 50px; margin-top: 50px;">
      <a-layout-sider width="200" style="background: #fff">
        <SidebarMenu />
      </a-layout-sider>

      <a-layout-content style="padding: 24px;">
        <router-view />
      </a-layout-content>
    </a-layout>

    <AppFooter />
  </a-layout>
</template>

<script lang="ts" setup>
import AppHeader from './components/AppHeader.vue';
import AppFooter from './components/AppFooter.vue';
import SidebarMenu from './components/SidebarMenu.vue';
</script>

<style scoped></style>
