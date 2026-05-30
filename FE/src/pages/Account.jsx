import { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useToast } from '../components/Toast';
import { orderApi } from '../api';
import { formatCurrency, formatDate, getStatusInfo } from '../utils/format';

const STATUS_TABS = [
  { value: '', label: 'TẤT CẢ' },
  { value: 'PENDING', label: 'CHỜ XÁC NHẬN' },
  { value: 'CONFIRMED', label: 'ĐÃ XÁC NHẬN' },
  { value: 'SHIPPED', label: 'ĐANG GIAO' },
  { value: 'DELIVERED', label: 'ĐÃ GIAO' },
  { value: 'CANCELLED', label: 'ĐÃ HỦY' },
];

export default function Account() {
  const { user, logout, updateProfile, refreshProfile } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [activeTab, setActiveTab] = useState('profile');
  const [form, setForm] = useState({ fullName: '', phoneNumber: '', address: '' });
  const [orders, setOrders] = useState([]);
  const [orderPage, setOrderPage] = useState(0);
  const [orderTotalPages, setOrderTotalPages] = useState(1);
  const [statusFilter, setStatusFilter] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => { refreshProfile(); }, []);
  useEffect(() => {
    if (user) setForm({ fullName: user.fullName || '', phoneNumber: user.phoneNumber || '', address: user.address || '' });
  }, [user]);
  useEffect(() => { if (location.hash === '#orders') setActiveTab('orders'); }, [location]);
  useEffect(() => { if (activeTab === 'orders') loadOrders(); }, [activeTab, orderPage, statusFilter]);

  const loadOrders = async () => {
    try {
      const res = await orderApi.getMyOrders(orderPage, 10);
      if (res.ok && res.data) {
        let items = res.data.content || [];
        if (statusFilter) items = items.filter(o => o.status === statusFilter);
        setOrders(items);
        setOrderTotalPages(res.data.totalPages || 1);
      }
    } catch { toast.error('Lỗi tải đơn hàng'); }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault(); setSaving(true);
    const res = await updateProfile(form);
    res.ok ? toast.success('Đã cập nhật hồ sơ') : toast.error('Lỗi: ' + (res.message || ''));
    setSaving(false);
  };

  const handleCancelOrder = async (id) => {
    if (!confirm('Hủy đơn #' + id + '?')) return;
    const res = await orderApi.cancelOrder(id);
    res.ok ? (toast.success('Đã hủy đơn #' + id), loadOrders()) : toast.error(res.message || 'Lỗi');
  };

  const { addToCart } = useCart();
  const [reordering, setReordering] = useState(null);

  const handleReorder = async (orderId) => {
    setReordering(orderId);
    try {
      const res = await orderApi.reorder(orderId);
      if (!res.ok) { toast.error(res.message || 'Lỗi mua lại'); setReordering(null); return; }
      const items = res.data || [];
      const available = items.filter(i => i.available);
      const unavailable = items.filter(i => !i.available);
      if (available.length === 0) { toast.error('Tất cả sản phẩm trong đơn đã hết hàng hoặc ngừng kinh doanh'); setReordering(null); return; }
      available.forEach(item => {
        addToCart({
          skuId: item.skuId,
          productName: item.productName,
          color: item.color,
          size: item.size,
          price: item.salePrice || item.currentPrice,
          originalPrice: item.currentPrice,
          image: item.productImageUrl || '',
          quantity: 1,
        });
      });
      if (unavailable.length > 0) {
        toast.error(`${unavailable.length} sản phẩm không khả dụng: ${unavailable.map(i => i.productName).join(', ')}`);
      }
      toast.success(`Đã thêm ${available.length} sản phẩm vào giỏ hàng`);
      navigate('/cart');
    } catch { toast.error('Lỗi kết nối'); }
    setReordering(null);
  };

  const handleLogout = () => { if (confirm('Đăng xuất?')) { logout(); navigate('/login'); } };

  return (
    <div className="account-page">
      <div className="container">
        <h1 className="page-title">TÀI KHOẢN CỦA TÔI</h1>
        <div className="account-layout">
          <aside className="account-sidebar">
            <div className="account-greeting">
              <p className="greeting-label">TÀI KHOẢN</p>
              <p className="greeting-name">Chào, {user?.fullName || user?.email}</p>
            </div>
            <nav className="account-nav">
              {['profile', 'orders', 'address'].map(tab => (
                <button key={tab} className={`account-nav-btn ${activeTab === tab ? 'active' : ''}`} onClick={() => setActiveTab(tab)}>
                  <span className="material-symbols-outlined">{tab === 'profile' ? 'person' : tab === 'orders' ? 'package' : 'location_on'}</span>
                  {tab === 'profile' ? 'HỒ SƠ' : tab === 'orders' ? 'ĐƠN HÀNG' : 'ĐỊA CHỈ'}
                </button>
              ))}
              <hr />
              <button className="account-nav-btn danger" onClick={handleLogout}>
                <span className="material-symbols-outlined">logout</span>ĐĂNG XUẤT
              </button>
            </nav>
          </aside>

          <div className="account-content">
            {activeTab === 'profile' && (
              <section className="account-panel">
                <div className="profile-header">
                  <div className="profile-avatar"><span className="material-symbols-outlined">account_circle</span></div>
                  <div>
                    <h2 className="profile-name">{user?.fullName || 'Chưa cập nhật'}</h2>
                    <p className="profile-email">{user?.email}</p>
                    <span className="profile-role">{user?.role === 'ADMIN' ? 'QUẢN TRỊ VIÊN' : user?.role === 'STAFF' ? 'NHÂN VIÊN' : 'KHÁCH HÀNG'}</span>
                  </div>
                </div>
                <form onSubmit={handleUpdateProfile} className="profile-form">
                  <div className="form-grid">
                    <div className="form-group"><label>HỌ TÊN</label><input value={form.fullName} onChange={e => setForm({...form, fullName: e.target.value})} /></div>
                    <div className="form-group"><label>EMAIL</label><input value={user?.email || ''} readOnly className="readonly" /></div>
                    <div className="form-group"><label>SỐ ĐIỆN THOẠI</label><input value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value})} /></div>
                    <div className="form-group"><label>ĐỊA CHỈ</label><input value={form.address} onChange={e => setForm({...form, address: e.target.value})} /></div>
                  </div>
                  <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'ĐANG LƯU...' : 'CẬP NHẬT'}</button>
                </form>
              </section>
            )}

            {activeTab === 'orders' && (
              <section className="account-panel">
                <h2 className="panel-title">ĐƠN HÀNG CỦA TÔI</h2>
                <div className="order-status-tabs">
                  {STATUS_TABS.map(t => (
                    <button key={t.value} className={`order-status-tab ${statusFilter === t.value ? 'active' : ''}`}
                      onClick={() => { setStatusFilter(t.value); setOrderPage(0); }}>{t.label}</button>
                  ))}
                </div>
                {orders.length === 0 ? (
                  <div className="empty-state"><span className="material-symbols-outlined">inbox</span><p>Không có đơn hàng</p></div>
                ) : (
                  <div className="orders-list">
                    {orders.map(order => {
                      const st = getStatusInfo(order.status);
                      return (
                        <article className="order-card" key={order.id}>
                          <div className="order-card-header">
                            <div><p className="order-id">Mã đơn: <strong>#{order.id}</strong></p><p className="order-date">{formatDate(order.createdAt)}</p></div>
                            <span className={`order-status-badge ${st.cls}`}>{st.label}</span>
                          </div>
                          {(order.items || []).map((it, i) => (
                            <div className="order-item" key={i}>
                              <div><p className="order-item-name">{it.productName}</p><p className="order-item-variant">{it.color}/{it.size} × {it.quantity}</p></div>
                              <p className="order-item-price">{formatCurrency(it.unitPrice * it.quantity)}</p>
                            </div>
                          ))}
                          <div className="order-card-footer">
                            <p>Tổng: <strong>{formatCurrency(order.totalAmount)}</strong></p>
                            <div className="order-card-actions">
                              {order.status === 'PENDING' && <button className="btn-cancel" onClick={() => handleCancelOrder(order.id)}>HỦY ĐƠN</button>}
                              {['DELIVERED', 'CANCELLED'].includes(order.status) && (
                                <button className="btn btn-sm btn-primary" onClick={() => handleReorder(order.id)} disabled={reordering === order.id}>
                                  {reordering === order.id ? 'ĐANG XỬ LÝ...' : 'MUA LẠI'}
                                </button>
                              )}
                              <Link to={`/account/orders/${order.id}`} className="btn btn-sm btn-outline">XEM CHI TIẾT</Link>
                            </div>
                          </div>
                        </article>
                      );
                    })}
                  </div>
                )}
                {orderTotalPages > 1 && (
                  <div className="pagination">{Array.from({length: orderTotalPages}, (_, i) => (
                    <button key={i} className={`page-btn ${i === orderPage ? 'active' : ''}`} onClick={() => setOrderPage(i)}>{i+1}</button>
                  ))}</div>
                )}
              </section>
            )}

            {activeTab === 'address' && (
              <section className="account-panel">
                <h2 className="panel-title">ĐỊA CHỈ GIAO HÀNG</h2>
                <div className="address-display"><p>{user?.address || 'Chưa cập nhật'}</p></div>
              </section>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
