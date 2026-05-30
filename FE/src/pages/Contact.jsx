import { Link } from 'react-router-dom';
import { useState } from 'react';

export default function Contact() {
  const [form, setForm] = useState({ name: '', email: '', phone: '', subject: '', message: '' });
  const [sent, setSent] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setSent(true);
    setTimeout(() => setSent(false), 5000);
    setForm({ name: '', email: '', phone: '', subject: '', message: '' });
  };

  return (
    <div className="policy-page">
      <section className="policy-hero">
        <div className="container">
          <div className="breadcrumb">
            <Link to="/">TRANG CHỦ</Link>
            <span className="breadcrumb-sep">›</span>
            <span className="breadcrumb-current">LIÊN HỆ</span>
          </div>
          <h1 className="policy-title">Liên Hệ Với Chúng Tôi</h1>
          <p className="policy-updated">Chúng tôi luôn sẵn sàng lắng nghe bạn</p>
        </div>
      </section>

      <section className="policy-content">
        <div className="container">
          <div className="contact-layout">
            {/* Contact Info */}
            <div className="contact-info-section">
              <h2>Thông Tin Liên Hệ</h2>
              <p className="contact-info-desc">Hãy liên hệ với chúng tôi qua bất kỳ kênh nào bên dưới, hoặc điền form liên hệ để được phản hồi nhanh nhất.</p>

              <div className="contact-info-cards">
                <div className="contact-info-card">
                  <div className="contact-info-icon">
                    <span className="material-symbols-outlined">call</span>
                  </div>
                  <h3>Hotline</h3>
                  <p className="contact-info-value">1900 8888</p>
                  <p className="contact-info-note">Thứ 2 - CN: 8:00 - 22:00</p>
                </div>

                <div className="contact-info-card">
                  <div className="contact-info-icon">
                    <span className="material-symbols-outlined">mail</span>
                  </div>
                  <h3>Email</h3>
                  <p className="contact-info-value">support@monowear.vn</p>
                  <p className="contact-info-note">Phản hồi trong 24 giờ</p>
                </div>

                <div className="contact-info-card">
                  <div className="contact-info-icon">
                    <span className="material-symbols-outlined">location_on</span>
                  </div>
                  <h3>Trụ sở chính</h3>
                  <p className="contact-info-value">123 Nguyễn Huệ, Q.1, TP.HCM</p>
                  <p className="contact-info-note">Thứ 2 - Thứ 6: 9:00 - 18:00</p>
                </div>

                <div className="contact-info-card">
                  <div className="contact-info-icon">
                    <span className="material-symbols-outlined">chat</span>
                  </div>
                  <h3>Live Chat</h3>
                  <p className="contact-info-value">Chat trực tuyến trên website</p>
                  <p className="contact-info-note">Hỗ trợ 24/7</p>
                </div>
              </div>
            </div>

            {/* Contact Form */}
            <div className="contact-form-section">
              <h2>Gửi Tin Nhắn</h2>
              {sent && (
                <div className="alert alert-success" style={{ marginBottom: '24px' }}>
                  <span className="material-symbols-outlined" style={{ fontSize: '18px', marginRight: '8px' }}>check_circle</span>
                  Gửi thành công! Chúng tôi sẽ phản hồi trong thời gian sớm nhất.
                </div>
              )}
              <form className="contact-form" onSubmit={handleSubmit}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Họ và tên *</label>
                    <input type="text" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required placeholder="Nguyễn Văn A" />
                  </div>
                  <div className="form-group">
                    <label>Email *</label>
                    <input type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required placeholder="email@example.com" />
                  </div>
                  <div className="form-group">
                    <label>Số điện thoại</label>
                    <input type="tel" value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} placeholder="0901 234 567" />
                  </div>
                  <div className="form-group">
                    <label>Chủ đề *</label>
                    <select value={form.subject} onChange={e => setForm({ ...form, subject: e.target.value })} required>
                      <option value="">-- Chọn chủ đề --</option>
                      <option value="order">Đơn hàng</option>
                      <option value="product">Sản phẩm</option>
                      <option value="return">Đổi trả / Hoàn tiền</option>
                      <option value="feedback">Góp ý / Phản hồi</option>
                      <option value="partnership">Hợp tác</option>
                      <option value="other">Khác</option>
                    </select>
                  </div>
                  <div className="form-group full">
                    <label>Nội dung *</label>
                    <textarea rows="5" value={form.message} onChange={e => setForm({ ...form, message: e.target.value })} required placeholder="Mô tả chi tiết vấn đề hoặc yêu cầu của bạn..." />
                  </div>
                </div>
                <button type="submit" className="btn btn-primary btn-lg btn-full">GỬI LIÊN HỆ</button>
              </form>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
