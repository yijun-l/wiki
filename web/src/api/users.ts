// api/users.ts

import request from '@/utils/request'
import type {User, UserQueryParams} from '@/types/user'

const BASE_PATH = '/users'

// POST /users
export function createUser(data: Partial<User>){
  return request.post(`${BASE_PATH}`, data)
}

// GET /users?page=1&size=10
export function listUser(params: UserQueryParams) {
  return request.get(`${BASE_PATH}`, { params })
}

// GET /users/:id
export function getUser(id: number) {
  return request.get(`${BASE_PATH}/${id}`)
}


// PUT /users/:id
export function updateUser(id: number, data: Partial<User>) {
  return request.put(`${BASE_PATH}/${id}`, data)
}

// PATCH /users/:id
export function updatePartialUser(id: number, data: Partial<User>) {
  return request.patch(`${BASE_PATH}/${id}`, data)
}

// DELETE /users/:id
export function deleteUser(id: number) {
  return request.delete(`${BASE_PATH}/${id}`)
}