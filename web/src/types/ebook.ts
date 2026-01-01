// types/ebook.ts

// ============================================================
// General type definition for Ebook entries
// ============================================================
export interface Ebook {
    id: number
    name: string
    cat1Id: number
    cat2Id: number
    descText: string
    coverUrl: string
    docUrl: string
    docType: string
    version: string
    views: number
    likes: number
}