import { useMemo, useRef, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useToast } from '../components/Toast';
import { couponApi, orderApi, paymentApi } from '../api';
import { formatCurrency } from '../utils/format';

export default function Checkout() {
  const { user } = useAuth();
  const { selectedCart, selectedTotal, selectedCount, toOrderItems, clearSelected } = useCart();
  const navigate = useNavigate();
  const toast = useToast();
  const allowLeaveRef = useRef(false);

  const [form, setForm] = useState({ note: '' });
  const [paymentMethod, setPaymentMethod] = useState('COD');
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [couponLoading, setCouponLoading] = useState(false);
  const [loading, setLoading] = useState(false);
  const [showSuccessPopup, setShowSuccessPopup] = useState(false);
  const [showPaymentConfirm, setShowPaymentConfirm] = useState(false);
  const [placedOrderId, setPlacedOrderId] = useState(null);
  const [vietQrData, setVietQrData] = useState(null);

  useEffect(() => {
    if (selectedCart.length === 0 && !placedOrderId && !showSuccessPopup) navigate('/cart');
  }, [selectedCart.length, navigate, placedOrderId, showSuccessPopup]);

  const checkoutForm = useMemo(() => ({
    name: form.name ?? user?.fullName ?? '',
    phone: form.phone ?? user?.phoneNumber ?? '',
    email: form.email ?? user?.email ?? '',
    address: form.address ?? user?.address ?? '',
    note: form.note ?? '',
  }), [form, user]);

  const shouldBlockLeaving = selectedCart.length > 0 && !placedOrderId && !showSuccessPopup && !loading;

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const discountAmount = appliedCoupon?.discountAmount ? Number(appliedCoupon.discountAmount) : 0;
  const payableTotal = Math.max(0, selectedTotal - discountAmount);

  const applyCoupon = async () => {
    if (!couponCode.trim()) { toast.error('Vui lòng nhập mã giảm giá'); return; }
    setCouponLoading(true);
    const res = await couponApi.apply(couponCode.trim(), selectedTotal);
    if (res.ok) {
      setAppliedCoupon(res.data);
      setCouponCode(res.data.code);
      toast.success('Đã áp dụng mã giảm giá');
    } else {
      setAppliedCoupon(null);
      toast.error(res.message || 'Mã giảm giá không hợp lệ');
    }
    setCouponLoading(false);
  };

  const removeCoupon = () => {
    setAppliedCoupon(null);
    setCouponCode('');
  };

  useEffect(() => {
    const handleBeforeUnload = (e) => {
      if (!shouldBlockLeaving || allowLeaveRef.current) return;
      e.preventDefault();
      e.returnValue = '';
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [shouldBlockLeaving]);

  useEffect(() => {
    const handleLinkClick = (e) => {
      if (!shouldBlockLeaving || allowLeaveRef.current) return;

      const link = e.target.closest?.('a[href]');
      if (!link) return;

      const url = new URL(link.href, window.location.origin);
      if (url.origin !== window.location.origin || url.pathname === window.location.pathname) return;

      if (!window.confirm('Bạn có chắc muốn rời trang thanh toán? Thông tin đang nhập có thể bị mất.')) {
        e.preventDefault();
        e.stopPropagation();
      }
    };

    document.addEventListener('click', handleLinkClick, true);
    return () => document.removeEventListener('click', handleLinkClick, true);
  }, [shouldBlockLeaving]);

  const requestPaymentConfirmation = () => {
    if (!checkoutForm.address) { toast.error('Vui lòng nhập địa chỉ giao hàng!'); return; }
    setShowPaymentConfirm(true);
  };

  const handleSubmit = async () => {
    if (!checkoutForm.address) { toast.error('Vui lòng nhập địa chỉ giao hàng!'); return; }
    setShowPaymentConfirm(false);
    setLoading(true);
    try {
      const items = toOrderItems();
      const res = await orderApi.placeOrder(checkoutForm.address, paymentMethod, items, appliedCoupon?.code || null);
      if (res.ok) {
        const orderId = res.data.id;

        // If MoMo — redirect to MoMo payment page
        if (paymentMethod === 'MOMO') {
          const momoRes = await paymentApi.createMomo(orderId, 'payWithMethod');
          if (momoRes.ok && momoRes.data?.payUrl) {
            clearSelected();
            allowLeaveRef.current = true;
            window.location.href = momoRes.data.payUrl;
            return;
          } else {
            toast.error('Không thể tạo thanh toán MoMo: ' + (momoRes.message || 'Lỗi'));
            setLoading(false);
            return;
          }
        }

        if (paymentMethod === 'BANK_TRANSFER') {
          const payOsRes = await paymentApi.createPayOs(orderId);
          if (payOsRes.ok && payOsRes.data?.checkoutUrl) {
            clearSelected();
            allowLeaveRef.current = true;
            window.location.href = payOsRes.data.checkoutUrl;
            return;
          }

          toast.error('Không thể tạo thanh toán payOS: ' + (payOsRes.message || 'Lỗi'));
          setLoading(false);
          return;
        }

        // COD — show success popup
        setVietQrData(null);
        setPlacedOrderId(orderId);
        setShowSuccessPopup(true);
        clearSelected();
      } else {
        toast.error('Lỗi: ' + (res.message || 'Không rõ nguyên nhân'));
      }
    } catch {
      toast.error('Lỗi hệ thống khi đặt hàng');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="checkout-page">
      <div className="container">
        <div className="checkout-header">
          <h1 className="page-title">THANH TOÁN</h1>
          <div className="checkout-steps">
            <span className="step done">1. GIỎ HÀNG <span className="material-symbols-outlined">check</span></span>
            <span className="material-symbols-outlined">chevron_right</span>
            <span className="step active">2. THANH TOÁN</span>
            <span className="material-symbols-outlined">chevron_right</span>
            <span className="step">3. HOÀN TẤT</span>
          </div>
        </div>

        <div className="checkout-layout">
          {/* Form */}
          <div className="checkout-form-col">
            {/* Shipping */}
            <section className="checkout-section">
              <h2 className="checkout-section-title">
                <span className="step-number">01</span> THÔNG TIN GIAO HÀNG
              </h2>
              <div className="form-grid">
                <div className="form-group full">
                  <label>Họ và tên</label>
                  <input name="name" value={checkoutForm.name} onChange={handleChange} placeholder="NGUYỄN VĂN A" />
                </div>
                <div className="form-group">
                  <label>Số điện thoại</label>
                  <input name="phone" value={checkoutForm.phone} onChange={handleChange} placeholder="0901 234 567" />
                </div>
                <div className="form-group">
                  <label>Email</label>
                  <input name="email" type="email" value={checkoutForm.email} onChange={handleChange} placeholder="EXAMPLE@MAIL.COM" />
                </div>
                <div className="form-group full">
                  <label>Địa chỉ giao hàng *</label>
                  <input name="address" value={checkoutForm.address} onChange={handleChange} placeholder="Số nhà, tên đường, quận/huyện, tỉnh/TP" />
                </div>
                <div className="form-group full">
                  <label>Ghi chú (tùy chọn)</label>
                  <textarea name="note" value={checkoutForm.note} onChange={handleChange} placeholder="Lời nhắn cho người bán..." rows={2} />
                </div>
              </div>
            </section>

            {/* Payment */}
            <section className="checkout-section">
              <h2 className="checkout-section-title">
                <span className="step-number">02</span> PHƯƠNG THỨC THANH TOÁN
              </h2>
              <div className="payment-options">
                {[
                  { value: 'COD', icon: 'payments', label: 'COD (Thanh toán khi nhận hàng)' },
                  { value: 'BANK_TRANSFER', icon: 'qr_code_2', label: 'Chuyển khoản ngân hàng bằng QR' },
                  { value: 'MOMO', icon: 'wallet', label: 'Thanh toán MoMo (QR / ATM / Visa)' },
                ].map((opt) => (
                  <label key={opt.value} className={`payment-option ${paymentMethod === opt.value ? 'selected' : ''}`}>
                    <input type="radio" name="payment" value={opt.value} checked={paymentMethod === opt.value}
                      onChange={(e) => setPaymentMethod(e.target.value)} />
                    <span className="material-symbols-outlined">{opt.icon}</span>
                    <span>{opt.label}</span>
                  </label>
                ))}
              </div>
            </section>
          </div>

          {/* Order Summary */}
          <aside className="checkout-summary">
            <h2 className="cart-summary-title">ĐƠN HÀNG CỦA BẠN</h2>
            <div className="checkout-items">
              {selectedCart.map((item) => (
                <div className="checkout-item" key={item.skuId}>
                  <div className="checkout-item-img">
                    <img src={item.image || 'https://via.placeholder.com/80x112'} alt={item.productName} />
                  </div>
                  <div className="checkout-item-info">
                    <h3>{item.productName}</h3>
                    <p>MÀU: {item.color} / SIZE: {item.size}</p>
                    <div className="checkout-item-row">
                      <span>{item.quantity} x {formatCurrency(item.price)}</span>
                      <span className="bold">{formatCurrency(item.price * item.quantity)}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            <div className="cart-summary-rows">
              <div className="cart-summary-row"><span>Tạm tính ({selectedCount})</span><span>{formatCurrency(selectedTotal)}</span></div>
              {appliedCoupon && (
                <div className="cart-summary-row">
                  <span>Mã {appliedCoupon.code}</span>
                  <span className="accent">-{formatCurrency(discountAmount)}</span>
                </div>
              )}
              <div className="cart-summary-row"><span>Phí vận chuyển</span><span className="accent">Miễn phí</span></div>
              <hr />
              <div className="cart-summary-row cart-summary-total"><span>TỔNG CỘNG</span><span>{formatCurrency(payableTotal)}</span></div>
            </div>
            <div className="coupon-box">
              <div className="coupon-input-row">
                <input
                  value={couponCode}
                  onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                  placeholder="Mã giảm giá"
                  disabled={couponLoading || !!appliedCoupon}
                />
                {appliedCoupon ? (
                  <button type="button" onClick={removeCoupon}>Xóa</button>
                ) : (
                  <button type="button" onClick={applyCoupon} disabled={couponLoading}>
                    {couponLoading ? '...' : 'Áp dụng'}
                  </button>
                )}
              </div>
              <p>Gợi ý: MONO10, WELCOME50</p>
            </div>
            <button className="btn btn-primary btn-full" onClick={requestPaymentConfirmation} disabled={loading}>
              {loading ? 'ĐANG XỬ LÝ...' : 'ĐẶT HÀNG'}
            </button>
            <div className="checkout-secure">
              <span className="material-symbols-outlined">lock</span> THANH TOÁN BẢO MẬT 256-BIT SSL
            </div>
          </aside>
        </div>
      </div>

      {showPaymentConfirm && (
        <div className="modal-overlay">
          <div className="modal payment-result-card" style={{ padding: '36px 32px' }}>
            <h2 style={{ marginBottom: '12px' }}>Xác nhận thanh toán</h2>
            <p className="payment-result-msg" style={{ textAlign: 'left', marginBottom: '16px' }}>
              Kiểm tra lại thông tin trước khi đặt hàng. Sau khi xác nhận, hệ thống sẽ tạo đơn và chuyển sang cổng thanh toán nếu bạn chọn MoMo hoặc chuyển khoản QR.
            </p>
            <div className="vietqr-info" style={{ marginBottom: '8px' }}>
              <div><span>Người nhận</span><strong>{checkoutForm.name || 'N/A'}</strong></div>
              <div><span>Địa chỉ</span><strong>{checkoutForm.address}</strong></div>
              <div><span>Thanh toán</span><strong>{paymentMethod === 'BANK_TRANSFER' ? 'Chuyển khoản QR qua payOS' : paymentMethod}</strong></div>
              <div><span>Tổng tiền</span><strong>{formatCurrency(payableTotal)}</strong></div>
            </div>
            <div className="payment-result-actions" style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '24px' }}>
              <button className="btn btn-accent-outline" onClick={() => setShowPaymentConfirm(false)} disabled={loading}>Kiểm tra lại</button>
              <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
                {loading ? 'Đang xử lý...' : 'Xác nhận thanh toán'}
              </button>
            </div>
          </div>
        </div>
      )}

      {showSuccessPopup && (
        <div className="modal-overlay">
          <div className="modal payment-result-card" style={{ padding: '48px 40px', textAlign: 'center' }}>
            <span className="material-symbols-outlined payment-result-icon success" style={{ fontSize: '64px', color: '#16a34a', marginBottom: '20px' }}>
              {vietQrData ? 'qr_code_2' : 'check_circle'}
            </span>
            <h2 style={{ fontSize: '24px', fontWeight: '700', marginBottom: '12px' }}>
              {vietQrData ? 'Quét mã VietQR để chuyển khoản' : 'Đặt hàng thành công!'}
            </h2>
            {vietQrData ? (
              <div className="vietqr-payment">
                <img src={vietQrData.qrDataURL} alt="Mã VietQR chuyển khoản" className="vietqr-image" />
                <div className="vietqr-info">
                  <div><span>Ngân hàng</span><strong>{vietQrData.bankName || vietQrData.bankCode || 'MB'}</strong></div>
                  <div><span>Số tài khoản</span><strong>{vietQrData.accountNo}</strong></div>
                  <div><span>Chủ tài khoản</span><strong>{vietQrData.accountName}</strong></div>
                  <div><span>Số tiền</span><strong>{formatCurrency(Number(vietQrData.amount || 0))}</strong></div>
                  <div><span>Nội dung</span><strong>{vietQrData.addInfo}</strong></div>
                </div>
                <p className="payment-result-msg">
                  Vui lòng chuyển khoản đúng số tiền và nội dung để đơn hàng được xác nhận nhanh hơn.
                </p>
              </div>
            ) : (
              <p className="payment-result-msg" style={{ marginBottom: '16px' }}>
                Đơn hàng của bạn đã được xác nhận. Chúng tôi sẽ sớm liên hệ để giao hàng.
              </p>
            )}
            {placedOrderId && <p className="payment-result-order" style={{ marginBottom: '24px' }}>Mã đơn hàng: <strong>{placedOrderId}</strong></p>}

            <div className="payment-result-actions" style={{ display: 'flex', gap: '12px', justifyContent: 'center' }}>
              <button className="btn btn-primary" onClick={() => navigate('/account#orders')}>Xem đơn hàng</button>
              <button className="btn btn-accent-outline" onClick={() => navigate('/shop')}>Tiếp tục mua sắm</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
