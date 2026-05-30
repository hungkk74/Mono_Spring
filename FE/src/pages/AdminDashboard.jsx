import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../components/Toast';
import { adminApi, catalogApi, reviewApi, bannerApi } from '../api';
import { formatCurrency, formatDate } from '../utils/format';

const STATUS_MAP = { PENDING: 'Chờ xác nhận', CONFIRMED: 'Đã xác nhận', SHIPPED: 'Đang giao', DELIVERED: 'Đã giao', CANCELLED: 'Đã hủy' };
const NEXT_STATUS = { CONFIRMED: 'SHIPPED', SHIPPED: 'DELIVERED' };
const NEXT_LABEL = { CONFIRMED: 'Giao hàng', SHIPPED: 'Đã giao' };
const NEXT_TOAST = { PENDING: 'Đơn hàng đã được xác nhận', CONFIRMED: 'Đơn đã được bàn giao cho đơn vị vận chuyển', SHIPPED: 'Đơn hàng đã giao thành công' };

// MoMo PENDING orders must NOT be manually confirmed — payment gateway handles this
const isGatewayPending = (o) => o.status === 'PENDING'
  && ['MOMO', 'BANK_TRANSFER'].includes(o.paymentMethod);

export default function AdminDashboard() {
  const { user, logout } = useAuth();
  const toast = useToast();
  const [tab, setTab] = useState('orders');

  return (
    <div className="admin-page">
      <header className="admin-header">
        <div className="admin-header-left">
          <span className="admin-logo">MONO WEAR</span>
          <span className="admin-badge">Admin</span>
        </div>
        <div className="admin-header-right">
          <span className="admin-name">{user?.fullName || user?.email}</span>
          <button onClick={logout} className="admin-logout">Đăng xuất</button>
        </div>
      </header>

      <div className="admin-layout">
        <aside className="admin-sidebar">
          <nav>
            {[
              { key: 'orders', icon: 'package_2', label: 'Đơn hàng' },
              { key: 'revenue', icon: 'bar_chart', label: 'Doanh thu' },
              { key: 'reviews', icon: 'reviews', label: 'Đánh giá' },
              { key: 'products', icon: 'inventory_2', label: 'Sản phẩm' },
              { key: 'banners', icon: 'slideshow', label: 'Banner' },
              ...(user?.role === 'ADMIN' ? [{ key: 'staff', icon: 'group', label: 'Nhân viên' }] : []),
            ].map(item => (
              <button key={item.key} className={`admin-nav-btn ${tab === item.key ? 'active' : ''}`} onClick={() => setTab(item.key)}>
                <span className="material-symbols-outlined">{item.icon}</span>{item.label}
              </button>
            ))}
          </nav>
        </aside>
        <main className="admin-main">
          {tab === 'orders' && <OrdersPanel toast={toast} />}
          {tab === 'revenue' && <RevenuePanel toast={toast} />}
          {tab === 'reviews' && <ReviewsPanel toast={toast} />}
          {tab === 'products' && <ProductsPanel toast={toast} />}
          {tab === 'banners' && <BannersPanel toast={toast} />}
          {tab === 'staff' && <StaffPanel toast={toast} />}
        </main>
      </div>
    </div>
  );
}

