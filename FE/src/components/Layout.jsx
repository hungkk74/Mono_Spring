import { useState, useEffect, useRef, useCallback } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { catalogApi } from '../api';
import ChatBot from './ChatBot';

export default function Layout({ children }) {
  return (
    <div className="app-layout">
      <Header />
      <main className="main-content">{children}</main>
      <Footer />
      <ChatBot />
    </div>
  );
}

function Header() {
  const { user, isLoggedIn, isAdminOrStaff } = useAuth();
  const { cartCount } = useCart();
  const location = useLocation();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [scrolled, setScrolled] = useState(() => window.scrollY > 8);
  const searchRef = useRef(null);
  const debounceRef = useRef(null);

  const currentSearchParams = new URLSearchParams(location.search);
  const isSalePage = location.pathname === '/shop' && currentSearchParams.get('sale') === 'true';
  const isShopPage = location.pathname === '/shop' && !isSalePage;
  const isActive = (path) => (path === '/shop' ? isShopPage : location.pathname === path);

  // Debounced search
  const handleSearch = useCallback((query) => {
    setSearchQuery(query);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    if (!query.trim()) { setSearchResults([]); return; }
    debounceRef.current = setTimeout(async () => {
      setSearchLoading(true);
      const res = await catalogApi.getProducts(0, 6, null, query.trim());
      if (res.ok && res.data) setSearchResults(res.data.content || []);
      setSearchLoading(false);
    }, 300);
  }, []);

  // Close search on click outside
  useEffect(() => {
    const handler = (e) => {
      if (searchRef.current && !searchRef.current.contains(e.target)) {
        setSearchOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  // Close search on route change
  useEffect(() => { setSearchOpen(false); setSearchQuery(''); setSearchResults([]); }, [location.pathname]);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 8);
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <header className={`header ${scrolled ? 'header-scrolled' : ''}`}>
      <div className="header-inner">
        <Link to="/" className="logo">MONO WEAR</Link>

        <nav className="nav-links">
          <Link to="/" className={`nav-link ${isActive('/') ? 'active' : ''}`}>TRANG CHỦ</Link>
          <Link to="/shop" className={`nav-link ${isActive('/shop') ? 'active' : ''}`}>SẢN PHẨM</Link>
          <Link to="/shop?sale=true" className={`nav-link ${isSalePage ? 'active' : ''}`}>SALE</Link>
          <Link to="/about" className={`nav-link ${isActive('/about') ? 'active' : ''}`}>VỀ CHÚNG TÔI</Link>
        </nav>

        <div className="header-actions">
          <button className="header-icon" title="Tìm kiếm" onClick={() => setSearchOpen(!searchOpen)}>
            <span className="material-symbols-outlined">search</span>
          </button>
          {isAdminOrStaff && (
            <Link to="/admin" className="header-icon" title="Dashboard">
              <span className="material-symbols-outlined">dashboard</span>
            </Link>
          )}
          <Link to={isLoggedIn ? '/account' : '/login'} className="header-icon" title={user?.fullName || 'Đăng nhập'}>
            <span className="material-symbols-outlined">
              {isLoggedIn ? 'account_circle' : 'person'}
            </span>
          </Link>
          <Link to="/cart" className="header-icon cart-icon-wrapper">
            <span className="material-symbols-outlined">shopping_bag</span>
            {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
          </Link>
          <button className="mobile-menu-btn" onClick={() => setMobileOpen(!mobileOpen)}>
            <span className="material-symbols-outlined">{mobileOpen ? 'close' : 'menu'}</span>
          </button>
        </div>
      </div>

      {/* Search Bar */}
      {searchOpen && (
        <div className="search-bar-container" ref={searchRef}>
          <div className="search-bar-inner">
            <span className="material-symbols-outlined search-bar-icon">search</span>
            <input
              type="text"
              className="search-bar-input"
              placeholder="Tìm kiếm sản phẩm..."
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && searchQuery.trim()) {
                  setSearchOpen(false);
                  navigate(`/shop?keyword=${encodeURIComponent(searchQuery.trim())}`);
                }
              }}
              autoFocus
            />
            {searchQuery && (
              <button className="search-bar-clear" onClick={() => { setSearchQuery(''); setSearchResults([]); }}>
                <span className="material-symbols-outlined">close</span>
              </button>
            )}
          </div>
          {(searchResults.length > 0 || searchLoading) && (
            <div className="search-results-dropdown">
              {searchLoading ? (
                <div className="search-loading">Đang tìm kiếm...</div>
              ) : (
                <>
                  {searchResults.map((p) => (
                    <Link to={`/product/${p.slug}`} className="search-result-item" key={p.id}
                      onClick={() => setSearchOpen(false)}>
                      <img src={p.imageUrl || 'https://via.placeholder.com/50'} alt={p.name} className="search-result-img" />
                      <div className="search-result-info">
                        <span className="search-result-name">{p.name}</span>
                        <span className="search-result-cat">{p.categoryName}</span>
                      </div>
                    </Link>
                  ))}
                  <Link to={`/shop?keyword=${encodeURIComponent(searchQuery)}`} className="search-view-all"
                    onClick={() => setSearchOpen(false)}>
                    Xem tất cả kết quả →
                  </Link>
                </>
              )}
            </div>
          )}
          {searchQuery && !searchLoading && searchResults.length === 0 && (
            <div className="search-results-dropdown">
              <div className="search-loading">Không tìm thấy sản phẩm</div>
            </div>
          )}
        </div>
      )}

      {mobileOpen && (
        <div className="mobile-nav">
          <Link to="/" className="mobile-nav-link" onClick={() => setMobileOpen(false)}>TRANG CHỦ</Link>
          <Link to="/shop" className="mobile-nav-link" onClick={() => setMobileOpen(false)}>SẢN PHẨM</Link>
          <Link to="/shop?sale=true" className="mobile-nav-link" onClick={() => setMobileOpen(false)}>SALE</Link>
          <Link to="/about" className="mobile-nav-link" onClick={() => setMobileOpen(false)}>VỀ CHÚNG TÔI</Link>
          <Link to="/cart" className="mobile-nav-link" onClick={() => setMobileOpen(false)}>GIỎ HÀNG</Link>
          {isLoggedIn ? (
            <Link to="/account" className="mobile-nav-link" onClick={() => setMobileOpen(false)}>TÀI KHOẢN</Link>
          ) : (
            <Link to="/login" className="mobile-nav-link" onClick={() => setMobileOpen(false)}>ĐĂNG NHẬP</Link>
          )}
        </div>
      )}
    </header>
  );
}

