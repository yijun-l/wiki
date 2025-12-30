import axios from "axios";

const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000
})

request.interceptors.response.use(
  res => res.data,
  err => Promise.reject(err)
)

export default request