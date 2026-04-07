import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

export const authApi = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  register: (name, email, password, role) => api.post('/auth/register', { name, email, password, role })
}

export const chatApi = {
  send: (sessionId, message) => api.post('/chat', { sessionId, message }),
  history: (sessionId) => api.get(`/chat/history/${sessionId}`)
}

export const faqApi = {
  getAll: () => api.get('/faqs'),
  search: (keyword) => api.get(`/faqs/search?keyword=${keyword}`)
}

export const ticketApi = {
  create: (data) => api.post('/tickets', data),
  getByUser: (userId) => api.get(`/tickets/user/${userId}`),
  getByEmail: (email) => api.get(`/tickets/email/${email}`),
  getAll: () => api.get('/tickets'),
  getById: (id) => api.get(`/tickets/${id}`)
}

export const adminApi = {
  getTickets: () => api.get('/admin/tickets'),
  getUsers: () => api.get('/admin/users'),
  updateTicket: (id, solution, status) => api.put(`/admin/tickets/${id}/solution`, { solution, status })
}
