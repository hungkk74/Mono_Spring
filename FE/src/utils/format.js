export function formatCurrency(amount) {
  return new Intl.NumberFormat('vi-VN').format(amount) + '₫';
}

export function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}

export const ORDER_STATUS = {
  PENDING: { label: 'CHỜ XÁC NHẬN', cls: 'status-pending' },
  CONFIRMED: { label: 'ĐÃ XÁC NHẬN', cls: 'status-confirmed' },
  SHIPPING: { label: 'ĐANG GIAO', cls: 'status-shipping' },
  SHIPPED: { label: 'ĐANG GIAO', cls: 'status-shipping' },
  DELIVERED: { label: 'ĐÃ GIAO', cls: 'status-delivered' },
  CANCELLED: { label: 'ĐÃ HỦY', cls: 'status-cancelled' },
};

export function getStatusInfo(status) {
  return ORDER_STATUS[status] || { label: status, cls: 'status-pending' };
}

export const COLOR_MAP = {
  BLACK: '#000', WHITE: '#fff', NAVY: '#001F3F', GRAY: '#808080', GREY: '#808080',
  RED: '#C62828', BLUE: '#1565C0', GREEN: '#2E7D32', CHARCOAL: '#333',
  'ĐEN': '#000', 'TRẮNG': '#fff', 'XÁM': '#808080', 'XANH': '#1565C0',
};

export function getColorHex(name) {
  return COLOR_MAP[(name || '').toUpperCase()] || '#ccc';
}
