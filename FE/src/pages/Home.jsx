import { Link } from 'react-router-dom';
import { useEffect, useRef, useState } from 'react';
import { catalogApi, bannerApi } from '../api';
import ProductCard, { ProductCardSkeleton } from '../components/ProductCard';

const FALLBACK_SLIDES = [
  {
    title: 'BỘ SƯU TẬP MỚI 2026',
    subtitle: 'Phong cách tối giản - Chất lượng vượt trội',
    ctaText: 'KHÁM PHÁ NGAY',
    mediaUrl: 'https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=1920&q=90&fit=crop&crop=center',
    mediaType: 'IMAGE',
  },
  {
    title: 'ESSENTIAL MENSWEAR',
    subtitle: 'Những thiết kế nền tảng cho tủ đồ hiện đại',
    ctaText: 'XEM BỘ SƯU TẬP',
    mediaUrl: 'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1920&q=90&fit=crop&crop=center',
    mediaType: 'IMAGE',
  },
  {
    title: 'SMART CASUAL',
    subtitle: 'Lịch sự, gọn gàng và linh hoạt trong mọi lịch trình',
    ctaText: 'MUA NGAY',
    mediaUrl: 'https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1920&q=90&fit=crop&crop=center',
    mediaType: 'IMAGE',
  },
  {
    title: 'SUMMER BASICS',
    subtitle: 'Chất liệu thoáng nhẹ cho ngày dài năng động',
    ctaText: 'KHÁM PHÁ',
    mediaUrl: 'https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=1920&q=90&fit=crop&crop=center',
    mediaType: 'IMAGE',
  },
];

