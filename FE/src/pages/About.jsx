import { Link } from 'react-router-dom';
import { useEffect, useRef, useState } from 'react';

const STATS = [
  { number: '10+', label: 'Năm kinh nghiệm' },
  { number: '50K+', label: 'Khách hàng tin dùng' },
  { number: '15+', label: 'Cửa hàng toàn quốc' },
  { number: '200+', label: 'Mẫu thiết kế mỗi năm' },
];

const VALUES = [
  {
    icon: 'eco',
    title: 'Bền Vững',
    desc: 'Chúng tôi cam kết sử dụng chất liệu thân thiện với môi trường, quy trình sản xuất có trách nhiệm và bao bì tái chế 100%.',
  },
  {
    icon: 'diamond',
    title: 'Chất Lượng',
    desc: 'Mỗi sản phẩm đều trải qua quy trình kiểm định nghiêm ngặt 5 bước, đảm bảo chất lượng vượt trội từ chất liệu đến đường may.',
  },
  {
    icon: 'palette',
    title: 'Thiết Kế Tối Giản',
    desc: 'Lấy cảm hứng từ triết lý "Less is More", mỗi thiết kế đều hướng đến sự tinh tế, thanh lịch và vượt thời gian.',
  },
  {
    icon: 'handshake',
    title: 'Tận Tâm',
    desc: 'Đội ngũ chăm sóc khách hàng luôn sẵn sàng hỗ trợ 24/7, mang đến trải nghiệm mua sắm hoàn hảo nhất.',
  },
];

const TEAM = [
  {
    name: 'Nguyễn Minh Đức',
    role: 'Founder & CEO',
    img: '/images/team/ceo.png',
    quote: '"Tối giản không phải là thiếu đi, mà là đủ đầy trong sự tinh tế."',
    bio: 'Với hơn 12 năm kinh nghiệm trong ngành thời trang, Minh Đức thành lập Mono Wear năm 2014 với tầm nhìn mang phong cách tối giản đẳng cấp đến người Việt. Anh từng làm việc tại các thương hiệu quốc tế trước khi quyết định xây dựng thương hiệu riêng.',
    experience: ['12+ năm trong ngành thời trang', 'Cựu Fashion Director tại Zara Vietnam', 'Tốt nghiệp ĐH Mỹ thuật Công nghiệp HN', 'Forbes 30 Under 30 Vietnam 2018'],
    email: 'duc.nguyen@monowear.vn',
  },
  {
    name: 'Trần Thị Hương',
    role: 'Creative Director',
    img: '/images/team/creative-director.png',
    quote: '"Mỗi sản phẩm là một câu chuyện — được kể bằng đường may và chất liệu."',
    bio: 'Hương là linh hồn sáng tạo của Mono Wear. Với nền tảng thiết kế tại Milan và kinh nghiệm làm việc với nhiều thương hiệu cao cấp, cô mang đến góc nhìn toàn cầu kết hợp bản sắc Việt trong từng thiết kế.',
    experience: ['Tốt nghiệp Istituto Marangoni, Milan', '8+ năm kinh nghiệm thiết kế', 'Giải Nhất Vietnam Fashion Design 2019', 'Hợp tác với 10+ thương hiệu quốc tế'],
    email: 'huong.tran@monowear.vn',
  },
  {
    name: 'Lê Quang Huy',
    role: 'Head of Production',
    img: '/images/team/production-head.png',
    quote: '"Chất lượng không bao giờ là ngẫu nhiên — nó là kết quả của sự tận tâm."',
    bio: 'Huy quản lý toàn bộ chuỗi cung ứng và sản xuất của Mono Wear. Với triết lý "Zero Defect", anh đã xây dựng hệ thống kiểm soát chất lượng 5 bước được nhiều đối tác quốc tế công nhận.',
    experience: ['15+ năm trong quản lý sản xuất', 'Chứng chỉ Lean Six Sigma Black Belt', 'Quản lý 3 nhà máy sản xuất', 'Giảm tỷ lệ lỗi sản phẩm xuống 0.5%'],
    email: 'huy.le@monowear.vn',
  },
  {
    name: 'Phạm Thanh Mai',
    role: 'Marketing Manager',
    img: '/images/team/marketing-mgr.png',
    quote: '"Thương hiệu mạnh được xây dựng từ sự chân thành và nhất quán."',
    bio: 'Mai dẫn dắt chiến lược marketing và truyền thông thương hiệu Mono Wear. Cô đã thành công đưa Mono Wear trở thành thương hiệu thời trang tối giản số 1 Việt Nam trên các nền tảng số.',
    experience: ['Thạc sĩ Marketing tại ĐH RMIT', '7+ năm trong Digital Marketing', 'Phát triển cộng đồng 200K+ followers', 'Google Digital Marketing Certificate'],
    email: 'mai.pham@monowear.vn',
  },
];

