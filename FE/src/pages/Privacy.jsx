import { Link } from 'react-router-dom';

export default function Privacy() {
  return (
    <div className="policy-page">
      <section className="policy-hero">
        <div className="container">
          <div className="breadcrumb">
            <Link to="/">TRANG CHỦ</Link>
            <span className="breadcrumb-sep">›</span>
            <span className="breadcrumb-current">CHÍNH SÁCH BẢO MẬT</span>
          </div>
          <h1 className="policy-title">Chính Sách Bảo Mật</h1>
          <p className="policy-updated">Cập nhật lần cuối: 01/01/2026</p>
        </div>
      </section>

      <section className="policy-content">
        <div className="container">
          <div className="policy-layout">
            {/* Sidebar TOC */}
            <aside className="policy-sidebar">
              <nav className="policy-toc">
                <h4>MỤC LỤC</h4>
                <ul>
                  <li><a href="#thu-thap">Thu thập thông tin</a></li>
                  <li><a href="#su-dung">Sử dụng thông tin</a></li>
                  <li><a href="#chia-se">Chia sẻ thông tin</a></li>
                  <li><a href="#bao-mat">Bảo mật dữ liệu</a></li>
                  <li><a href="#cookies">Cookies</a></li>
                  <li><a href="#quyen-loi">Quyền của bạn</a></li>
                  <li><a href="#lien-he">Liên hệ</a></li>
                </ul>
              </nav>
            </aside>

            {/* Content */}
            <div className="policy-body">
              <div className="policy-intro">
                <p>
                  Tại <strong>Mono Wear</strong>, chúng tôi tôn trọng và cam kết bảo vệ quyền riêng tư của bạn. 
                  Chính sách bảo mật này giải thích cách chúng tôi thu thập, sử dụng, lưu trữ và bảo vệ thông tin 
                  cá nhân khi bạn sử dụng website và các dịch vụ của chúng tôi.
                </p>
              </div>

              <article className="policy-article" id="thu-thap">
                <div className="policy-article-header">
                  <span className="policy-article-num">01</span>
                  <h2>Thu Thập Thông Tin</h2>
                </div>
                <p>Chúng tôi thu thập các loại thông tin sau khi bạn sử dụng dịch vụ:</p>
                <div className="policy-card">
                  <h3>Thông tin cá nhân bạn cung cấp</h3>
                  <ul>
                    <li>Họ và tên, địa chỉ email, số điện thoại</li>
                    <li>Địa chỉ giao hàng và địa chỉ thanh toán</li>
                    <li>Thông tin tài khoản (tên đăng nhập, mật khẩu đã mã hóa)</li>
                    <li>Lịch sử đơn hàng và sở thích mua sắm</li>
                  </ul>
                </div>
                <div className="policy-card">
                  <h3>Thông tin tự động thu thập</h3>
                  <ul>
                    <li>Địa chỉ IP, loại trình duyệt và thiết bị</li>
                    <li>Thời gian truy cập và trang đã xem</li>
                    <li>Dữ liệu từ cookies và các công nghệ tương tự</li>
                  </ul>
                </div>
              </article>

              <article className="policy-article" id="su-dung">
                <div className="policy-article-header">
                  <span className="policy-article-num">02</span>
                  <h2>Sử Dụng Thông Tin</h2>
                </div>
                <p>Chúng tôi sử dụng thông tin của bạn cho các mục đích:</p>
                <div className="policy-grid">
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">shopping_bag</span>
                    <h4>Xử lý đơn hàng</h4>
                    <p>Hoàn tất giao dịch, giao hàng và quản lý đơn hàng của bạn.</p>
                  </div>
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">support_agent</span>
                    <h4>Hỗ trợ khách hàng</h4>
                    <p>Phản hồi yêu cầu, thắc mắc và xử lý khiếu nại.</p>
                  </div>
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">campaign</span>
                    <h4>Marketing</h4>
                    <p>Gửi thông tin khuyến mãi, sản phẩm mới (có thể từ chối nhận).</p>
                  </div>
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">security</span>
                    <h4>Bảo mật</h4>
                    <p>Phát hiện và ngăn chặn các hoạt động gian lận, lừa đảo.</p>
                  </div>
                </div>
              </article>

              <article className="policy-article" id="chia-se">
                <div className="policy-article-header">
                  <span className="policy-article-num">03</span>
                  <h2>Chia Sẻ Thông Tin</h2>
                </div>
                <p>
                  Chúng tôi <strong>không bán</strong> thông tin cá nhân của bạn cho bên thứ ba. 
                  Thông tin chỉ được chia sẻ trong các trường hợp:
                </p>
                <ul>
                  <li><strong>Đối tác vận chuyển:</strong> Để giao hàng đến bạn (tên, địa chỉ, số điện thoại).</li>
                  <li><strong>Cổng thanh toán:</strong> Xử lý giao dịch thanh toán (thông tin được mã hóa đầu-cuối).</li>
                  <li><strong>Yêu cầu pháp lý:</strong> Khi có yêu cầu từ cơ quan nhà nước có thẩm quyền.</li>
                </ul>
              </article>

              <article className="policy-article" id="bao-mat">
                <div className="policy-article-header">
                  <span className="policy-article-num">04</span>
                  <h2>Bảo Mật Dữ Liệu</h2>
                </div>
                <div className="policy-highlight">
                  <span className="material-symbols-outlined">verified_user</span>
                  <div>
                    <h3>Cam kết bảo mật</h3>
                    <p>
                      Chúng tôi áp dụng các biện pháp kỹ thuật và tổ chức phù hợp để bảo vệ thông tin của bạn, 
                      bao gồm mã hóa SSL/TLS, tường lửa, kiểm soát truy cập nghiêm ngặt và kiểm tra bảo mật định kỳ.
                    </p>
                  </div>
                </div>
              </article>

              <article className="policy-article" id="cookies">
                <div className="policy-article-header">
                  <span className="policy-article-num">05</span>
                  <h2>Cookies</h2>
                </div>
                <p>Chúng tôi sử dụng cookies để:</p>
                <div className="policy-table-wrap">
                  <table className="policy-table">
                    <thead>
                      <tr>
                        <th>Loại Cookie</th>
                        <th>Mục đích</th>
                        <th>Thời hạn</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td><strong>Thiết yếu</strong></td>
                        <td>Duy trì phiên đăng nhập, giỏ hàng</td>
                        <td>Phiên làm việc</td>
                      </tr>
                      <tr>
                        <td><strong>Phân tích</strong></td>
                        <td>Hiểu hành vi người dùng, cải thiện dịch vụ</td>
                        <td>12 tháng</td>
                      </tr>
                      <tr>
                        <td><strong>Tùy chọn</strong></td>
                        <td>Lưu cài đặt ngôn ngữ, giao diện</td>
                        <td>6 tháng</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <p className="policy-note">
                  Bạn có thể tắt cookies trong cài đặt trình duyệt, tuy nhiên một số tính năng có thể bị hạn chế.
                </p>
              </article>

              <article className="policy-article" id="quyen-loi">
                <div className="policy-article-header">
                  <span className="policy-article-num">06</span>
                  <h2>Quyền Của Bạn</h2>
                </div>
                <p>Bạn có quyền:</p>
                <div className="policy-rights-grid">
                  {[
                    { icon: 'visibility', title: 'Truy cập', desc: 'Xem thông tin cá nhân chúng tôi đang lưu trữ.' },
                    { icon: 'edit', title: 'Chỉnh sửa', desc: 'Cập nhật, sửa đổi thông tin cá nhân.' },
                    { icon: 'delete', title: 'Xóa', desc: 'Yêu cầu xóa tài khoản và dữ liệu cá nhân.' },
                    { icon: 'block', title: 'Từ chối', desc: 'Từ chối nhận email marketing bất kỳ lúc nào.' },
                  ].map((r) => (
                    <div className="policy-right-item" key={r.title}>
                      <span className="material-symbols-outlined">{r.icon}</span>
                      <div>
                        <h4>{r.title}</h4>
                        <p>{r.desc}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </article>

              <article className="policy-article" id="lien-he">
                <div className="policy-article-header">
                  <span className="policy-article-num">07</span>
                  <h2>Liên Hệ</h2>
                </div>
                <p>
                  Nếu bạn có bất kỳ câu hỏi nào về chính sách bảo mật, vui lòng liên hệ:
                </p>
                <div className="policy-contact-box">
                  <div className="policy-contact-item">
                    <span className="material-symbols-outlined">mail</span>
                    <span>privacy@monowear.vn</span>
                  </div>
                  <div className="policy-contact-item">
                    <span className="material-symbols-outlined">call</span>
                    <span>1900 8888</span>
                  </div>
                  <div className="policy-contact-item">
                    <span className="material-symbols-outlined">location_on</span>
                    <span>123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh</span>
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
