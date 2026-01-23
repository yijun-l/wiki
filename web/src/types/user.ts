// types/user.ts

// ============================================================
// General type definition for User entries
// ============================================================
export interface User {
    id: number
    username: string
    email: string
    nickname: string
    avatarUrl: string
    status: string
}

export interface UserQueryParams {
    page: number
    size: number
    username?: string
}