function Footer() {
  return (
    <footer className="footer" id="footer">
      <div className="footer-inner">
        <div className="footer-brand">
          <div className="footer-logo">MONO WEAR</div>
          <p className="footer-desc">
            Mono Wear là biểu tượng cho sự tối giản tinh khiết, mang lại phong thái tự tin và đẳng cấp cho người đàn ông hiện đại.
          </p>
          <div className="footer-socials">
            <span className="material-symbols-outlined">share</span>
            <span className="material-symbols-outlined">public</span>
            <span className="material-symbols-outlined">alternate_email</span>
          </div>
        </div>
        <div className="footer-col">
          <h4>CHÍNH SÁCH</h4>
          <ul>
            <li><Link to="/privacy">CHÍNH SÁCH BẢO MẬT</Link></li>
            <li><Link to="/terms">ĐIỀU KHOẢN DỊCH VỤ</Link></li>
            <li><Link to="/returns">CHÍNH SÁCH ĐỔI TRẢ</Link></li>
          </ul>
        </div>
        <div className="footer-col">
          <h4>DỊCH VỤ KHÁCH HÀNG</h4>
          <ul>
            <li><Link to="/stores">HỆ THỐNG CỬA HÀNG</Link></li>
            <li><Link to="/contact">LIÊN HỆ</Link></li>
            <li><Link to="/account#orders">TRA CỨU ĐƠN HÀNG</Link></li>
          </ul>
        </div>
        <div className="footer-col">
          <h4>THANH TOÁN</h4>
          <div className="footer-payments">
            <span className="material-symbols-outlined">credit_card</span>
            <span className="material-symbols-outlined">account_balance</span>
            <span className="material-symbols-outlined">contactless</span>
          </div>
          <div className="footer-copyright">© 2024 MONO WEAR. PURE MINIMALISM.</div>
        </div>
      </div>
    </footer>
  );
}
