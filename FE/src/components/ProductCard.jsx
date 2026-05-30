import { Link } from 'react-router-dom';
import { useState } from 'react';
import { formatCurrency } from '../utils/format';

export default function ProductCard({
  product,
  className = '',
  price = null,
  oldPrice = null,
  badge = null,
  cta = 'Xem chi tiết',
  onImageError,
}) {
  const [imageBroken, setImageBroken] = useState(false);

  if (!product?.slug || !product?.imageUrl || imageBroken || product.isActive === false) return null;

  const currentPrice = product.salePrice || price || product.minPrice || null;
  const originalPrice = product.onSale ? (product.minPrice || price) : oldPrice;
  const showSale = product.onSale && currentPrice && originalPrice && Number(currentPrice) < Number(originalPrice);
  const cardClasses = ['product-card', showSale ? 'is-sale' : '', className].filter(Boolean).join(' ');

  return (
    <Link to={`/product/${product.slug}`} className={cardClasses}>
      {(badge || showSale) && <div className="product-badge">{badge || `SALE -${product.salePercent}%`}</div>}
      <div className="product-card-img">
        <img
          src={product.imageUrl}
          alt={product.name}
          loading="lazy"
          onError={() => {
            setImageBroken(true);
            onImageError?.();
          }}
        />
        <div className="product-card-overlay">
          <span>{cta}</span>
        </div>
      </div>
      <div className="product-card-info">
        {product.categoryName && <p className="product-card-category">{product.categoryName}</p>}
        <h3 className="product-card-name">{product.name}</h3>
        {currentPrice && (
          <div className="product-card-price">
            <span className="price-current">{formatCurrency(currentPrice)}</span>
            {showSale && <span className="price-old">{formatCurrency(originalPrice)}</span>}
          </div>
        )}
      </div>
    </Link>
  );
}

export function ProductCardSkeleton({ className = '' }) {
  return (
    <div className={`product-card product-card-skeleton ${className}`.trim()} aria-hidden="true">
      <div className="product-card-img skeleton-box" />
      <div className="product-card-info">
        <div className="skeleton-line skeleton-line-sm" />
        <div className="skeleton-line" />
        <div className="skeleton-line skeleton-line-xs" />
      </div>
    </div>
  );
}