const MILESTONES = [
  { year: '2014', title: 'Khởi đầu', desc: 'Mono Wear ra đời tại Tp.HCM với cửa hàng đầu tiên rộng 30m² và một đam mê cháy bỏng về thời trang tối giản.' },
  { year: '2016', title: 'Bước ngoặt', desc: 'Mở rộng lên 5 cửa hàng, ra mắt bộ sưu tập Essentials được giới truyền thông đánh giá cao.' },
  { year: '2019', title: 'Vươn tầm', desc: 'Đạt mốc 25K khách hàng thành viên, ra mắt nền tảng thương mại điện tử và mở rộng ra miền Trung, miền Bắc.' },
  { year: '2022', title: 'Cam kết xanh', desc: 'Chuyển đổi toàn bộ sang chất liệu bền vững, bao bì thân thiện môi trường và chương trình thu hồi quần áo cũ.' },
  { year: '2024', title: 'Hiện tại', desc: 'Hơn 15 cửa hàng toàn quốc, 50K+ khách hàng tin dùng và tiếp tục sứ mệnh "Pure Minimalism".' },
];

function useIntersectionObserver() {
  const ref = useRef(null);
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('about-visible');
          }
        });
      },
      { threshold: 0.15 }
    );
    const elements = ref.current?.querySelectorAll('.about-animate');
    elements?.forEach((el) => observer.observe(el));
    return () => observer.disconnect();
  }, []);
  return ref;
}

