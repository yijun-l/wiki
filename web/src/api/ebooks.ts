// api/ebooks.ts

import request from '@/utils/request'
import type {Ebook, EbookQueryParams} from '@/types/ebook'

const BASE_PATH = '/ebooks'

// POST /ebooks
export function createEbook(data: Partial<Ebook>){
  return request.post(`${BASE_PATH}`, data)
}

// GET /ebooks?page=1&size=10
export function listEbook(params: EbookQueryParams) {
  return request.get(`${BASE_PATH}`, { params })
}

// GET /ebooks/:id
export function getEbook(id: number) {
  return request.get(`${BASE_PATH}/${id}`)
}


// PUT /ebooks/:id
export function updateEbook(id: number, data: Partial<Ebook>) {
  return request.put(`${BASE_PATH}/${id}`, data)
}

// PATCH /ebooks/:id
export function updatePartialEbook(id: number, data: Partial<Ebook>) {
  return request.patch(`${BASE_PATH}/${id}`, data)
}

// DELETE /ebooks/:id
export function deleteEbook(id: number) {
  return request.delete(`${BASE_PATH}/${id}`)
}