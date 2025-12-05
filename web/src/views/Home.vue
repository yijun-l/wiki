<template>
  <div>
    <button @click="fetchData">Fetch Data (Console Log Only)</button>
    <br>
    <star-outlined />
    <star-filled />
    <star-two-tone two-tone-color="#eb2f96" />
  </div>
</template>

<script setup>
import axios from 'axios';
import { StarOutlined, StarFilled, StarTwoTone } from '@ant-design/icons-vue';

const fetchData = async () => {
  try {
    const res = await axios.get('http://localhost:8080/ebook/list?id=3')
    console.log('Data:', res.data)
  } catch (err) {
    console.error('Error:', err)
  }
}
</script>