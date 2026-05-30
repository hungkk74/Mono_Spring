import { Link } from 'react-router-dom';

export default function Terms() {
  return (
    <div className="policy-page">
      <section className="policy-hero">
        <div className="container">
          <div className="breadcrumb">
            <Link to="/">TRANG CHỦ</Link>
            <span className="breadcrumb-sep">›</span>
            <span className="breadcrumb-current">ĐIỀU KHOẢN DỊCH VỤ</span>
          </div>
          <h1 className="policy-title">Điều Khoản Dịch Vụ</h1>
          <p className="policy-updated">Cập nhật lần cuối: 01/01/2026</p>
        </div>
      </section>
      <section className="policy-content">
        <div className="container">
          <div className="policy-layout">
            <aside className="policy-sidebar">
              <nav className="policy-toc">
                <h4>MỤC LỤC</h4>
                <ul>
                  <li><a href="#gioi-thieu">Giới thiệu</a></li>
                  <li><a href="#tai-khoan">Tài khoản</a></li>
                  <li><a href="#dat-hang">Đặt hàng & Thanh toán</a></li>
                  <li><a href="#giao-hang">Giao hàng</a></li>
                  <li><a href="#so-huu">Sở hữu trí tuệ</a></li>
                  <li><a href="#thay-doi">Thay đổi điều khoản</a></li>
                </ul>
              </nav>
            </aside>
            <div className="policy-body">
              <div className="policy-intro">
                <p>Bằng việc truy cập và sử dụng website monowear.vn, bạn đồng ý tuân thủ các điều khoản và điều kiện sau đây.</p>
              </div>

              <article className="policy-article" id="gioi-thieu">
                <div className="policy-article-header">
                  <span className="policy-article-num">01</span>
                  <h2>Giới Thiệu Chung</h2>
                </div>
                <p>Website monowear.vn thuộc sở hữu và vận hành bởi Công ty TNHH Mono Wear Việt Nam. Các điều khoản này áp dụng cho tất cả người dùng.</p>
                <div className="policy-highlight">
                  <span className="material-symbols-outlined">info</span>
                  <div>
                    <h3>Lưu ý quan trọng</h3>
                    <p>Nếu bạn không đồng ý với bất kỳ điều khoản nào, vui lòng ngừng sử dụng website.</p>
                  </div>
                </div>
              </article>

              <article className="policy-article" id="tai-khoan">
                <div className="policy-article-header">
                  <span className="policy-article-num">02</span>
                  <h2>Tài Khoản Người Dùng</h2>
                </div>
                <div className="policy-card">
                  <h3>Đăng ký tài khoản</h3>
                  <ul>
                    <li>Bạn phải từ <strong>16 tuổi trở lên</strong> để tạo tài khoản.</li>
                    <li>Thông tin đăng ký phải chính xác, đầy đủ và cập nhật.</li>
                    <li>Mỗi người chỉ được sở hữu <strong>một tài khoản</strong>.</li>
                  </ul>
                </div>
                <div className="policy-card">
                  <h3>Trách nhiệm bảo mật</h3>
                  <ul>
                    <li>Bạn chịu trách nhiệm bảo mật mật khẩu và thông tin đăng nhập.</li>
                    <li>Thông báo ngay cho chúng tôi nếu phát hiện truy cập trái phép.</li>
                  </ul>
                </div>
              </article>

              <article className="policy-article" id="dat-hang">
                <div className="policy-article-header">
                  <span className="policy-article-num">03</span>
                  <h2>Đặt Hàng & Thanh Toán</h2>
                </div>
                <div className="policy-grid">
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">receipt_long</span>
                    <h4>Xác nhận đơn hàng</h4>
                    <p>Đơn hàng chỉ được xác nhận khi bạn nhận email xác nhận.</p>
                  </div>
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">price_change</span>
                    <h4>Giá cả</h4>
                    <p>Giá hiển thị đã bao gồm VAT. Có thể thay đổi không báo trước.</p>
                  </div>
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">credit_card</span>
                    <h4>Thanh toán</h4>
                    <p>Chấp nhận COD, chuyển khoản, thẻ tín dụng/ghi nợ và ví điện tử.</p>
                  </div>
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">cancel</span>
                    <h4>Hủy đơn</h4>
                    <p>Có thể hủy trong vòng 2 giờ sau khi đặt.</p>
                  </div>
                </div>
              </article>

              <article className="policy-article" id="giao-hang">
                <div className="policy-article-header">
                  <span className="policy-article-num">04</span>
                  <h2>Giao Hàng</h2>
                </div>
                <div className="policy-table-wrap">
                  <table className="policy-table">
                    <thead><tr><th>Khu vực</th><th>Thời gian</th><th>Phí ship</th></tr></thead>
                    <tbody>
                      <tr><td><strong>Nội thành HCM & HN</strong></td><td>1-2 ngày</td><td>Miễn phí (đơn ≥ 500K)</td></tr>
                      <tr><td><strong>Tỉnh thành khác</strong></td><td>3-5 ngày</td><td>30.000₫</td></tr>
                      <tr><td><strong>Vùng sâu, vùng xa</strong></td><td>5-7 ngày</td><td>50.000₫</td></tr>
                    </tbody>
                  </table>
                </div>
                <p className="policy-note">Miễn phí giao hàng toàn quốc cho đơn từ 1.000.000₫.</p>
              </article>

              <article className="policy-article" id="so-huu">
                <div className="policy-article-header">
                  <span className="policy-article-num">05</span>
                  <h2>Sở Hữu Trí Tuệ</h2>
                </div>
                <p>Toàn bộ nội dung trên website bao gồm logo, thiết kế, hình ảnh, văn bản đều thuộc quyền sở hữu của Mono Wear.</p>
                <ul>
                  <li>Không được sao chép nội dung mà không có sự cho phép bằng văn bản.</li>
                  <li>Vi phạm có thể bị truy cứu trách nhiệm pháp lý.</li>
                </ul>
              </article>

              <article className="policy-article" id="thay-doi">
                <div className="policy-article-header">
                  <span className="policy-article-num">06</span>
                  <h2>Thay Đổi Điều Khoản</h2>
                </div>
                <p>Mono Wear có quyền cập nhật điều khoản bất kỳ lúc nào. Thay đổi có hiệu lực từ ngày đăng tải.</p>
                <div className="policy-contact-box">
                  <div className="policy-contact-item">
                    <span className="material-symbols-outlined">mail</span>
                    <span>legal@monowear.vn</span>
                  </div>
                  <div className="policy-contact-item">
                    <span className="material-symbols-outlined">call</span>
                    <span>1900 8888</span>
                  </div>
                </div>
              </article>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
