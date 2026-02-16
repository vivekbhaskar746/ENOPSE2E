import axios from 'axios';
import dotenv from 'dotenv';

dotenv.config();

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:9090/api'; // Default fallback

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auth API
export const authService = {
  login: (credentials: { username: string; password: string }) =>
    api.post('/auth/login', credentials),
  
  register: (userData: { username: string; email: string; password: string; role: string }) =>
    api.post('/auth/register', userData),
  
  getCurrentUser: () => api.get('/auth/me'),
};

// Blog API
export const blogService = {
  generateBlog: (request: {
    keywords: string;
    tone: string;
    wordCount: number;
    targetAudience?: string;
    focusKeyword?: string;
  }) => api.post('/blogs/generate', request),
  
  saveBlog: (blog: any) => api.post('/blogs/save', blog),
  
  getMyBlogs: () => api.get('/blogs/my-blogs'),
  
  getBlog: (id: number) => api.get(`/blogs/${id}`),
  
  publishBlog: (id: number) => api.put(`/blogs/${id}/publish`),
};

// News Feed API
export const newsService = {
  getPersonalizedFeed: () => api.get('/news/feed'),
  
  likeArticle: (articleId: string) => api.post(`/news/articles/${articleId}/like`),
  
  dislikeArticle: (articleId: string) => api.post(`/news/articles/${articleId}/dislike`),
  
  bookmarkArticle: (articleId: string) => api.post(`/news/articles/${articleId}/bookmark`),
  
  getBookmarks: () => api.get('/news/bookmarks'),
  
  updateReadingBehavior: (data: {
    articleId: string;
    timeSpent: number;
    scrollDepth: number;
  }) => api.post('/news/reading-behavior', data),
};

// Email Campaign API
export const emailService = {
  saveTemplate: (template: any) => api.post('/email/templates', template),
  
  getTemplates: () => api.get('/email/templates'),
  
  getTemplate: (id: number) => api.get(`/email/templates/${id}`),
  
  createCampaign: (campaign: any) => api.post('/email/campaigns', campaign),
  
  getCampaigns: () => api.get('/email/campaigns'),
  
  getCampaignAnalytics: (id: number) => api.get(`/email/campaigns/${id}/analytics`),
  
  scheduleCampaign: (id: number, scheduleData: any) => 
    api.post(`/email/campaigns/${id}/schedule`, scheduleData),
};

export default api;
