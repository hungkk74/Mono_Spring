import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { paymentApi } from '../api';

export default function PaymentResult() {
  const [searchParams] = useSearchParams();
  const resultCode = searchParams.get('resultCode');
  const extraData = searchParams.get('extraData');
  const orderId = searchParams.get('orderId');
  const payOsOrderCode = searchParams.get('orderCode');
  const payOsCode = searchParams.get('code');
  const payOsStatus = searchParams.get('status');
  const payOsCancel = searchParams.get('cancel');
  const calledRef = useRef(false);
  const [processed, setProcessed] = useState(false);

  const isPayOs = Boolean(payOsOrderCode);
  const isSuccess = isPayOs
    ? payOsCode === '00' && payOsCancel !== 'true' && payOsStatus !== 'CANCELLED'
    : resultCode === '0';
  const transactionId = orderId || payOsOrderCode;

  useEffect(() => {
    if (calledRef.current) return;
    calledRef.current = true;

    const allParams = {};
    searchParams.forEach((v, k) => { allParams[k] = v; });
    console.log('[Payment Redirect] params:', allParams);

    if (payOsOrderCode) {
      paymentApi.payOsCallback(payOsOrderCode)
        .then(() => setProcessed(true))
        .catch(() => setProcessed(true));
      return;
    }

    if (resultCode === null) {
      setProcessed(true);
      return;
    }

    let internalOrderId = extraData || '';
    if (!internalOrderId && orderId && orderId.startsWith('MONO-')) {
      const parts = orderId.split('-');
      if (parts.length >= 2) internalOrderId = parts[1];
    }

    paymentApi.momoCallback(parseInt(resultCode), internalOrderId, orderId || '')
      .then(() => setProcessed(true))
      .catch(() => setProcessed(true));
  }, []);

  return (
    <div className="payment-result-page">
      <div className="container">
        <div className="payment-result-card">
          <span className={`material-symbols-outlined payment-result-icon ${isSuccess ? 'success' : 'error'}`}>
            {isSuccess ? 'check_circle' : 'cancel'}
          </span>
          <h1>{isSuccess ? 'Thanh toán thành công!' : 'Thanh toán thất bại'}</h1>
          <p className="payment-result-msg">
            {isSuccess
              ? 'Đơn hàng của bạn đã được xác nhận. Cảm ơn bạn đã mua sắm tại Mono Wear!'
              : 'Giao dịch đã bị hủy hoặc gặp lỗi. Đơn hàng sẽ được cập nhật theo trạng thái thanh toán.'}
          </p>
          {transactionId && (
            <p className="payment-result-order">Mã giao dịch: <strong>{transactionId}</strong></p>
          )}
          <div className="payment-result-actions">
            <Link to="/account#orders" className="btn btn-primary">Xem đơn hàng</Link>
            <Link to="/shop" className="btn btn-accent-outline">Tiếp tục mua sắm</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