const CATEGORY_IMAGES = [
  { slug: 'ao-polo', name: 'ÁO POLO', img: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDG7yBZFm9vvXCotLI1OELQAMGjNOg_43oG4g1ALDKgVRy_CqIqVXpnxnQhvELJ1lC4a8mIYhMYEcqeJlezfd-CsVXmD02rHyjLzjq67Mge9Bimk_-Z_zSMVAH3Ktq488XeBdegbtzsJhKmB8LE8BXZQLB16NtgCIsRs651nzFJb82iRIDHaVi35YbeyaeksjHwGpLMjE1QqDCCVw93U0B2Y1EcwSm3b__0l6_ijajwgyFB1mboc6v4UhI0PEtMQVThv0a3hUA-zuk' },
  { slug: 'ao-thun', name: 'ÁO THUN', img: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBC6E0LXtge6jEZPlyM358ny7ZASF1uUMo0CIYeJ7nERAKZHMp6lPLDLsco5LLl_vGFWG8HVKoxN3OyvyoN3opQdJZNF-OEgyRSQQgm_v_mz-KU7_RhizrURsU0Jf15UOGGnT4juUNlEbX48BmzekBgprdjoVJ2_fUk8nX995HlHL0o4R9daAkMNM3lYLDtrYGQilie6SVeSAOyOMeUxMXCMtzrZ8eCmVPXeIQ_Ix8PpPnpe3KanvD4Vce5xztw9vTKIJGNbySTmyc' },
  { slug: 'quan', name: 'QUẦN', img: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAeAaZek-j1qWx_JuvzvmuJ5t3ul3BgjqmRmkVx4F71e2dGEPp7hEfvUXUi3-aRVhZA4mnPmZknDkeSTrV9GwgYrAXVXF65wDYzn0L8byIpRaJGT6TcL9di-DkB9eAXrFDTm6tQslyWK17AJWLAR7jF36IlbxLufk1CoNJlLNY5P8t4_XMhojlDiGJdY6jyQzXG-w-MxTsXkef_Ykyv5oJshFFO1w_6vnhzI7wVVlk_bQl9gp5cb7x-zLPO27BoC5UkEpVK1hfsDW8' },
  { slug: 'phu-kien', name: 'PHỤ KIỆN', img: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDoqj0yahKwB9UjCWtt27PPMhbCYgZD-yxUjmwk9pDDZajZFsXp6wAHztSt5-Ze1K1AesTzvdBumxmmHFR5OBcQ7f5jWpown6GoOZFfa7EViB1xAQAPX87yLCYnxznEUpQlRifE3bHaWAa_Mb28vTabvUxvayQ8poroXfAn57oIrYMZTKPPbvi3xR4Y0PDn6tWDh9DlhbCDhSVHV5g64w27HAyuLg6HKzx_iYWRpdpI7tQYgYO0x7mLzMl2E9mxPEWMczMXYQA-CMU' },
  { slug: 'quan-ao-the-thao', name: 'QUẦN ÁO THỂ THAO', img: 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=900&q=80' },
  { slug: 'giay-nam', name: 'GIÀY NAM', img: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=900&q=80' },
  { slug: 'giay-nu', name: 'GIÀY NỮ', img: 'https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=900&q=80' },
  { slug: 'dong-ho', name: 'ĐỒNG HỒ', img: 'https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=900&q=80' },
];

const hasValidImageUrl = (product) => typeof product.imageUrl === 'string' && product.imageUrl.trim().length > 0;
const isVisibleProduct = (product) => hasValidImageUrl(product) && product.isActive !== false && !product.deleted && !product.isDeleted;

const getProductPrice = (product) => {
  const activeSku = product.skus?.find((sku) => sku.isActive !== false && sku.price);
  return activeSku?.price ? Number(activeSku.price) : null;
};

export default function Home() {
  const [activeHero, setActiveHero] = useState(0);
  const [heroSlides, setHeroSlides] = useState(FALLBACK_SLIDES);
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [saleProducts, setSaleProducts] = useState([]);
  const [featuredLoading, setFeaturedLoading] = useState(true);
  const [saleLoading, setSaleLoading] = useState(true);
  const [brokenImageSlugs, setBrokenImageSlugs] = useState([]);
  const featuredRef = useRef(null);
  const saleRef = useRef(null);
  const featuredAnimationRef = useRef(null);
  const saleAnimationRef = useRef(null);
  const visibleFeaturedProducts = featuredProducts.filter((product) => (
    isVisibleProduct(product) && !brokenImageSlugs.includes(product.slug)
  ));
  const visibleSaleProducts = saleProducts.filter((product) => (
    isVisibleProduct(product) && !brokenImageSlugs.includes(product.slug)
  ));

  useEffect(() => {
    const timer = window.setInterval(() => {
      setActiveHero((current) => (current + 1) % heroSlides.length);
    }, 5000);

    return () => {
      window.clearInterval(timer);
      if (featuredAnimationRef.current) window.cancelAnimationFrame(featuredAnimationRef.current);
      if (saleAnimationRef.current) window.cancelAnimationFrame(saleAnimationRef.current);
    };
  }, [heroSlides.length]);

  // Fetch banners from API
  useEffect(() => {
    let mounted = true;
    const loadBanners = async () => {
      try {
        const res = await bannerApi.getPublic();
        if (mounted && res.ok && res.data && res.data.length > 0) {
          setHeroSlides(res.data);
          setActiveHero(0);
        }
      } catch (err) {
        console.error('Failed to load banners:', err);
      }
    };
    loadBanners();
    return () => { mounted = false; };
  }, []);


  useEffect(() => {
    let mounted = true;

    const loadFeaturedProducts = async () => {
      try {
        const res = await catalogApi.getProducts(0, 24);
        if (!mounted || !res.ok || !res.data) return;

        const activeProducts = (res.data.content || []).filter(isVisibleProduct).slice(0, 16);
        const detailedProducts = await Promise.all(
          activeProducts.map(async (product) => {
            const detailRes = await catalogApi.getProductBySlug(product.slug);
            return detailRes.ok && detailRes.data ? detailRes.data : product;
          })
        );

        if (mounted) setFeaturedProducts(detailedProducts.filter(isVisibleProduct));
      } catch (err) {
        console.error('Failed to load featured products:', err);
        if (mounted) setFeaturedProducts([]);
      } finally {
        if (mounted) setFeaturedLoading(false);
      }
    };

    loadFeaturedProducts();

    return () => { mounted = false; };
  }, []);

  useEffect(() => {
    let mounted = true;

    const loadSaleProducts = async () => {
      try {
        const res = await catalogApi.getProducts(0, 8, null, null, [], [], true);
        if (mounted && res.ok && res.data) {
          setSaleProducts((res.data.content || []).filter(isVisibleProduct).slice(0, 8));
        }
      } catch (err) {
        console.error('Failed to load sale products:', err);
        if (mounted) setSaleProducts([]);
      } finally {
        if (mounted) setSaleLoading(false);
      }
    };

    loadSaleProducts();

    return () => { mounted = false; };
  }, []);

  const goToHero = (direction) => {
    setActiveHero((current) => (current + direction + heroSlides.length) % heroSlides.length);
  };

  const scrollCarousel = (carouselRef, animationRef, direction) => {
    const carousel = carouselRef.current;
    if (!carousel) return;

    const cards = carousel.querySelectorAll('.featured-product-card');
    const firstCard = cards[0];
    const styles = window.getComputedStyle(carousel);
    const gap = Number.parseFloat(styles.columnGap || styles.gap) || 0;
    const cardWidth = firstCard?.getBoundingClientRect().width || carousel.clientWidth;
    const step = cardWidth + gap;
    const maxScroll = carousel.scrollWidth - carousel.clientWidth;
    const currentIndex = Math.round(carousel.scrollLeft / step);
    const targetIndex = Math.max(0, Math.min(currentIndex + direction, cards.length - 1));
    const start = carousel.scrollLeft;
    const target = Math.max(0, Math.min(targetIndex * step, maxScroll));
    const duration = 1000;
    const startedAt = window.performance.now();
    const originalSnapType = carousel.style.scrollSnapType;

    if (animationRef.current) window.cancelAnimationFrame(animationRef.current);
    carousel.style.scrollSnapType = 'none';

    const animate = (time) => {
      const progress = Math.min((time - startedAt) / duration, 1);
      const eased = progress < 0.5
        ? 4 * progress * progress * progress
        : 1 - Math.pow(-2 * progress + 2, 3) / 2;
      carousel.scrollLeft = start + (target - start) * eased;

      if (progress < 1) {
        animationRef.current = window.requestAnimationFrame(animate);
      } else {
        carousel.scrollLeft = target;
        carousel.style.scrollSnapType = originalSnapType;
      }
    };

    animationRef.current = window.requestAnimationFrame(animate);
  };

  return (
    <>
      <section className="hero">
        <div className="hero-bg">
          {heroSlides.map((slide, index) => (
            slide.mediaType === 'VIDEO' ? (
              <video
                src={slide.mediaUrl}
                className={index === activeHero ? 'active' : ''}
                key={slide.title + index}
                muted
                autoPlay
                loop
                playsInline
                poster={slide.thumbnailUrl || undefined}
              />
            ) : (
              <img
                src={slide.mediaUrl || slide.img}
                alt={slide.title}
                className={index === activeHero ? 'active' : ''}
                key={slide.title + index}
              />
            )
          ))}
        </div>

        <button className="hero-nav hero-nav-prev" type="button" onClick={() => goToHero(-1)} aria-label="Banner trước">
          <span className="material-symbols-outlined">chevron_left</span>
        </button>
        <button className="hero-nav hero-nav-next" type="button" onClick={() => goToHero(1)} aria-label="Banner tiếp theo">
          <span className="material-symbols-outlined">chevron_right</span>
        </button>

        <div className="hero-content">
          <h1 className="hero-title">{heroSlides[activeHero]?.title}</h1>
          <p className="hero-subtitle">{heroSlides[activeHero]?.subtitle}</p>
          <Link to={heroSlides[activeHero]?.linkUrl || '/shop'} className="btn btn-primary btn-lg">
            {heroSlides[activeHero]?.ctaText || heroSlides[activeHero]?.cta || 'XEM NGAY'}
          </Link>
        </div>

        <div className="hero-dots" aria-label="Chọn banner">
          {heroSlides.map((slide, index) => (
            <button
              className={`hero-dot ${index === activeHero ? 'active' : ''}`}
              type="button"
              onClick={() => setActiveHero(index)}
              aria-label={`Xem banner ${index + 1}: ${slide.title}`}
              key={slide.title + index}
            />
          ))}
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="category-grid">
            {CATEGORY_IMAGES.map((cat) => (
              <Link
                to={`/shop?slug=${cat.slug}`}
                className="category-card"
                key={cat.slug}
              >
                <div className="category-card-img">
                  <img src={cat.img} alt={cat.name} loading="lazy" />
                </div>
                <h3 className="category-card-title">{cat.name}</h3>
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="section bg-white">
        <div className="container">
          <h2 className="section-title">SẢN PHẨM NỔI BẬT</h2>

          <div className="featured-carousel-shell">
            <button className="featured-side-nav featured-side-nav-prev" type="button" onClick={() => scrollCarousel(featuredRef, featuredAnimationRef, -1)} aria-label="Sản phẩm trước">
              <span className="material-symbols-outlined">chevron_left</span>
            </button>

            <div className="featured-carousel" ref={featuredRef}>
              {featuredLoading
                ? Array.from({ length: 4 }, (_, index) => (
                  <ProductCardSkeleton className="featured-product-card" key={index} />
                ))
                : visibleFeaturedProducts.map((p) => (
                  <ProductCard
                    product={p}
                    price={getProductPrice(p)}
                    className="featured-product-card"
                    key={p.id || p.slug}
                    onImageError={() => setBrokenImageSlugs((prev) => (
                      prev.includes(p.slug) ? prev : [...prev, p.slug]
                    ))}
                  />
                ))}
            </div>

            <button className="featured-side-nav featured-side-nav-next" type="button" onClick={() => scrollCarousel(featuredRef, featuredAnimationRef, 1)} aria-label="Sản phẩm tiếp theo">
              <span className="material-symbols-outlined">chevron_right</span>
            </button>
          </div>

          <div className="section-cta">
            <Link to="/shop" className="btn btn-outline">Xem tất cả sản phẩm</Link>
          </div>
        </div>
      </section>

      <section className="section sale-section">
        <div className="container">
          <div className="section-heading-with-actions">
            <h2 className="section-title">SALE ĐANG DIỄN RA</h2>
            <Link to="/shop?sale=true" className="btn btn-outline btn-sm">Xem tất cả</Link>
          </div>

          {saleLoading ? (
            <div className="featured-carousel-shell sale-carousel-shell">
              <div className="featured-carousel sale-carousel">
                {Array.from({ length: 4 }, (_, index) => (
                  <ProductCardSkeleton className="featured-product-card sale-product-card" key={index} />
                ))}
              </div>
            </div>
          ) : visibleSaleProducts.length > 0 ? (
            <div className="featured-carousel-shell sale-carousel-shell">
              <button className="featured-side-nav featured-side-nav-prev sale-side-nav" type="button" onClick={() => scrollCarousel(saleRef, saleAnimationRef, -1)} aria-label="Sản phẩm sale trước">
                <span className="material-symbols-outlined">chevron_left</span>
              </button>

              <div className="featured-carousel sale-carousel" ref={saleRef}>
                {visibleSaleProducts.map((product) => (
                  <ProductCard
                    product={product}
                    className="featured-product-card sale-product-card"
                    key={product.id || product.slug}
                    onImageError={() => setBrokenImageSlugs((prev) => (
                      prev.includes(product.slug) ? prev : [...prev, product.slug]
                    ))}
                  />
                ))}
              </div>

              <button className="featured-side-nav featured-side-nav-next sale-side-nav" type="button" onClick={() => scrollCarousel(saleRef, saleAnimationRef, 1)} aria-label="Sản phẩm sale tiếp theo">
                <span className="material-symbols-outlined">chevron_right</span>
              </button>
            </div>
          ) : (
            <div className="empty-state">
              <span className="material-symbols-outlined">local_offer</span>
              <p>Chưa có sản phẩm sale</p>
            </div>
          )}
        </div>
      </section>

      <section className="club-banner">
        <div className="container club-banner-inner">
          <div className="club-banner-text">
            <h2 className="club-banner-title">ĐĂNG KÝ MW CLUB</h2>
            <p className="club-banner-desc">Tích điểm mỗi đơn hàng - Nhận ưu đãi độc quyền dành riêng cho hội viên.</p>
          </div>
          <Link to="/register" className="btn btn-accent btn-lg">THAM GIA NGAY</Link>
        </div>
      </section>
    </>
  );
}
