import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { orderApi } from '../api';
import { useCart } from '../context/CartContext';
import { useToast } from '../components/Toast';
import { formatCurrency, formatDate, getStatusInfo } from '../utils/format';

const paymentLabels = {
  COD: 'Thanh toán khi nhận hàng (COD)',
  MOMO: 'Ví MoMo',
  BANK_TRANSFER: 'Chuyển khoản ngân hàng',
};

const STATUS_STEPS = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED'];
const STATUS_STEP_LABELS = {
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  SHIPPED: 'Đang giao',
  DELIVERED: 'Đã giao',
};

const REORDERABLE = ['DELIVERED', 'CANCELLED'];

export default function OrderDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const toast = useToast();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelling, setCancelling] = useState(false);
  const [reordering, setReordering] = useState(false);

  useEffect(() => {
    loadOrder();
  }, [id]);

  const loadOrder = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await orderApi.getOrderDetail(id);
      if (res.ok && res.data) {
        setOrder(res.data);
      } else {
        setError(res.message || 'Không tìm thấy đơn hàng');
      }
    } catch {
      setError('Lỗi kết nối');
    }
    setLoading(false);
  };

  const handleCancel = async () => {
    if (!confirm('Bạn chắc chắn muốn hủy đơn #' + id + '?')) return;
    setCancelling(true);
    const res = await orderApi.cancelOrder(id);
    if (res.ok) {
      setOrder(res.data);
    }
    setCancelling(false);
  };

  const handleReorder = async () => {
    setReordering(true);
    try {
      const res = await orderApi.reorder(id);
      if (!res.ok) { toast.error(res.message || 'Lỗi mua lại'); setReordering(false); return; }
      const items = res.data || [];
      const available = items.filter(i => i.available);
      const unavailable = items.filter(i => !i.available);
      if (available.length === 0) {
        toast.error('Tất cả sản phẩm trong đơn đã hết hàng hoặc ngừng kinh doanh');
        setReordering(false);
        return;
      }
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
    setReordering(false);
  };

  if (loading) {
    return (
      <div className="order-detail-page">
        <div className="container">
          <div className="loading-state">
            <span className="material-symbols-outlined spin">progress_activity</span>
            <p>Đang tải đơn hàng...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="order-detail-page">
        <div className="container">
          <div className="empty-state">
            <span className="material-symbols-outlined">error_outline</span>
            <p>{error || 'Không tìm thấy đơn hàng'}</p>
            <button className="btn btn-outline" style={{ marginTop: 16 }} onClick={() => navigate('/account#orders')}>
              Quay lại đơn hàng
            </button>
          </div>
        </div>
      </div>
    );
  }

  const st = getStatusInfo(order.status);
  const isCancelled = order.status === 'CANCELLED';
  const canReorder = REORDERABLE.includes(order.status);
  const currentStepIndex = isCancelled ? -1 : STATUS_STEPS.indexOf(order.status);

  return (
    <div className="order-detail-page">
      <div className="container">
        {/* Breadcrumb */}
        <div className="breadcrumb">
          <Link to="/account#orders">Đơn hàng</Link>
          <span className="breadcrumb-sep">/</span>
          <span className="breadcrumb-current">#{order.id}</span>
        </div>

        {/* Header */}
        <div className="od-header">
          <div className="od-header-left">
            <h1 className="od-title">Đơn hàng #{order.id}</h1>
            <p className="od-date">
              <span className="material-symbols-outlined">calendar_today</span>
              Đặt ngày {formatDate(order.createdAt)}
            </p>
          </div>
          <span className={`order-status-badge ${st.cls}`}>{st.label}</span>
        </div>

        {/* Status Progress */}
        {!isCancelled && (
          <div className="od-progress">
            {STATUS_STEPS.map((step, i) => {
              const done = i <= currentStepIndex;
              const active = i === currentStepIndex;
              return (
                <div className={`od-step ${done ? 'done' : ''} ${active ? 'active' : ''}`} key={step}>
                  <div className="od-step-dot">
                    {done && <span className="material-symbols-outlined">check</span>}
                  </div>
                  <span className="od-step-label">{STATUS_STEP_LABELS[step]}</span>
                  {i < STATUS_STEPS.length - 1 && <div className={`od-step-line ${done ? 'done' : ''}`} />}
                </div>
              );
            })}
          </div>
        )}

        {isCancelled && (
          <div className="od-cancelled-banner">
            <span className="material-symbols-outlined">cancel</span>
            Đơn hàng đã bị hủy
          </div>
        )}

        {/* Content */}
        <div className="od-layout">
          {/* Items */}
          <div className="od-items-card">
            <h2 className="od-card-title">
              <span className="material-symbols-outlined">inventory_2</span>
              Sản phẩm ({(order.items || []).length} mặt hàng)
            </h2>
            <div className="od-items-list">
              {(order.items || []).map((item, i) => (
                <div className="od-item" key={i}>
                  <div className="od-item-info">
                    <p className="od-item-name">{item.productName}</p>
                    <p className="od-item-variant">
                      {item.color && <span>{item.color}</span>}
                      {item.color && item.size && <span> / </span>}
                      {item.size && <span>{item.size}</span>}
                    </p>
                    <p className="od-item-qty">Số lượng: {item.quantity}</p>
                  </div>
                  <div className="od-item-prices">
                    <p className="od-item-unit">{formatCurrency(item.unitPrice)}</p>
                    <p className="od-item-subtotal">{formatCurrency(item.subtotal)}</p>
                  </div>
                </div>
              ))}
            </div>

            {/* Totals */}
            <div className="od-totals">
              <div className="od-total-row">
                <span>Tạm tính</span>
                <span>{formatCurrency(order.subtotalAmount)}</span>
              </div>
              {order.discountAmount > 0 && (
                <div className="od-total-row discount">
                  <span>Giảm giá {order.couponCode && `(${order.couponCode})`}</span>
                  <span>-{formatCurrency(order.discountAmount)}</span>
                </div>
              )}
              <hr />
              <div className="od-total-row total">
                <span>Tổng cộng</span>
                <span>{formatCurrency(order.totalAmount)}</span>
              </div>
            </div>
          </div>

          {/* Sidebar info */}
          <div className="od-sidebar">
            <div className="od-info-card">
              <h3 className="od-info-title">
                <span className="material-symbols-outlined">local_shipping</span>
                Địa chỉ giao hàng
              </h3>
              <p className="od-info-text">{order.shippingAddress || 'Chưa có'}</p>
            </div>

            <div className="od-info-card">
              <h3 className="od-info-title">
                <span className="material-symbols-outlined">payments</span>
                Phương thức thanh toán
              </h3>
              <p className="od-info-text">{paymentLabels[order.paymentMethod] || order.paymentMethod}</p>
            </div>

            {canReorder && (
              <button
                className="btn btn-primary btn-full od-reorder-btn"
                onClick={handleReorder}
                disabled={reordering}
              >
                <span className="material-symbols-outlined">replay</span>
                {reordering ? 'Đang xử lý...' : 'Mua lại đơn hàng'}
              </button>
            )}

            {order.status === 'PENDING' && (
              <button
                className="btn btn-full od-cancel-btn"
                onClick={handleCancel}
                disabled={cancelling}
              >
                <span className="material-symbols-outlined">close</span>
                {cancelling ? 'Đang hủy...' : 'Hủy đơn hàng'}
              </button>
            )}

            <Link to="/account#orders" className="btn btn-outline btn-full od-back-btn">
              <span className="material-symbols-outlined">arrow_back</span>
              Quay lại danh sách
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
