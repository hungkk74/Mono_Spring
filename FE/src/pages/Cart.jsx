import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useToast } from '../components/Toast';
import { formatCurrency } from '../utils/format';

export default function Cart() {
  const {
    cart, removeFromCart, updateQuantity,
    selectedCount, selectedTotal, allSelected,
    toggleSelect, selectAll, deselectAll, isSelected,
  } = useCart();
  const navigate = useNavigate();
  const toast = useToast();

  if (cart.length === 0) {
    return (
      <div className="cart-page">
        <div className="container">
          <h1 className="page-title">GIỎ HÀNG CỦA BẠN</h1>
          <div className="empty-state">
            <span className="material-symbols-outlined">shopping_bag</span>
            <p>Giỏ hàng trống</p>
            <Link to="/shop" className="btn btn-primary">TIẾP TỤC MUA SẮM</Link>
          </div>
        </div>
      </div>
    );
  }

  const handleCheckout = () => {
    if (selectedCount === 0) {
      toast.error('Vui lòng chọn ít nhất 1 sản phẩm để thanh toán');
      return;
    }
    navigate('/checkout');
  };

  return (
    <div className="cart-page">
      <div className="container">
        <div className="cart-header">
          <h1 className="page-title">GIỎ HÀNG CỦA BẠN</h1>
          <p className="cart-count">({cart.length} sản phẩm)</p>
        </div>

        <div className="cart-layout">
          {/* Items */}
          <div className="cart-items">
            {/* Select all */}
            <div className="cart-select-all">
              <label className="cart-checkbox-label">
                <input
                  type="checkbox"
                  checked={allSelected}
                  onChange={() => allSelected ? deselectAll() : selectAll()}
                />
                <span className="cart-checkbox-custom">
                  {allSelected && <span className="material-symbols-outlined">check</span>}
                </span>
                <span>Chọn tất cả ({cart.length} sản phẩm)</span>
              </label>
            </div>

            {cart.map((item) => {
              const checked = isSelected(item.skuId);
              return (
                <div className={`cart-item ${checked ? '' : 'cart-item-unselected'}`} key={item.skuId}>
                  <label className="cart-item-checkbox">
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={() => toggleSelect(item.skuId)}
                    />
                    <span className="cart-checkbox-custom">
                      {checked && <span className="material-symbols-outlined">check</span>}
                    </span>
                  </label>
                  <div className="cart-item-img">
                    <img src={item.image || 'https://via.placeholder.com/112x144?text=No+Img'} alt={item.productName}
                      onError={(e) => { e.target.src = 'https://via.placeholder.com/112x144?text=No+Img'; }} />
                  </div>
                  <div className="cart-item-details">
                    <div className="cart-item-top">
                      <div>
                        <h3 className="cart-item-name">{item.productName}</h3>
                        <p className="cart-item-variant">{item.color} / {item.size}</p>
                      </div>
                      <button className="cart-item-remove" onClick={() => removeFromCart(item.skuId)}>
                        <span className="material-symbols-outlined">close</span>
                      </button>
                    </div>
                    <div className="cart-item-bottom">
                      <div className="qty-selector">
                        <button onClick={() => item.quantity <= 1 ? removeFromCart(item.skuId) : updateQuantity(item.skuId, item.quantity - 1)}>−</button>
                        <span>{item.quantity}</span>
                        <button onClick={() => updateQuantity(item.skuId, item.quantity + 1)}>+</button>
                      </div>
                      <span className="cart-item-price">{formatCurrency(item.price * item.quantity)}</span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Summary */}
          <aside className="cart-summary">
            <h2 className="cart-summary-title">TÓM TẮT ĐƠN HÀNG</h2>
            <div className="cart-summary-rows">
              <div className="cart-summary-row">
                <span>Đã chọn:</span>
                <span className="bold">{selectedCount} sản phẩm</span>
              </div>
              <div className="cart-summary-row">
                <span>Tạm tính:</span>
                <span className="bold">{formatCurrency(selectedTotal)}</span>
              </div>
              <div className="cart-summary-row">
                <span>Phí vận chuyển:</span>
                <span className="accent">Miễn phí</span>
              </div>
              <hr />
              <div className="cart-summary-row cart-summary-total">
                <span>TỔNG CỘNG:</span>
                <span>{formatCurrency(selectedTotal)}</span>
              </div>
            </div>
            <button
              className="btn btn-primary btn-full"
              onClick={handleCheckout}
              disabled={selectedCount === 0}
            >
              TIẾN HÀNH THANH TOÁN ({selectedCount})
            </button>
            <div className="cart-continue">
              <Link to="/shop">TIẾP TỤC MUA SẮM</Link>
            </div>
          </aside>
        </div>
      </div>
    </div>
  );
}
