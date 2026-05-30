import { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { catalogApi, reviewApi } from '../api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../components/Toast';
import { formatCurrency, getColorHex } from '../utils/format';

function StarRating({ rating, onRate, interactive = false, size = 20 }) {
  return (
    <div className="star-rating" style={{ fontSize: size }}>
      {[1, 2, 3, 4, 5].map((star) => (
        <span
          key={star}
          className={`star ${star <= rating ? 'filled' : ''} ${interactive ? 'interactive' : ''}`}
          onClick={() => interactive && onRate?.(star)}
          style={{ cursor: interactive ? 'pointer' : 'default' }}
        >
          {star <= rating ? '★' : '☆'}
        </span>
      ))}
    </div>
  );
}

export default function ProductDetail() {
  const { slug } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { user, isLoggedIn } = useAuth();
  const toast = useToast();

  const [product, setProduct] = useState(null);
  const [selectedColor, setSelectedColor] = useState(null);
  const [selectedSize, setSelectedSize] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);

  // Reviews state
  const [reviews, setReviews] = useState([]);
  const [reviewStats, setReviewStats] = useState({ totalReviews: 0, averageRating: 0 });
  const [newRating, setNewRating] = useState(5);
  const [newComment, setNewComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!slug) { navigate('/shop'); return; }
    setLoading(true);
    catalogApi.getProductBySlug(slug).then((res) => {
      if (res.ok && res.data) {
        setProduct(res.data);
        const skus = res.data.skus || [];
        const colors = [...new Set(skus.map((s) => s.color).filter(Boolean))];
        const sizes = [...new Set(skus.map((s) => s.size).filter(Boolean))];
        if (colors.length > 0) setSelectedColor(colors[0]);
        if (sizes.length > 0) setSelectedSize(sizes[0]);
        loadReviews(res.data.id);
      }
      setLoading(false);
    });
  }, [slug]);

  const loadReviews = async (productId) => {
    const [reviewsRes, statsRes] = await Promise.all([
      reviewApi.getReviews(productId),
      reviewApi.getStats(productId),
    ]);
    if (reviewsRes.ok) setReviews(reviewsRes.data || []);
    if (statsRes.ok) setReviewStats(statsRes.data || { totalReviews: 0, averageRating: 0 });
  };

  if (loading) return <div className="loading-page"><span className="material-symbols-outlined spin">progress_activity</span></div>;
  if (!product) return <div className="error-page"><h1>Sản phẩm không tìm thấy</h1><Link to="/shop" className="btn btn-primary">Quay lại cửa hàng</Link></div>;

  const skus = product.skus || [];

  // All unique colors and sizes
  const allColors = [...new Set(skus.map((s) => s.color).filter(Boolean))];
  const allSizes = [...new Set(skus.map((s) => s.size).filter(Boolean))];

  // Available sizes for selected color, and available colors for selected size
  const availableSizes = selectedColor
    ? [...new Set(skus.filter((s) => s.color === selectedColor).map((s) => s.size).filter(Boolean))]
    : allSizes;
  const availableColors = selectedSize
    ? [...new Set(skus.filter((s) => s.size === selectedSize).map((s) => s.color).filter(Boolean))]
    : allColors;

  const handleColorChange = (color) => {
    setSelectedColor(color);
    // Auto-select first available size for this color
    const sizesForColor = skus.filter((s) => s.color === color).map((s) => s.size).filter(Boolean);
    if (sizesForColor.length > 0 && !sizesForColor.includes(selectedSize)) {
      setSelectedSize(sizesForColor[0]);
    }
  };

  const handleSizeChange = (size) => {
    setSelectedSize(size);
    // Auto-select first available color for this size
    const colorsForSize = skus.filter((s) => s.size === size).map((s) => s.color).filter(Boolean);
    if (colorsForSize.length > 0 && !colorsForSize.includes(selectedColor)) {
      setSelectedColor(colorsForSize[0]);
    }
  };

  const selectedSku = skus.find((s) => s.color === selectedColor && s.size === selectedSize);
  const displayPrice = selectedSku ? selectedSku.price : (skus.length > 0 ? Math.min(...skus.map((s) => s.price)) : 0);
  const salePrice = product.onSale && displayPrice
    ? Math.round(Number(displayPrice) * (100 - Number(product.salePercent || 0)) / 100)
    : null;
  const checkoutPrice = salePrice || displayPrice;
  const mainImage = product.imageUrl || selectedSku?.imageUrl || skus.find((s) => s.imageUrl)?.imageUrl || '';

  const handleAddToCart = () => {
    if (!selectedSku) { toast.error('Vui lòng chọn màu sắc và kích thước!'); return; }
    if (selectedSku.stock < 1) { toast.error('Sản phẩm đã hết hàng!'); return; }
    addToCart({
      skuId: selectedSku.id, productName: product.name, color: selectedColor,
      size: selectedSize, price: checkoutPrice, originalPrice: selectedSku.price, image: mainImage, quantity,
    });
    toast.success('Đã thêm vào giỏ hàng!');
  };

  const handleBuyNow = () => {
    if (!selectedSku) { toast.error('Vui lòng chọn màu sắc và kích thước!'); return; }
    addToCart({
      skuId: selectedSku.id, productName: product.name, color: selectedColor,
      size: selectedSize, price: checkoutPrice, originalPrice: selectedSku.price, image: mainImage, quantity,
    });
    navigate('/cart');
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!isLoggedIn) { toast.error('Vui lòng đăng nhập để đánh giá'); return; }
    setSubmitting(true);
    try {
      const res = await reviewApi.create({ productId: product.id, rating: newRating, comment: newComment.trim() || null });
      if (res.ok) {
        toast.success('Đánh giá thành công!');
        setNewComment('');
        setNewRating(5);
        loadReviews(product.id);
      } else {
        toast.error(res.message || 'Không thể đánh giá');
      }
    } catch {
      toast.error('Có lỗi xảy ra, vui lòng thử lại');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteReview = async (reviewId) => {
    try {
      const res = await reviewApi.delete(reviewId);
      if (res.ok) {
        toast.success('Đã xóa đánh giá');
        setReviews((prev) => prev.filter((r) => r.id !== reviewId));
        loadReviews(product.id);
      } else {
        toast.error(res.message || 'Xóa thất bại');
      }
    } catch {
      toast.error('Có lỗi xảy ra, vui lòng thử lại');
    }
  };

  // eslint-disable-next-line eqeqeq
  const alreadyReviewed = reviews.some((r) => r.userId == user?.id);

  return (
    <div className="product-detail-page">
      <div className="container">
        {/* Breadcrumb */}
        <nav className="breadcrumb">
          <Link to="/">Trang chủ</Link>
          <span className="material-symbols-outlined breadcrumb-sep">chevron_right</span>
          <Link to="/shop">Sản phẩm</Link>
          <span className="material-symbols-outlined breadcrumb-sep">chevron_right</span>
          <span className="breadcrumb-current">{product.name}</span>
        </nav>

        <div className="product-detail-layout">
          {/* Gallery */}
          <div className="product-gallery">
            <div className="product-main-image">
              <img src={mainImage} alt={product.name}
                onError={(e) => { e.target.src = 'https://via.placeholder.com/600x750?text=No+Image'; }} />
            </div>
          </div>

          {/* Details */}
          <div className="product-info">
            <h1 className="product-name">{product.name}</h1>
            <div className="product-price">
              {salePrice ? (
                <>
                  <span className="price-current">{formatCurrency(salePrice)}</span>
                  <span className="price-old product-price-old">{formatCurrency(displayPrice)}</span>
                  <span className="sale-chip">SALE -{product.salePercent}%</span>
                </>
              ) : (
                formatCurrency(displayPrice)
              )}
            </div>

            {/* Rating summary inline */}
            {reviewStats.totalReviews > 0 && (
              <div className="product-rating-summary">
                <StarRating rating={Math.round(reviewStats.averageRating)} size={18} />
                <span className="rating-text">{reviewStats.averageRating} / 5 ({reviewStats.totalReviews} đánh giá)</span>
              </div>
            )}

            <hr className="divider" />

            {/* Color */}
            {allColors.length > 0 && (
              <div className="variant-section">
                <span className="variant-label">Màu sắc: {selectedColor}</span>
                <div className="variant-options">
                  {allColors.map((c) => (
                    <button key={c} className={`color-option ${c === selectedColor ? 'active' : ''}`}
                      onClick={() => handleColorChange(c)} title={c}>
                      <span style={{ background: getColorHex(c), border: c.toUpperCase() === 'WHITE' ? '1px solid #ddd' : 'none' }} />
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Size */}
            {allSizes.length > 0 && (
              <div className="variant-section">
                <span className="variant-label">Kích thước</span>
                <div className="variant-options">
                  {allSizes.map((s) => (
                    <button key={s}
                      className={`size-option ${s === selectedSize ? 'active' : ''} ${!availableSizes.includes(s) ? 'disabled' : ''}`}
                      onClick={() => availableSizes.includes(s) && handleSizeChange(s)}
                      disabled={!availableSizes.includes(s)}
                    >{s}</button>
                  ))}
                </div>
              </div>
            )}

            {/* Quantity & Actions */}
            <div className="product-actions">
              <div className="qty-selector">
                <button onClick={() => setQuantity(Math.max(1, quantity - 1))}>
                  <span className="material-symbols-outlined">remove</span>
                </button>
                <span>{quantity}</span>
                <button onClick={() => setQuantity(quantity + 1)}>
                  <span className="material-symbols-outlined">add</span>
                </button>
              </div>
              <button className="btn btn-primary btn-full" onClick={handleAddToCart}>THÊM VÀO GIỎ HÀNG</button>
              <button className="btn btn-accent-outline btn-full" onClick={handleBuyNow}>MUA NGAY</button>
            </div>

            {/* SKU Info */}
            <div className="sku-info">
              {selectedSku ? `SKU: ${selectedSku.skuCode} | Còn ${selectedSku.stock} sản phẩm`
                : (selectedColor && selectedSize ? 'Không có biến thể này' : 'Vui lòng chọn màu sắc và kích thước')}
            </div>

            {/* Description */}
            <div className="product-description">
              <h3>Mô tả</h3>
              <div dangerouslySetInnerHTML={{ __html: product.description || 'Chưa có mô tả cho sản phẩm này.' }} />
            </div>
          </div>
        </div>

        {/* ==================== REVIEWS SECTION ==================== */}
        <section className="reviews-section">
          <div className="reviews-header">
            <h2>Đánh giá sản phẩm</h2>
            {reviewStats.totalReviews > 0 && (
              <div className="reviews-summary">
                <span className="reviews-avg">{reviewStats.averageRating}</span>
                <StarRating rating={Math.round(reviewStats.averageRating)} size={22} />
                <span className="reviews-count">{reviewStats.totalReviews} đánh giá</span>
              </div>
            )}
          </div>

          {/* Review Form */}
          {isLoggedIn && !alreadyReviewed && (
            <form className="review-form" onSubmit={handleSubmitReview}>
              <h4>Viết đánh giá của bạn</h4>
              <div className="review-form-rating">
                <span>Đánh giá:</span>
                <StarRating rating={newRating} onRate={setNewRating} interactive size={24} />
              </div>
              <textarea
                className="review-form-comment"
                placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                maxLength={1000}
                rows={4}
              />
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? 'Đang gửi...' : 'Gửi đánh giá'}
              </button>
            </form>
          )}
          {isLoggedIn && alreadyReviewed && (
            <div className="review-already">
              <span className="material-symbols-outlined">check_circle</span>
              Bạn đã đánh giá sản phẩm này
            </div>
          )}
          {!isLoggedIn && (
            <div className="review-login-prompt">
              <Link to="/login">Đăng nhập</Link> để đánh giá sản phẩm
            </div>
          )}

          {/* Review List */}
          <div className="review-list">
            {reviews.length === 0 ? (
              <p className="review-empty">Chưa có đánh giá nào cho sản phẩm này</p>
            ) : (
              reviews.map((r) => (
                <div className="review-item" key={r.id}>
                  <div className="review-item-header">
                    <div className="review-item-user">
                      <span className="material-symbols-outlined">account_circle</span>
                      <strong>{r.userFullName}</strong>
                    </div>
                    <StarRating rating={r.rating} size={16} />
                  </div>
                  {r.comment && <p className="review-item-comment">{r.comment}</p>}
                  <div className="review-item-footer">
                    <span className="review-item-date">
                      {new Date(r.createdAt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })}
                    </span>
                    {user && (user.id === r.userId || user.role === 'ADMIN') && (
                      <button className="review-delete-btn" onClick={() => handleDeleteReview(r.id)}>Xóa</button>
                    )}
                  </div>
                  {/* Replies */}
                  {r.replies && r.replies.length > 0 && (
                    <div className="review-replies">
                      {r.replies.map((rp) => (
                        <div key={rp.id} className="review-reply-item">
                          <div className="review-reply-header">
                            <strong>{rp.userFullName}</strong>
                            <span className={`role-badge role-${rp.userRole?.toLowerCase()}`}>{rp.userRole === 'ADMIN' ? 'Admin' : 'Nhân viên'}</span>
                          </div>
                          <p className="review-reply-content">{rp.content}</p>
                          <span className="review-reply-date">{new Date(rp.createdAt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
