<template>
  <a-layout-header class="header">
    <div class="logo" />
    <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="horizontal" :style="{ lineHeight: '64px' }">
      <a-menu-item key="home">
        <router-link to="/">Home</router-link>
      </a-menu-item>
      <a-menu-item key="admin">
        <router-link to="/admin">Admin</router-link>
      </a-menu-item>
      <a-menu-item key="users">
        <router-link to="/users">Users</router-link>
      </a-menu-item>
      <a-menu-item key="about">
        <router-link to="/about">About</router-link>
      </a-menu-item>
    </a-menu>
  </a-layout-header>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

// Get current route information
const route = useRoute();

// Delayed execution (after route ready), auto-reactive updates
const selectedKeys = computed(() => [route.name?.toLowerCase() || 'home']);
</script>

<style scoped></style>