export default function About() {
  const pageRef = useIntersectionObserver();
  const [selectedMember, setSelectedMember] = useState(null);

  return (
    <div className="about-page" ref={pageRef}>
      {/* Hero Banner */}
      <section className="about-hero">
        <div className="about-hero-overlay" />
        <div className="about-hero-content about-animate">
          <span className="about-hero-tag">VỀ CHÚNG TÔI</span>
          <h1 className="about-hero-title">MONO WEAR</h1>
          <p className="about-hero-subtitle">
            Pure Minimalism — Tôn vinh vẻ đẹp thuần khiết của sự tối giản, kiến tạo phong cách bền vững cho người đàn ông hiện đại.
          </p>
          <div className="about-hero-scroll">
            <span className="material-symbols-outlined">expand_more</span>
          </div>
        </div>
      </section>

      {/* Brand Story */}
      <section className="about-section">
        <div className="container">
          <div className="about-story about-animate">
            <div className="about-story-img">
              <img
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuC4JV_N_CuuihA8ehJ7LUCJZ59cP2rWHj8krkp1w1wwsU0WW3mGEEiiL40o4nSX5P_n6gdVuWqx2vZmnajSrBpP6nDcv2Z3lHjOqR63wFfK3iS1VhQY7Xo-lHbgnClLTiAhFMKmd9LBftVF5wCXbGEOcA-u3xPQAAkl5hNU8bFKj4d9sb0iSmLNlknRGcnyET2PQIEi3OgR_cwi_4qDmiWMr1oLAlJOEphDLcGI5DKGaAjoQQ5Vu3c_UjOkAlIR1zzbYMLzgTIipAs"
                alt="Mono Wear Workshop"
              />
            </div>
            <div className="about-story-text">
              <span className="about-section-tag">CÂU CHUYỆN THƯƠNG HIỆU</span>
              <h2 className="about-section-title">Hơn Cả Thời Trang</h2>
              <p>
                Mono Wear không chỉ là một thương hiệu thời trang — chúng tôi là một triết lý sống. Được thành lập năm 2014 bởi những con người yêu sự tối giản, Mono Wear ra đời với sứ mệnh mang đến cho người đàn ông Việt Nam những sản phẩm chất lượng cao, thiết kế tinh tế và giá thành hợp lý.
              </p>
              <p>
                Chúng tôi tin rằng phong cách không đến từ sự phô trương, mà từ sự tinh tế trong từng chi tiết. Mỗi sản phẩm Mono Wear đều là kết tinh của sự am hiểu sâu sắc về chất liệu, kỹ thuật may đo và xu hướng thời trang toàn cầu.
              </p>
              <div className="about-story-signature">
                <div className="signature-line" />
                <span>Đội ngũ sáng lập Mono Wear</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Stats */}
      <section className="about-stats">
        <div className="container">
          <div className="about-stats-grid about-animate">
            {STATS.map((s) => (
              <div className="about-stat-item" key={s.label}>
                <span className="about-stat-number">{s.number}</span>
                <span className="about-stat-label">{s.label}</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Core Values */}
      <section className="about-section about-section-light">
        <div className="container">
          <div className="about-section-header about-animate">
            <span className="about-section-tag">GIÁ TRỊ CỐT LÕI</span>
            <h2 className="about-section-title">Điều Chúng Tôi Theo Đuổi</h2>
            <p className="about-section-desc">
              Bốn giá trị cốt lõi định hình mọi quyết định của chúng tôi, từ thiết kế đến sản xuất, từ bán hàng đến chăm sóc khách hàng.
            </p>
          </div>
          <div className="about-values-grid">
            {VALUES.map((v, i) => (
              <div className="about-value-card about-animate" key={v.title} style={{ transitionDelay: `${i * 0.1}s` }}>
                <div className="about-value-icon">
                  <span className="material-symbols-outlined">{v.icon}</span>
                </div>
                <h3>{v.title}</h3>
                <p>{v.desc}</p>
                <div className="about-value-line" />
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Timeline / Milestones */}
      <section className="about-section">
        <div className="container">
          <div className="about-section-header about-animate">
            <span className="about-section-tag">HÀNH TRÌNH</span>
            <h2 className="about-section-title">Dấu Mốc Quan Trọng</h2>
          </div>
          <div className="about-timeline">
            {MILESTONES.map((m, i) => (
              <div className={`about-timeline-item about-animate ${i % 2 === 0 ? 'left' : 'right'}`} key={m.year} style={{ transitionDelay: `${i * 0.12}s` }}>
                <div className="about-timeline-dot" />
                <div className="about-timeline-content">
                  <span className="about-timeline-year">{m.year}</span>
                  <h3>{m.title}</h3>
                  <p>{m.desc}</p>
                </div>
              </div>
            ))}
            <div className="about-timeline-line" />
          </div>
        </div>
      </section>

      {/* Team */}
      <section className="about-section about-section-light">
        <div className="container">
          <div className="about-section-header about-animate">
            <span className="about-section-tag">ĐỘI NGŨ</span>
            <h2 className="about-section-title">Những Con Người Phía Sau</h2>
            <p className="about-section-desc">
              Đam mê, sáng tạo và tận tâm — đó là DNA chung của mỗi thành viên Mono Wear.
            </p>
          </div>
          <div className="about-team-grid">
            {TEAM.map((t, i) => (
              <div
                className="about-team-card about-animate"
                key={t.name}
                style={{ transitionDelay: `${i * 0.1}s` }}
                onClick={() => setSelectedMember(t)}
              >
                <div className="about-team-img">
                  <img src={t.img} alt={t.name} />
                  <div className="about-team-overlay">
                    <span className="material-symbols-outlined">person</span>
                    <span className="about-team-hint">Xem thông tin</span>
                  </div>
                </div>
                <div className="about-team-info">
                  <h3>{t.name}</h3>
                  <span>{t.role}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Team Member Modal */}
      {selectedMember && (
        <div className="team-modal-overlay" onClick={() => setSelectedMember(null)}>
          <div className="team-modal" onClick={(e) => e.stopPropagation()}>
            <button className="team-modal-close" onClick={() => setSelectedMember(null)}>
              <span className="material-symbols-outlined">close</span>
            </button>
            <div className="team-modal-content">
              <div className="team-modal-left">
                <div className="team-modal-avatar">
                  <img src={selectedMember.img} alt={selectedMember.name} />
                </div>
                <h2>{selectedMember.name}</h2>
                <span className="team-modal-role">{selectedMember.role}</span>
                <blockquote className="team-modal-quote">
                  {selectedMember.quote}
                </blockquote>
                <a href={`mailto:${selectedMember.email}`} className="team-modal-email">
                  <span className="material-symbols-outlined">mail</span>
                  {selectedMember.email}
                </a>
              </div>
              <div className="team-modal-right">
                <div className="team-modal-section">
                  <h3>
                    <span className="material-symbols-outlined">info</span>
                    Giới thiệu
                  </h3>
                  <p>{selectedMember.bio}</p>
                </div>
                <div className="team-modal-section">
                  <h3>
                    <span className="material-symbols-outlined">workspace_premium</span>
                    Thành tựu & Kinh nghiệm
                  </h3>
                  <ul className="team-modal-exp">
                    {selectedMember.experience.map((exp, i) => (
                      <li key={i}>
                        <span className="material-symbols-outlined">check_circle</span>
                        {exp}
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* CTA Section */}
      <section className="about-cta about-animate">
        <div className="container">
          <div className="about-cta-inner">
            <h2>Trở Thành Một Phần<br />Của Mono Wear</h2>
            <p>Khám phá bộ sưu tập mới nhất và tận hưởng phong cách tối giản đẳng cấp.</p>
            <div className="about-cta-buttons">
              <Link to="/shop" className="btn btn-primary btn-lg">MUA SẮM NGAY</Link>
              <Link to="/register" className="btn btn-outline btn-lg" style={{ borderColor: '#fff', color: '#fff' }}>THAM GIA MW CLUB</Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
