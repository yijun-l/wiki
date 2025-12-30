import request from '@/utils/request'
import type {Ebook} from '@/types/ebook'

const BASE_PATH = '/ebooks'

export function listEbook(params: { page: number; size: number }) {
  return request.get(`${BASE_PATH}/list`, { params })
}

export function deleteEbook(id: number) {
  return request.delete(`${BASE_PATH}/${id}`)
}

export function updateEbook(id: number, data: Partial<Ebook>) {
  return request.patch(`${BASE_PATH}/${id}`, data)
}