import { defineStore } from 'pinia'

const STORAGE_KEY = 'dancing_live_rooms_v1'

export const useLiveRoomStore = defineStore('liveRoom', {
  state: () => ({
    rooms: {}
  }),
  actions: {
    hydrateFromStorage() {
      try {
        const raw = localStorage.getItem(STORAGE_KEY)
        if (!raw) return
        const parsed = JSON.parse(raw)
        if (parsed && typeof parsed === 'object') {
          this.rooms = parsed
        }
      } catch (e) {
        console.warn('live room list hydrate failed', e)
      }
    },
    persist() {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.rooms))
      } catch (e) {
        console.warn('live room list persist failed', e)
      }
    },
    registerRoom({ roomId, title }) {
      if (!roomId || !title) return
      this.rooms[roomId] = {
        title: String(title).trim(),
        createdAt: Date.now()
      }
      this.persist()
    },
    getRoom(roomId) {
      return this.rooms[roomId] || null
    },
    clearRoom(roomId) {
      if (roomId && this.rooms[roomId]) {
        delete this.rooms[roomId]
        this.persist()
      }
    }
  }
})
