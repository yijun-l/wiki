<template>
  <div>
    <button @click="fetchData">Fetch Data (Console Log Only)</button>
  </div>
</template>

<script setup>
import axios from 'axios'

const fetchData = async () => {
  try {
    const res = await axios.get('http://localhost:8080/ebook/list?id=3')
    console.log('Data:', res.data)
  } catch (err) {
    console.error('Error:', err)
  }
}
</script>