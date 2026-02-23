<template>
  <div class="app">
    <NavBar />
    <router-view />
  </div>
</template>

<script setup lang="ts">
import NavBar from '@/components/NavBar.vue'
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: #f6f7f9;
}
</style>


