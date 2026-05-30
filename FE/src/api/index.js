import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// Auto-attach JWT token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auto-redirect on 401
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      const hadToken = !!localStorage.getItem('token');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (hadToken) {
        alert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
      }
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/**
 * Parse ApiResponse<T> → { ok, data, message, errorCode }
 * Handles both success (2xx) and error (4xx/5xx) response bodies.
 */
function parse(res) {
  const json = res.data;
  return {
    ok: json.status >= 200 && json.status < 300,
    data: json.data,
    message: json.message,
    errorCode: json.error_code ?? json.errorCode,
  };
}

function parseError(error) {
  if (error.response?.data) {
    const json = error.response.data;
    return {
      ok: false,
      data: null,
      message: json.message || 'Có lỗi xảy ra',
      errorCode: json.error_code ?? json.errorCode,
    };
  }
  return { ok: false, data: null, message: error.message || 'Lỗi kết nối', errorCode: null };
}

// ==================== AUTH ====================
export const authApi = {
  login: async (email, password) => {
    const res = await api.post('/auth/login', { email, password });
    return parse(res);
  },
  register: async (data) => {
    const res = await api.post('/auth/register', data);
    return parse(res);
  },
  getMe: async () => {
    const res = await api.get('/auth/me');
    return parse(res);
  },
  updateProfile: async (data) => {
    const res = await api.put('/auth/me', data);
    return parse(res);
  },
  // ===== Forgot Password =====
  forgotPassword: async (email) => {
    try {
      const res = await api.post('/auth/forgot-password', { email });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  verifyOtp: async (email, code) => {
    try {
      const res = await api.post('/auth/verify-otp', { email, code });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  resetPassword: async (resetToken, newPassword) => {
    try {
      const res = await api.post('/auth/reset-password', { resetToken, newPassword });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
};

// ==================== PUBLIC CATALOG ====================
export const catalogApi = {
  getCategories: async () => {
    const res = await api.get('/public/categories');
    return parse(res);
  },
  getCategoryBySlug: async (slug) => {
    const res = await api.get(`/public/categories/${slug}`);
    return parse(res);
  },
  getProducts: async (page = 0, size = 20, categoryId = null, keyword = null, skuSizes = [], skuColors = [], saleOnly = false) => {
    let url = `/public/products?page=${page}&size=${size}`;
    if (categoryId) url += `&categoryId=${categoryId}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
    if (saleOnly) url += '&saleOnly=true';
    if (skuSizes && skuSizes.length > 0) {
      skuSizes.forEach(s => url += `&skuSizes=${encodeURIComponent(s)}`);
    }
    if (skuColors && skuColors.length > 0) {
      skuColors.forEach(c => url += `&skuColors=${encodeURIComponent(c)}`);
    }
    const res = await api.get(url);
    return parse(res);
  },
  getProductBySlug: async (slug) => {
    const res = await api.get(`/public/products/${slug}`);
    return parse(res);
  },
};

// ==================== ORDERS ====================
export const orderApi = {
  placeOrder: async (shippingAddress, paymentMethod, items, couponCode = null) => {
    try {
      const res = await api.post('/orders', { shippingAddress, paymentMethod, couponCode, items });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  getMyOrders: async (page = 0, size = 20) => {
    const res = await api.get('/orders', { params: { page, size } });
    return parse(res);
  },
  getOrderDetail: async (id) => {
    const res = await api.get(`/orders/${id}`);
    return parse(res);
  },
  cancelOrder: async (id) => {
    const res = await api.patch(`/orders/${id}/cancel`);
    return parse(res);
  },
  reorder: async (id) => {
    try {
      const res = await api.post(`/orders/${id}/reorder`);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  trackOrder: async (id) => {
    try {
      const res = await api.get(`/public/orders/${id}/tracking`);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
};

// ==================== COUPONS ====================
export const couponApi = {
  apply: async (code, subtotalAmount) => {
    try {
      const res = await api.post('/public/coupons/apply', { code, subtotalAmount });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
};

// ==================== ADMIN ====================
export const adminApi = {
  getOrders: async (page = 0, size = 15, status = '', search = '') => {
    const params = { page, size };
    if (status) params.status = status;
    if (search) params.search = search;
    const res = await api.get('/admin/orders', { params });
    return parse(res);
  },
  updateOrderStatus: async (id, status) => {
    const res = await api.patch(`/admin/orders/${id}/status`, { status });
    return parse(res);
  },
  getProducts: async (page = 0, size = 15) => {
    const res = await api.get('/admin/products', { params: { page, size } });
    return parse(res);
  },
  createProduct: async (data) => {
    const res = await api.post('/admin/products', data);
    return parse(res);
  },
  deleteProduct: async (id) => {
    const res = await api.delete(`/admin/products/${id}`);
    return parse(res);
  },
  createSku: async (data) => {
    const res = await api.post('/admin/skus', data);
    return parse(res);
  },
  getUsers: async (role = 'STAFF', page = 0, size = 50) => {
    const res = await api.get('/admin/users', { params: { role, page, size } });
    return parse(res);
  },
  createStaff: async (data) => {
    const res = await api.post('/admin/users/staff', data);
    return parse(res);
  },
  deactivateUser: async (id) => {
    const res = await api.delete(`/admin/users/${id}`);
    return parse(res);
  },
  activateUser: async (id) => {
    const res = await api.patch(`/admin/users/${id}/activate`);
    return parse(res);
  },
  // ===== Revenue =====
  getRevenue: async (period = '30d', from = null, to = null) => {
    try {
      const params = { period };
      if (from) params.from = from;
      if (to) params.to = to;
      const res = await api.get('/admin/revenue', { params });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
};

// ==================== REVIEWS ====================
export const reviewApi = {
  getReviews: async (productId) => {
    try {
      const res = await api.get(`/public/reviews?productId=${productId}`);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  getStats: async (productId) => {
    try {
      const res = await api.get(`/public/reviews/stats?productId=${productId}`);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  getAll: async (page = 0, size = 15) => {
    try {
      const res = await api.get('/reviews/all', { params: { page, size } });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  create: async (data) => {
    try {
      const res = await api.post('/reviews', data);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  delete: async (id) => {
    try {
      const res = await api.delete(`/reviews/${id}`);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  // ===== Reply =====
  createReply: async (reviewId, content) => {
    try {
      const res = await api.post(`/reviews/${reviewId}/replies`, { reviewId, content });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  deleteReply: async (replyId) => {
    try {
      const res = await api.delete(`/reviews/replies/${replyId}`);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
};

// ==================== PAYMENT ====================
export const paymentApi = {
  createMomo: async (orderId, requestType = 'payWithMethod') => {
    try {
      const res = await api.post('/payment/momo/create', { orderId, requestType });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  createVietQr: async (orderId) => {
    try {
      const res = await api.post('/payment/vietqr/create', { orderId });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  createPayOs: async (orderId) => {
    try {
      const res = await api.post('/payment/payos/create', { orderId });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  momoCallback: async (resultCode, extraData, orderId) => {
    try {
      const res = await api.post('/payment/momo/callback', { resultCode, extraData, orderId });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  payOsCallback: async (orderCode) => {
    try {
      const res = await api.post('/payment/payos/callback', { orderCode });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
};

// ==================== CHATBOT ====================
export const chatbotApi = {
  chat: async (message) => {
    try {
      const res = await api.post('/public/chatbot/chat', { message });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
};

// ==================== BANNERS ====================
export const bannerApi = {
  getPublic: async () => {
    try {
      const res = await api.get('/public/banners');
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  getAll: async (page = 0, size = 20) => {
    try {
      const res = await api.get('/admin/banners', { params: { page, size } });
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  getById: async (id) => {
    try {
      const res = await api.get(`/admin/banners/${id}`);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  create: async (data) => {
    try {
      const res = await api.post('/admin/banners', data);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  update: async (id, data) => {
    try {
      const res = await api.put(`/admin/banners/${id}`, data);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  delete: async (id) => {
    try {
      const res = await api.delete(`/admin/banners/${id}`);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
  reorder: async (ids) => {
    try {
      const res = await api.put('/admin/banners/reorder', ids);
      return parse(res);
    } catch (e) { return parseError(e); }
  },
};

export default api;