function OrdersPanel({ toast }) {
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filter, setFilter] = useState('');
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');

  const load = async () => {
    const res = await adminApi.getOrders(page, 15, filter, search);
    if (res.ok && res.data) { setOrders(res.data.content || []); setTotalPages(res.data.totalPages || 1); }
  };
  useEffect(() => { load(); }, [page, filter, search]);

  const handleSearch = (e) => {
    e.preventDefault();
    setSearch(searchInput.trim());
    setPage(0);
  };

  const clearSearch = () => {
    setSearchInput('');
    setSearch('');
    setPage(0);
  };

  const handleUpdateStatus = async (id, status, label, currentStatus) => {
    const res = await adminApi.updateOrderStatus(id, status);
    if (res.ok) {
      toast.success(NEXT_TOAST[currentStatus] || `Đơn #${id}: ${label} thành công`);
      setOrders(prev => prev.map(o => o.id === id ? { ...o, status } : o));
    } else {
      toast.error(res.message || 'Cập nhật thất bại');
    }
  };

  const handleCancelOrder = async (id) => {
    const res = await adminApi.updateOrderStatus(id, 'CANCELLED');
    if (res.ok) {
      toast.success(`Đơn #${id} đã hủy`);
      setOrders(prev => prev.map(o => o.id === id ? { ...o, status: 'CANCELLED' } : o));
    } else {
      toast.error(res.message || 'Hủy đơn thất bại');
    }
  };

  return (
    <div>
      <div className="admin-panel-header">
        <h1>Quản lý đơn hàng</h1>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8 }}>
            <input
              value={searchInput}
              onChange={e => setSearchInput(e.target.value)}
              placeholder="Tìm mã đơn hoặc tên KH..."
              style={{ padding: '8px 12px', border: '1px solid #ddd', borderRadius: 4, fontSize: '0.9rem', width: 220 }}
            />
            <button type="submit" className="btn btn-sm btn-primary">Tìm</button>
            {search && <button type="button" className="btn-link" onClick={clearSearch} style={{ fontSize: '0.85rem' }}>Xóa</button>}
          </form>
          <select value={filter} onChange={e => { setFilter(e.target.value); setPage(0); }} className="admin-select">
            <option value="">Tất cả</option>
            {Object.entries(STATUS_MAP).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </div>
      </div>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead><tr><th>ID</th><th>Khách hàng</th><th>Sản phẩm</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th><th>Ngày đặt</th><th>Thao tác</th></tr></thead>
          <tbody>
            {orders.map(o => (
              <tr key={o.id}>
                <td><strong>#{o.id}</strong></td>
                <td>{o.userFullName || 'N/A'}</td>
                <td style={{ maxWidth: 220, fontSize: '0.85rem' }}>
                  {o.items && o.items.length > 0 ? (
                    <div>
                      {o.items.slice(0, 2).map((item, i) => (
                        <div key={i} style={{ lineHeight: 1.4 }}>
                          {item.productName} <span style={{ color: '#888' }}>×{item.quantity}</span>
                        </div>
                      ))}
                      {o.items.length > 2 && <div style={{ color: '#888', fontStyle: 'italic' }}>+{o.items.length - 2} sản phẩm khác</div>}
                    </div>
                  ) : <span style={{ color: '#aaa' }}>—</span>}
                </td>
                <td><strong>{formatCurrency(o.totalAmount)}</strong></td>
                <td><span className={`admin-status-badge status-${(o.paymentMethod || '').toLowerCase()}`}>{o.paymentMethod || 'N/A'}</span></td>
                <td><span className={`admin-status-badge status-${o.status.toLowerCase()}`}>{STATUS_MAP[o.status] || o.status}</span></td>
                <td>{formatDate(o.createdAt)}</td>
                <td style={{ display: 'flex', gap: 6, flexWrap: 'wrap', alignItems: 'center' }}>
                  {/* MoMo PENDING: ẩn nút Xác nhận, hiển thị badge chờ thanh toán */}
                  {isGatewayPending(o) && (
                    <span style={{ color: '#e85d04', fontWeight: 600, fontSize: '12px',
                      background: '#fff3e0', padding: '3px 8px', borderRadius: '6px',
                      border: '1px solid #e85d04', whiteSpace: 'nowrap' }}>
                      Chờ thanh toán
                    </span>
                  )}
                  {/* Nút Xác nhận thủ công: chỉ với đơn PENDING không phải gateway */}
                  {o.status === 'PENDING' && !isGatewayPending(o) && (
                    <button className="btn btn-sm btn-primary"
                      onClick={() => handleUpdateStatus(o.id, 'CONFIRMED', 'Xác nhận', 'PENDING')}>
                      Xác nhận
                    </button>
                  )}
                  {/* Nút tiến trình: CONFIRMED→Giao hàng, SHIPPED→Đã giao */}
                  {NEXT_STATUS[o.status] && (
                    <button className="btn btn-sm btn-primary"
                      onClick={() => handleUpdateStatus(o.id, NEXT_STATUS[o.status], NEXT_LABEL[o.status], o.status)}>
                      {NEXT_LABEL[o.status]}
                    </button>
                  )}
                  {/* Nút Hủy: chỉ khi chưa hoàn tất/hủy */}
                  {o.status !== 'CANCELLED' && o.status !== 'DELIVERED' && (
                    <button className="btn btn-sm btn-cancel" onClick={() => handleCancelOrder(o.id)}>Hủy</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {orders.length === 0 && <div className="empty-state"><p>Không có đơn hàng</p></div>}
      </div>
      {totalPages > 1 && <div className="pagination">{Array.from({length: totalPages}, (_, i) => (
        <button key={i} className={`page-btn ${i === page ? 'active' : ''}`} onClick={() => setPage(i)}>{i+1}</button>
      ))}</div>}
    </div>
  );
}

function ProductsPanel({ toast }) {
  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [showModal, setShowModal] = useState(false);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ name: '', categoryId: '', material: '', description: '', imageUrl: '' });
  const [skuRows, setSkuRows] = useState([{ skuCode: '', size: '', color: '', price: '', stock: 50 }]);
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState('');
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  const load = async () => {
    const res = await adminApi.getProducts(page, 15);
    if (res.ok && res.data) { setProducts(res.data.content || []); setTotalPages(res.data.totalPages || 1); }
  };
  useEffect(() => {
    load();
    const interval = setInterval(load, 10000); // Polling every 10 seconds to update stock levels
    return () => clearInterval(interval);
  }, [page]);

  const openModal = async () => {
    setForm({ name: '', categoryId: '', material: '', description: '', imageUrl: '' });
    setSkuRows([{ skuCode: '', size: '', color: '', price: '', stock: 50 }]);
    setImageFile(null);
    setImagePreview('');
    setUploadProgress(0);
    const catRes = await catalogApi.getCategories();
    if (catRes.ok) { setCategories(catRes.data || []); if (catRes.data.length) setForm(f => ({...f, categoryId: catRes.data[0].id})); }
    setShowModal(true);
  };

  const handleImageSelect = (file) => {
    if (!file) return;
    if (!file.type.startsWith('image/')) { toast.error('Chỉ chấp nhận file ảnh'); return; }
    if (file.size > 5 * 1024 * 1024) { toast.error('Ảnh tối đa 5MB'); return; }
    setImageFile(file);
    setImagePreview(URL.createObjectURL(file));
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.currentTarget.classList.remove('drag-over');
    const file = e.dataTransfer.files[0];
    handleImageSelect(file);
  };

  const handleCreateProduct = async (e) => {
    e.preventDefault();
    let imageUrl = form.imageUrl;

    // Upload ảnh lên Firebase nếu có chọn file
    if (imageFile) {
      try {
        setUploading(true);
        const { uploadImage } = await import('../firebase');
        imageUrl = await uploadImage(imageFile, setUploadProgress);
      } catch (err) {
        toast.error('Upload ảnh thất bại: ' + (err.message || 'Lỗi'));
        setUploading(false);
        return;
      }
      setUploading(false);
    }

    const res = await adminApi.createProduct({ ...form, categoryId: +form.categoryId, imageUrl });
    if (!res.ok) { toast.error(res.message || 'Lỗi'); return; }
    const pid = res.data.id;
    for (const sku of skuRows) { await adminApi.createSku({ ...sku, productId: pid, price: +sku.price, stock: +sku.stock }); }
    toast.success('Tạo sản phẩm thành công!');
    setShowModal(false); load();
  };

  const handleDelete = async (id) => {
    // Optimistic UI: remove instantly from list
    const backup = [...products];
    setProducts(prev => prev.filter(p => p.id !== id));
    const res = await adminApi.deleteProduct(id);
    if (res.ok) {
      toast.success('Đã xóa sản phẩm');
    } else {
      toast.error(res.message || 'Xóa thất bại');
      setProducts(backup); // rollback if failed
    }
  };

  return (
    <div>
      <div className="admin-panel-header">
        <h1>Quản lý sản phẩm</h1>
        <button className="btn btn-primary btn-sm" onClick={openModal}>+ Thêm sản phẩm</button>
      </div>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead><tr><th>ID</th><th>Ảnh</th><th>Tên</th><th>Danh mục</th><th>Chất liệu</th><th>Biến thể & Kho</th><th>Thao tác</th></tr></thead>
          <tbody>{products.map(p => {
            const totalStock = (p.skus || []).reduce((acc, sku) => acc + (sku.stock || 0), 0);
            return (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>
                  {p.imageUrl
                    ? <img src={p.imageUrl} alt={p.name} className="admin-product-thumb" />
                    : <span className="admin-no-image"><span className="material-symbols-outlined">image</span></span>
                  }
                </td>
                <td><strong>{p.name}</strong></td>
                <td>{p.categoryName || '-'}</td>
                <td>{p.material || '-'}</td>
                <td>
                  <div>{(p.skus || []).length} biến thể</div>
                  <div style={{ fontSize: '11px', marginTop: '3px' }}>
                    {totalStock > 0 ? (
                      <span style={{ color: '#16a34a', fontWeight: 'bold' }}>Còn {totalStock} cái</span>
                    ) : (
                      <span style={{ color: '#dc2626', fontWeight: 'bold' }}>Hết hàng</span>
                    )}
                  </div>
                </td>
                <td><button className="btn-cancel" onClick={() => handleDelete(p.id)}>Xóa</button></td>
              </tr>
            );
          })}</tbody>
        </table>
        {products.length === 0 && <div className="empty-state"><p>Chưa có sản phẩm</p></div>}
      </div>
      {totalPages > 1 && <div className="pagination">{Array.from({length: totalPages}, (_, i) => (
        <button key={i} className={`page-btn ${i === page ? 'active' : ''}`} onClick={() => setPage(i)}>{i+1}</button>
      ))}</div>}


      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>Thêm sản phẩm</h2>
            <form onSubmit={handleCreateProduct}>
              {/* Image Upload */}
              <div className="form-group">
                <label>Ảnh sản phẩm</label>
                <div
                  className={`upload-zone ${imagePreview ? 'has-preview' : ''}`}
                  onDrop={handleDrop}
                  onDragOver={(e) => { e.preventDefault(); e.currentTarget.classList.add('drag-over'); }}
                  onDragLeave={(e) => e.currentTarget.classList.remove('drag-over')}
                  onClick={() => document.getElementById('product-image-input').click()}
                >
                  {imagePreview ? (
                    <div className="upload-preview">
                      <img src={imagePreview} alt="Preview" />
                      <button type="button" className="upload-remove" onClick={(e) => { e.stopPropagation(); setImageFile(null); setImagePreview(''); }}>
                        <span className="material-symbols-outlined">close</span>
                      </button>
                    </div>
                  ) : (
                    <div className="upload-placeholder">
                      <span className="material-symbols-outlined">cloud_upload</span>
                      <p>Kéo thả ảnh vào đây hoặc <strong>nhấn để chọn</strong></p>
                      <span className="upload-hint">JPG, PNG, WebP — tối đa 5MB</span>
                    </div>
                  )}
                  <input
                    id="product-image-input"
                    type="file"
                    accept="image/*"
                    hidden
                    onChange={(e) => handleImageSelect(e.target.files[0])}
                  />
                </div>
                {uploading && (
                  <div className="upload-progress">
                    <div className="upload-progress-bar" style={{ width: `${uploadProgress}%` }} />
                    <span>{uploadProgress}%</span>
                  </div>
                )}
              </div>

              <div className="form-group"><label>Tên *</label><input required value={form.name} onChange={e => setForm({...form, name: e.target.value})} /></div>
              <div className="form-row">
                <div className="form-group"><label>Danh mục</label>
                  <select value={form.categoryId} onChange={e => setForm({...form, categoryId: e.target.value})}>
                    {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
                <div className="form-group"><label>Chất liệu</label><input value={form.material} onChange={e => setForm({...form, material: e.target.value})} /></div>
              </div>
              <div className="form-group"><label>Mô tả</label><textarea value={form.description} onChange={e => setForm({...form, description: e.target.value})} rows={2} /></div>
              <h3>Biến thể (SKU)</h3>
              {skuRows.map((sku, i) => (
                <div key={i} className="sku-row">
                  <input placeholder="Mã SKU" value={sku.skuCode} onChange={e => { const s = [...skuRows]; s[i].skuCode = e.target.value; setSkuRows(s); }} required />
                  <input placeholder="Size" value={sku.size} onChange={e => { const s = [...skuRows]; s[i].size = e.target.value; setSkuRows(s); }} required />
                  <input placeholder="Màu" value={sku.color} onChange={e => { const s = [...skuRows]; s[i].color = e.target.value; setSkuRows(s); }} required />
                  <input placeholder="Giá" type="number" value={sku.price} onChange={e => { const s = [...skuRows]; s[i].price = e.target.value; setSkuRows(s); }} required />
                  <input placeholder="Tồn kho" type="number" value={sku.stock} onChange={e => { const s = [...skuRows]; s[i].stock = e.target.value; setSkuRows(s); }} />
                </div>
              ))}
              <button type="button" className="btn-link" onClick={() => setSkuRows([...skuRows, { skuCode: '', size: '', color: '', price: '', stock: 50 }])}>+ Thêm biến thể</button>
              <div className="modal-actions">
                <button type="button" className="btn btn-outline" onClick={() => setShowModal(false)}>Hủy</button>
                <button type="submit" className="btn btn-primary" disabled={uploading}>
                  {uploading ? 'Đang upload...' : 'Lưu'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

function StaffPanel({ toast }) {
  const [staff, setStaff] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ email: '', password: '', fullName: '', phoneNumber: '' });

  const load = async () => {
    const res = await adminApi.getUsers('STAFF');
    if (res.ok && res.data) setStaff(res.data.content || []);
  };
  useEffect(() => { load(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    const res = await adminApi.createStaff(form);
    if (res.ok) { toast.success('Tạo nhân viên thành công!'); setShowModal(false); load(); }
    else toast.error(res.message || 'Lỗi');
  };

  const handleToggle = async (u) => {
    const res = u.isActive ? await adminApi.deactivateUser(u.id) : await adminApi.activateUser(u.id);
    res.ok ? (toast.success(u.isActive ? 'Đã vô hiệu hóa' : 'Đã kích hoạt'), load()) : toast.error(res.message || 'Lỗi');
  };

  return (
    <div>
      <div className="admin-panel-header">
        <h1>Quản lý nhân viên</h1>
        <button className="btn btn-primary btn-sm" onClick={() => { setForm({ email: '', password: '', fullName: '', phoneNumber: '' }); setShowModal(true); }}>+ Thêm nhân viên</button>
      </div>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead><tr><th>ID</th><th>Email</th><th>Họ tên</th><th>SĐT</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
          <tbody>{staff.map(u => (
            <tr key={u.id}><td>{u.id}</td><td>{u.email}</td><td><strong>{u.fullName}</strong></td><td>{u.phoneNumber || '-'}</td>
            <td>{u.isActive ? <span className="text-green">Hoạt động</span> : <span className="text-red">Vô hiệu</span>}</td>
            <td><button className={u.isActive ? 'btn-cancel' : 'btn-link'} onClick={() => handleToggle(u)}>{u.isActive ? 'Vô hiệu hóa' : 'Kích hoạt'}</button></td></tr>
          ))}</tbody>
        </table>
        {staff.length === 0 && <div className="empty-state"><p>Chưa có nhân viên</p></div>}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>Thêm nhân viên</h2>
            <form onSubmit={handleCreate}>
              <div className="form-group"><label>Email *</label><input type="email" required value={form.email} onChange={e => setForm({...form, email: e.target.value})} /></div>
              <div className="form-group"><label>Mật khẩu *</label><input type="password" required minLength={6} value={form.password} onChange={e => setForm({...form, password: e.target.value})} /></div>
              <div className="form-group"><label>Họ tên *</label><input required value={form.fullName} onChange={e => setForm({...form, fullName: e.target.value})} /></div>
              <div className="form-group"><label>SĐT</label><input value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value})} /></div>
              <div className="modal-actions">
                <button type="button" className="btn btn-outline" onClick={() => setShowModal(false)}>Hủy</button>
                <button type="submit" className="btn btn-primary">Tạo</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

function RevenuePanel({ toast }) {
  const [data, setData] = useState(null);
  const [period, setPeriod] = useState('30d');
  const [loading, setLoading] = useState(false);
  // Chi tiết đơn hàng DELIVERED
  const [orders, setOrders] = useState([]);
  const [orderPage, setOrderPage] = useState(0);
  const [orderTotalPages, setOrderTotalPages] = useState(1);
  const [viewMode, setViewMode] = useState('summary'); // 'summary' | 'detail'

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      const res = await adminApi.getRevenue(period);
      if (res.ok && res.data) setData(res.data);
      else toast.error(res.message || 'Lỗi tải doanh thu');
      setLoading(false);
    };
    load();
  }, [period]);
  useEffect(() => {
    const load = async () => {
      const res = await adminApi.getOrders(orderPage, 15, 'DELIVERED');
      if (res.ok && res.data) { setOrders(res.data.content || []); setOrderTotalPages(res.data.totalPages || 1); }
    };
    load();
  }, [orderPage]);

  return (
    <div>
      <div className="admin-panel-header">
        <h1>Thống kê doanh thu</h1>
        <div style={{ display: 'flex', gap: 8 }}>
          {['7d', '30d', '90d'].map(p => (
            <button key={p} className={`btn btn-sm ${period === p ? 'btn-primary' : 'btn-outline'}`} onClick={() => setPeriod(p)}>
              {p === '7d' ? '7 ngày' : p === '30d' ? '30 ngày' : '90 ngày'}
            </button>
          ))}
        </div>
      </div>
      {loading && <div className="empty-state"><p>Đang tải...</p></div>}
      {data && !loading && (<>
        <div className="revenue-cards">
          <div className="revenue-card"><span className="material-symbols-outlined" style={{ color: '#16a34a', fontSize: 32 }}>payments</span><div className="revenue-card-info"><span className="revenue-card-label">Tổng doanh thu</span><span className="revenue-card-value" style={{ color: '#16a34a' }}>{formatCurrency(data.totalRevenue)}</span></div></div>
          <div className="revenue-card"><span className="material-symbols-outlined" style={{ color: '#2563eb', fontSize: 32 }}>local_shipping</span><div className="revenue-card-info"><span className="revenue-card-label">Đơn đã giao</span><span className="revenue-card-value" style={{ color: '#2563eb' }}>{data.totalOrders}</span></div></div>
          <div className="revenue-card"><span className="material-symbols-outlined" style={{ color: '#d97706', fontSize: 32 }}>trending_up</span><div className="revenue-card-info"><span className="revenue-card-label">TB / ngày</span><span className="revenue-card-value" style={{ color: '#d97706' }}>{formatCurrency(data.avgRevenuePerDay)}</span></div></div>
        </div>

        {/* Toggle view */}
        <div style={{ display: 'flex', gap: 8, margin: '24px 0 16px' }}>
          <button className={`btn btn-sm ${viewMode === 'summary' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setViewMode('summary')}>
            <span className="material-symbols-outlined" style={{ fontSize: 16, marginRight: 4, verticalAlign: 'middle' }}>bar_chart</span> Theo ngày
          </button>
          <button className={`btn btn-sm ${viewMode === 'detail' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setViewMode('detail')}>
            <span className="material-symbols-outlined" style={{ fontSize: 16, marginRight: 4, verticalAlign: 'middle' }}>receipt_long</span> Chi tiết đơn hàng
          </button>
        </div>

        {viewMode === 'summary' && (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead><tr><th>Ngày</th><th>Số đơn</th><th>Doanh thu</th></tr></thead>
              <tbody>
                {(data.dailyRevenue || []).map(d => (<tr key={d.date}><td>{d.date}</td><td>{d.orderCount}</td><td><strong>{formatCurrency(d.revenue)}</strong></td></tr>))}
                {(!data.dailyRevenue || data.dailyRevenue.length === 0) && <tr><td colSpan={3} style={{ textAlign: 'center', padding: 24, color: '#888' }}>Không có dữ liệu</td></tr>}
              </tbody>
            </table>
          </div>
        )}

        {viewMode === 'detail' && (
          <>
            <div className="admin-table-wrap">
              <table className="admin-table">
                <thead><tr><th>Mã đơn</th><th>Khách hàng</th><th>Thanh toán</th><th>Tổng tiền</th><th>Địa chỉ</th><th>Ngày đặt</th></tr></thead>
                <tbody>
                  {orders.map(o => (
                    <tr key={o.id}>
                      <td><strong>#{o.id}</strong></td>
                      <td>{o.userFullName || 'N/A'}</td>
                      <td><span className={`admin-status-badge status-${(o.paymentMethod || '').toLowerCase()}`}>{o.paymentMethod || 'N/A'}</span></td>
                      <td><strong style={{ color: '#16a34a' }}>{formatCurrency(o.totalAmount)}</strong></td>
                      <td style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={o.shippingAddress}>{o.shippingAddress || '-'}</td>
                      <td>{formatDate(o.createdAt)}</td>
                    </tr>
                  ))}
                  {orders.length === 0 && <tr><td colSpan={6} style={{ textAlign: 'center', padding: 24, color: '#888' }}>Không có đơn hàng đã giao</td></tr>}
                </tbody>
              </table>
            </div>
            {orderTotalPages > 1 && <div className="pagination" style={{ marginTop: 16 }}>{Array.from({length: orderTotalPages}, (_, i) => (
              <button key={i} className={`page-btn ${i === orderPage ? 'active' : ''}`} onClick={() => setOrderPage(i)}>{i+1}</button>
            ))}</div>}
          </>
        )}
      </>)}
    </div>
  );
}

function ReviewsPanel({ toast }) {
  const [reviews, setReviews] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [replyingId, setReplyingId] = useState(null);
  const [replyContent, setReplyContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);
  const reload = () => setReloadKey(k => k + 1);
  useEffect(() => {
    const load = async () => {
      const res = await reviewApi.getAll(page, 10);
      if (res.ok && res.data) { setReviews(res.data.content || []); setTotalPages(res.data.totalPages || 1); }
    };
    load();
  }, [page, reloadKey]);
  const handleReply = async (reviewId) => {
    if (!replyContent.trim()) return;
    setSubmitting(true);
    const res = await reviewApi.createReply(reviewId, replyContent.trim());
    if (res.ok) { toast.success('Phản hồi thành công'); setReplyingId(null); setReplyContent(''); reload(); }
    else toast.error(res.message || 'Lỗi');
    setSubmitting(false);
  };
  const handleDeleteReply = async (replyId) => {
    const res = await reviewApi.deleteReply(replyId);
    if (res.ok) { toast.success('Đã xóa phản hồi'); reload(); }
    else toast.error(res.message || 'Lỗi');
  };
  const handleDeleteReview = async (reviewId) => {
    const res = await reviewApi.delete(reviewId);
    if (res.ok) { toast.success('Đã xóa đánh giá'); reload(); }
    else toast.error(res.message || 'Lỗi');
  };
  return (
    <div>
      <div className="admin-panel-header"><h1>Quản lý đánh giá</h1></div>
      <div className="admin-reviews-list">
        {reviews.map(r => (
          <div key={r.id} className="admin-review-card">
            <div className="admin-review-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span className="material-symbols-outlined" style={{ fontSize: 28, color: '#888' }}>account_circle</span>
                <div><strong>{r.userFullName}</strong><span style={{ marginLeft: 8, color: '#d97706' }}>{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span></div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span style={{ fontSize: 12, color: '#888' }}>{formatDate(r.createdAt)}</span>
                <button className="btn-cancel" style={{ fontSize: 12 }} onClick={() => handleDeleteReview(r.id)}>Xóa</button>
              </div>
            </div>
            {r.comment && <p style={{ margin: '8px 0', color: '#444' }}>{r.comment}</p>}
            {r.replies && r.replies.length > 0 && (<div className="admin-replies">
              {r.replies.map(rp => (<div key={rp.id} className="admin-reply-item">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div><strong>{rp.userFullName}</strong><span className={`role-badge role-${rp.userRole?.toLowerCase()}`}>{rp.userRole === 'ADMIN' ? 'Admin' : 'NV'}</span></div>
                  <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}><span style={{ fontSize: 11, color: '#999' }}>{formatDate(rp.createdAt)}</span><button className="btn-cancel" style={{ fontSize: 11 }} onClick={() => handleDeleteReply(rp.id)}>Xóa</button></div>
                </div>
                <p style={{ margin: '4px 0 0', color: '#555' }}>{rp.content}</p>
              </div>))}
            </div>)}
            {replyingId === r.id ? (
              <div className="admin-reply-form">
                <textarea value={replyContent} onChange={e => setReplyContent(e.target.value)} placeholder="Nhập phản hồi..." rows={2} maxLength={500} />
                <div style={{ display: 'flex', gap: 6, marginTop: 6 }}>
                  <button className="btn btn-sm btn-primary" disabled={submitting} onClick={() => handleReply(r.id)}>{submitting ? 'Đang gửi...' : 'Gửi'}</button>
                  <button className="btn btn-sm btn-outline" onClick={() => { setReplyingId(null); setReplyContent(''); }}>Hủy</button>
                </div>
              </div>
            ) : (
              <button className="btn-link" style={{ marginTop: 6, fontSize: 13 }} onClick={() => { setReplyingId(r.id); setReplyContent(''); }}>
                <span className="material-symbols-outlined" style={{ fontSize: 16, verticalAlign: 'middle' }}>reply</span> Phản hồi
              </button>
            )}
          </div>
        ))}
        {reviews.length === 0 && <div className="empty-state"><p>Chưa có đánh giá nào</p></div>}
      </div>
      {totalPages > 1 && <div className="pagination">{Array.from({length: totalPages}, (_, i) => (
        <button key={i} className={`page-btn ${i === page ? 'active' : ''}`} onClick={() => setPage(i)}>{i+1}</button>
      ))}</div>}
    </div>
  );
}

function BannersPanel({ toast }) {
  const [banners, setBanners] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({ title: '', subtitle: '', mediaUrl: '', mediaType: 'IMAGE', thumbnailUrl: '', linkUrl: '', ctaText: '', displayOrder: '' });
  const [mediaFile, setMediaFile] = useState(null);
  const [mediaPreview, setMediaPreview] = useState('');
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  const load = async () => {
    const res = await bannerApi.getAll(page, 20);
    if (res.ok && res.data) { setBanners(res.data.content || []); setTotalPages(res.data.totalPages || 1); }
  };
  useEffect(() => { load(); }, [page]);

  const openCreateModal = () => {
    setEditingId(null);
    setForm({ title: '', subtitle: '', mediaUrl: '', mediaType: 'IMAGE', thumbnailUrl: '', linkUrl: '', ctaText: '', displayOrder: '' });
    setMediaFile(null);
    setMediaPreview('');
    setUploadProgress(0);
    setShowModal(true);
  };

  const openEditModal = (banner) => {
    setEditingId(banner.id);
    setForm({
      title: banner.title || '',
      subtitle: banner.subtitle || '',
      mediaUrl: banner.mediaUrl || '',
      mediaType: banner.mediaType || 'IMAGE',
      thumbnailUrl: banner.thumbnailUrl || '',
      linkUrl: banner.linkUrl || '',
      ctaText: banner.ctaText || '',
      displayOrder: banner.displayOrder ?? '',
    });
    setMediaFile(null);
    setMediaPreview(banner.mediaUrl || '');
    setUploadProgress(0);
    setShowModal(true);
  };

  const handleMediaSelect = (file) => {
    if (!file) return;
    const isVideo = file.type.startsWith('video/');
    const isImage = file.type.startsWith('image/');
    if (!isVideo && !isImage) { toast.error('Chỉ chấp nhận file ảnh hoặc video'); return; }
    if (isImage && file.size > 5 * 1024 * 1024) { toast.error('Ảnh tối đa 5MB'); return; }
    if (isVideo && file.size > 100 * 1024 * 1024) { toast.error('Video tối đa 100MB'); return; }
    setMediaFile(file);
    setMediaPreview(URL.createObjectURL(file));
    setForm(f => ({ ...f, mediaType: isVideo ? 'VIDEO' : 'IMAGE' }));
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.currentTarget.classList.remove('drag-over');
    handleMediaSelect(e.dataTransfer.files[0]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    let mediaUrl = form.mediaUrl;

    if (mediaFile) {
      try {
        setUploading(true);
        const { uploadMedia } = await import('../firebase');
        mediaUrl = await uploadMedia(mediaFile, 'banners', setUploadProgress);
      } catch (err) {
        toast.error('Upload thất bại: ' + (err.message || 'Lỗi'));
        setUploading(false);
        return;
      }
      setUploading(false);
    }

    const payload = {
      ...form,
      mediaUrl,
      displayOrder: form.displayOrder ? +form.displayOrder : null,
    };

    const res = editingId
      ? await bannerApi.update(editingId, payload)
      : await bannerApi.create(payload);

    if (res.ok) {
      toast.success(editingId ? 'Cập nhật banner thành công' : 'Tạo banner thành công');
      setShowModal(false);
      load();
    } else {
      toast.error(res.message || 'Lỗi');
    }
  };

  const handleDelete = async (id) => {
    const backup = [...banners];
    setBanners(prev => prev.filter(b => b.id !== id));
    const res = await bannerApi.delete(id);
    if (res.ok) {
      toast.success('Đã xóa banner');
    } else {
      toast.error(res.message || 'Xóa thất bại');
      setBanners(backup);
    }
  };

  const handleMoveOrder = async (index, direction) => {
    const items = [...banners];
    const target = index + direction;
    if (target < 0 || target >= items.length) return;
    [items[index], items[target]] = [items[target], items[index]];
    setBanners(items);
    const ids = items.map(b => b.id);
    const res = await bannerApi.reorder(ids);
    if (!res.ok) { toast.error('Sắp xếp thất bại'); load(); }
  };

  return (
    <div>
      <div className="admin-panel-header">
        <h1>Quản lý Banner</h1>
        <button className="btn btn-primary btn-sm" onClick={openCreateModal}>+ Thêm banner</button>
      </div>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead><tr><th>Thứ tự</th><th>Preview</th><th>Tiêu đề</th><th>Loại</th><th>CTA</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
          <tbody>
            {banners.map((b, i) => (
              <tr key={b.id}>
                <td>
                  <div style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
                    <button className="btn-link" onClick={() => handleMoveOrder(i, -1)} disabled={i === 0} title="Lên">↑</button>
                    <span>{b.displayOrder}</span>
                    <button className="btn-link" onClick={() => handleMoveOrder(i, 1)} disabled={i === banners.length - 1} title="Xuống">↓</button>
                  </div>
                </td>
                <td>
                  {b.mediaType === 'VIDEO' ? (
                    <video src={b.mediaUrl} muted style={{ width: 80, height: 45, objectFit: 'cover', borderRadius: 4 }} />
                  ) : (
                    <img src={b.mediaUrl} alt={b.title} style={{ width: 80, height: 45, objectFit: 'cover', borderRadius: 4 }} />
                  )}
                </td>
                <td><strong>{b.title}</strong>{b.subtitle && <div style={{ fontSize: 11, color: '#888' }}>{b.subtitle}</div>}</td>
                <td><span className={`admin-status-badge status-${b.mediaType === 'VIDEO' ? 'shipped' : 'confirmed'}`}>{b.mediaType}</span></td>
                <td>{b.ctaText || '-'}</td>
                <td>{b.isActive ? <span className="text-green">Hiển thị</span> : <span className="text-red">Ẩn</span>}</td>
                <td style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                  <button className="btn-link" onClick={() => openEditModal(b)}>Sửa</button>
                  <button className="btn-cancel" onClick={() => handleDelete(b.id)}>Xóa</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {banners.length === 0 && <div className="empty-state"><p>Chưa có banner</p></div>}
      </div>
      {totalPages > 1 && <div className="pagination">{Array.from({length: totalPages}, (_, i) => (
        <button key={i} className={`page-btn ${i === page ? 'active' : ''}`} onClick={() => setPage(i)}>{i+1}</button>
      ))}</div>}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 600 }}>
            <h2>{editingId ? 'Sửa banner' : 'Thêm banner'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Media (Ảnh / Video)</label>
                <div
                  className={`upload-zone ${mediaPreview ? 'has-preview' : ''}`}
                  onDrop={handleDrop}
                  onDragOver={(e) => { e.preventDefault(); e.currentTarget.classList.add('drag-over'); }}
                  onDragLeave={(e) => e.currentTarget.classList.remove('drag-over')}
                  onClick={() => document.getElementById('banner-media-input').click()}
                >
                  {mediaPreview ? (
                    <div className="upload-preview">
                      {form.mediaType === 'VIDEO' ? (
                        <video src={mediaPreview} style={{ maxHeight: 180, maxWidth: '100%', borderRadius: 8 }} muted autoPlay loop playsInline />
                      ) : (
                        <img src={mediaPreview} alt="Preview" />
                      )}
                      <button type="button" className="upload-remove" onClick={(e) => { e.stopPropagation(); setMediaFile(null); setMediaPreview(''); setForm(f => ({...f, mediaUrl: ''})); }}>
                        <span className="material-symbols-outlined">close</span>
                      </button>
                    </div>
                  ) : (
                    <div className="upload-placeholder">
                      <span className="material-symbols-outlined">cloud_upload</span>
                      <p>Kéo thả ảnh hoặc video vào đây hoặc <strong>nhấn để chọn</strong></p>
                      <span className="upload-hint">JPG, PNG, WebP, MP4, WebM — Ảnh tối đa 5MB, Video tối đa 100MB</span>
                    </div>
                  )}
                  <input
                    id="banner-media-input"
                    type="file"
                    accept="image/*,video/*"
                    hidden
                    onChange={(e) => handleMediaSelect(e.target.files[0])}
                  />
                </div>
                {uploading && (
                  <div className="upload-progress">
                    <div className="upload-progress-bar" style={{ width: `${uploadProgress}%` }} />
                    <span>{uploadProgress}%</span>
                  </div>
                )}
              </div>
              <div className="form-group"><label>Tiêu đề *</label><input required value={form.title} onChange={e => setForm({...form, title: e.target.value})} /></div>
              <div className="form-group"><label>Phụ đề</label><input value={form.subtitle} onChange={e => setForm({...form, subtitle: e.target.value})} /></div>
              <div className="form-row">
                <div className="form-group"><label>Loại media</label>
                  <select value={form.mediaType} onChange={e => setForm({...form, mediaType: e.target.value})}>
                    <option value="IMAGE">Ảnh</option>
                    <option value="VIDEO">Video</option>
                  </select>
                </div>
                <div className="form-group"><label>Thứ tự</label><input type="number" value={form.displayOrder} onChange={e => setForm({...form, displayOrder: e.target.value})} placeholder="Tự động" /></div>
              </div>
              <div className="form-row">
                <div className="form-group"><label>Text nút CTA</label><input value={form.ctaText} onChange={e => setForm({...form, ctaText: e.target.value})} placeholder="VD: KHÁM PHÁ NGAY" /></div>
                <div className="form-group"><label>Link URL</label><input value={form.linkUrl} onChange={e => setForm({...form, linkUrl: e.target.value})} placeholder="/shop" /></div>
              </div>
              {form.mediaType === 'VIDEO' && (
                <div className="form-group"><label>Thumbnail URL (poster)</label><input value={form.thumbnailUrl} onChange={e => setForm({...form, thumbnailUrl: e.target.value})} placeholder="URL ảnh poster cho video" /></div>
              )}
              <div className="modal-actions">
                <button type="button" className="btn btn-outline" onClick={() => setShowModal(false)}>Hủy</button>
                <button type="submit" className="btn btn-primary" disabled={uploading}>
                  {uploading ? 'Đang upload...' : (editingId ? 'Cập nhật' : 'Tạo')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

