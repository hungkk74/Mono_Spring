import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { catalogApi } from '../api';
import ProductCard, { ProductCardSkeleton } from '../components/ProductCard';

const findCategoryBySlug = (categories, slug) => {
  if (!slug) return null;

  for (const category of categories) {
    if (category.slug === slug) return category;

    const child = findCategoryBySlug(category.children || [], slug);
    if (child) return child;
  }

  return null;
};

export default function Category() {
  const [searchParams, setSearchParams] = useSearchParams();
  const slugParam = searchParams.get('slug');
  const keywordParam = searchParams.get('keyword') || '';
  const saleParam = searchParams.get('sale') === 'true';

  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [totalItems, setTotalItems] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  
  const [tempSizes, setTempSizes] = useState([]);
  const [tempColors, setTempColors] = useState([]);
  const [appliedSizes, setAppliedSizes] = useState([]);
  const [appliedColors, setAppliedColors] = useState([]);

  useEffect(() => {
    let mounted = true;
    catalogApi.getCategories().then((res) => {
      if (mounted && res.ok) setCategories(res.data || []);
    });
    return () => { mounted = false; };
  }, []);

  // Derive current category & title synchronously to avoid race conditions
  let currentCategoryId = null;
  let pageTitle = 'SẢN PHẨM';

  if (saleParam) {
    pageTitle = 'SALE';
  } else if (keywordParam) {
    pageTitle = `Kết quả tìm kiếm: "${keywordParam}"`;
  } else if (slugParam && categories.length > 0) {
    const matchedCategory = findCategoryBySlug(categories, slugParam);
    if (matchedCategory) {
      currentCategoryId = matchedCategory.id;
      pageTitle = matchedCategory.name.toUpperCase();
    }
  }

  // Effect to fetch products when dependencies change
  useEffect(() => {
    // Wait until categories are loaded if a slug is provided, so we can resolve the ID
    if (slugParam && categories.length === 0) return;

    let mounted = true;
    setLoading(true);

    const activeKeyword = keywordParam || null;

    catalogApi.getProducts(currentPage, 21, currentCategoryId, activeKeyword, appliedSizes, appliedColors, saleParam)
      .then((res) => {
        if (!mounted) return;
        if (res.ok && res.data) {
          setProducts(res.data.content || []);
          setTotalItems(res.data.totalItems || 0);
          setTotalPages(res.data.totalPages || 1);
        }
      })
      .catch((err) => {
        if (!mounted) return;
        console.error('Failed to load products:', err);
        setProducts([]);
        setTotalItems(0);
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });

    return () => { mounted = false; };
  }, [currentPage, currentCategoryId, appliedSizes, appliedColors, keywordParam, saleParam, slugParam, categories.length]);

  // Reset pagination when category, keyword, or sale filter changes
  useEffect(() => {
    setCurrentPage(0);
  }, [slugParam, keywordParam, saleParam]);

  const resetFilters = () => {
    setTempSizes([]);
    setTempColors([]);
    setAppliedSizes([]);
    setAppliedColors([]);
    setCurrentPage(0);
  };

  const navigateShop = (params = {}) => {
    resetFilters();
    setSearchParams(params);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const applyFilters = () => {
    setAppliedSizes(tempSizes);
    setAppliedColors(tempColors);
    setCurrentPage(0);
  };

  const toggleSize = (size) => {
    setTempSizes((prev) => (
      prev.includes(size) ? prev.filter((item) => item !== size) : [...prev, size]
    ));
  };

  const toggleColor = (color) => {
    setTempColors((prev) => (
      prev.includes(color) ? prev.filter((item) => item !== color) : [...prev, color]
    ));
  };

  const handleCategoryClick = (category) => {
    navigateShop(category ? { slug: category.slug } : {});
  };

  const clearKeyword = () => {
    navigateShop({});
  };

  const isAllProductsActive = !currentCategoryId && !saleParam && !keywordParam;

  return (
    <div className="shop-page">
      <div className="container">
        <nav className="breadcrumb">
          <Link to="/">Trang chủ</Link>
          <span className="material-symbols-outlined breadcrumb-sep">chevron_right</span>
          <span className="breadcrumb-current">{pageTitle}</span>
        </nav>

        <div className="shop-header">
          <div>
            <h1 className="page-title">{pageTitle}</h1>
            <p className="product-count">{totalItems} sản phẩm</p>
          </div>
          {keywordParam && (
            <button
              className="btn btn-accent-outline"
              onClick={clearKeyword}
              style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px' }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>close</span>
              Xóa bộ lọc tìm kiếm
            </button>
          )}
        </div>

        <div className="shop-layout">
          <aside className="shop-sidebar">
            <div className="filter-section">
              <h3 className="filter-title">Danh mục</h3>
              <ul className="filter-list">
                <li>
                  <button
                    className={`filter-cat-btn ${saleParam ? 'active' : ''}`}
                    onClick={() => navigateShop({ sale: 'true' })}
                  >
                    SALE
                  </button>
                </li>
                <li>
                  <button
                    className={`filter-cat-btn ${isAllProductsActive ? 'active' : ''}`}
                    onClick={() => handleCategoryClick(null)}
                  >
                    Tất cả sản phẩm
                  </button>
                </li>
                {categories.map((category) => (
                  <li key={category.id}>
                    <button
                      className={`filter-cat-btn ${currentCategoryId === category.id ? 'active' : ''}`}
                      onClick={() => handleCategoryClick(category)}
                    >
                      {category.name}
                    </button>
                    {category.children && category.children.length > 0 && (
                      <ul className="filter-sublist">
                        {category.children.map((child) => (
                          <li key={child.id}>
                            <button
                              className={`filter-cat-btn filter-cat-sub ${currentCategoryId === child.id ? 'active' : ''}`}
                              onClick={() => handleCategoryClick(child)}
                            >
                              {child.name}
                            </button>
                          </li>
                        ))}
                      </ul>
                    )}
                  </li>
                ))}
              </ul>
            </div>

            <div className="filter-section">
              <h3 className="filter-title">Kích cỡ</h3>
              <div className="size-filter-grid">
                {['S', 'M', 'L', 'XL', 'XXL'].map((size) => (
                  <button
                    key={size}
                    className={`size-filter-btn ${tempSizes.includes(size) ? 'active' : ''}`}
                    onClick={() => toggleSize(size)}
                  >
                    {size}
                  </button>
                ))}
              </div>
            </div>

            <div className="filter-section">
              <h3 className="filter-title">Màu sắc</h3>
              <div className="color-filter-grid">
                {[
                  { name: 'Đen', hex: '#000' },
                  { name: 'Trắng', hex: '#fff' },
                  { name: 'Navy', hex: '#001F3F' },
                  { name: 'Xám', hex: '#808080' },
                ].map((color) => (
                  <button
                    key={color.name}
                    className={`color-filter-btn ${tempColors.includes(color.name) ? 'active' : ''}`}
                    style={{ background: color.hex, border: color.hex === '#fff' ? '1px solid #ddd' : 'none' }}
                    title={color.name}
                    onClick={() => toggleColor(color.name)}
                  />
                ))}
              </div>
            </div>

            <button className="btn btn-primary btn-full" onClick={applyFilters} style={{ marginTop: '20px' }}>
              LỌC SẢN PHẨM
            </button>
          </aside>

          <section className="shop-products">
            {loading ? (
              <div className="product-grid product-grid-3">
                {Array.from({ length: 9 }, (_, index) => (
                  <ProductCardSkeleton key={index} />
                ))}
              </div>
            ) : products.length === 0 ? (
              <div className="empty-state">
                <span className="material-symbols-outlined">search_off</span>
                <p>Không tìm thấy sản phẩm phù hợp</p>
              </div>
            ) : (
              <>
                <div className="product-grid product-grid-3">
                  {products.map((product) => (
                    <ProductCard product={product} key={product.id || product.slug} />
                  ))}
                </div>

                {totalPages > 1 && (
                  <div className="pagination">
                    <button
                      className="page-btn page-arrow"
                      disabled={currentPage === 0}
                      onClick={() => {
                        setCurrentPage((page) => page - 1);
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                      }}
                    >
                      <span className="material-symbols-outlined">chevron_left</span>
                    </button>

                    {Array.from({ length: totalPages }, (_, index) => (
                      <button
                        key={index}
                        className={`page-btn ${index === currentPage ? 'active' : ''}`}
                        onClick={() => {
                          setCurrentPage(index);
                          window.scrollTo({ top: 0, behavior: 'smooth' });
                        }}
                      >
                        {index + 1}
                      </button>
                    ))}

                    <button
                      className="page-btn page-arrow"
                      disabled={currentPage === totalPages - 1}
                      onClick={() => {
                        setCurrentPage((page) => page + 1);
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                      }}
                    >
                      <span className="material-symbols-outlined">chevron_right</span>
                    </button>
                  </div>
                )}
              </>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}
