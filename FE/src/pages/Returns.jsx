import { Link } from 'react-router-dom';

export default function Returns() {
  return (
    <div className="policy-page">
      <section className="policy-hero">
        <div className="container">
          <div className="breadcrumb">
            <Link to="/">TRANG CHỦ</Link>
            <span className="breadcrumb-sep">›</span>
            <span className="breadcrumb-current">CHÍNH SÁCH ĐỔI TRẢ</span>
          </div>
          <h1 className="policy-title">Chính Sách Đổi Trả</h1>
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
                  <li><a href="#dieu-kien">Điều kiện đổi trả</a></li>
                  <li><a href="#thoi-gian">Thời gian</a></li>
                  <li><a href="#quy-trinh">Quy trình</a></li>
                  <li><a href="#hoan-tien">Hoàn tiền</a></li>
                  <li><a href="#khong-ap-dung">Không áp dụng</a></li>
                </ul>
              </nav>
            </aside>
            <div className="policy-body">
              <div className="policy-intro">
                <p>Mono Wear cam kết mang đến trải nghiệm mua sắm tốt nhất. Nếu bạn không hài lòng với sản phẩm, chúng tôi sẵn sàng hỗ trợ đổi trả.</p>
              </div>

              <article className="policy-article" id="dieu-kien">
                <div className="policy-article-header">
                  <span className="policy-article-num">01</span>
                  <h2>Điều Kiện Đổi Trả</h2>
                </div>
                <div className="policy-grid">
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">check_circle</span>
                    <h4>Sản phẩm lỗi</h4>
                    <p>Lỗi sản xuất, đường may, chất liệu không đạt tiêu chuẩn.</p>
                  </div>
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">check_circle</span>
                    <h4>Sai sản phẩm</h4>
                    <p>Giao sai mẫu, sai size, sai màu so với đơn đặt hàng.</p>
                  </div>
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">check_circle</span>
                    <h4>Đổi size</h4>
                    <p>Sản phẩm chưa qua sử dụng, còn nguyên tem mác, bao bì.</p>
                  </div>
                  <div className="policy-feature">
                    <span className="material-symbols-outlined">check_circle</span>
                    <h4>Không vừa ý</h4>
                    <p>Đổi trả trong 7 ngày nếu sản phẩm còn nguyên trạng.</p>
                  </div>
                </div>
              </article>

              <article className="policy-article" id="thoi-gian">
                <div className="policy-article-header">
                  <span className="policy-article-num">02</span>
                  <h2>Thời Gian Đổi Trả</h2>
                </div>
                <div className="policy-table-wrap">
                  <table className="policy-table">
                    <thead><tr><th>Trường hợp</th><th>Thời hạn</th><th>Chi phí</th></tr></thead>
                    <tbody>
                      <tr><td><strong>Sản phẩm lỗi / Sai sản phẩm</strong></td><td>30 ngày</td><td>Miễn phí hoàn toàn</td></tr>
                      <tr><td><strong>Đổi size</strong></td><td>15 ngày</td><td>Miễn phí (lần đầu)</td></tr>
                      <tr><td><strong>Đổi ý / Không vừa ý</strong></td><td>7 ngày</td><td>Khách chịu phí ship</td></tr>
                    </tbody>
                  </table>
                </div>
                <p className="policy-note">Thời gian tính từ ngày bạn nhận được hàng (theo xác nhận của đơn vị vận chuyển).</p>
              </article>

              <article className="policy-article" id="quy-trinh">
                <div className="policy-article-header">
                  <span className="policy-article-num">03</span>
                  <h2>Quy Trình Đổi Trả</h2>
                </div>
                <div className="policy-steps">
                  {[
                    { step: '1', title: 'Liên hệ', desc: 'Gọi hotline 1900 8888 hoặc email support@monowear.vn kèm mã đơn hàng và hình ảnh sản phẩm.' },
                    { step: '2', title: 'Xác nhận', desc: 'Nhân viên CSKH xác nhận yêu cầu và hướng dẫn gửi trả sản phẩm trong vòng 24 giờ.' },
                    { step: '3', title: 'Gửi hàng', desc: 'Đóng gói sản phẩm kèm phiếu đổi trả và gửi về địa chỉ kho Mono Wear.' },
                    { step: '4', title: 'Hoàn tất', desc: 'Kiểm tra sản phẩm, gửi hàng thay thế hoặc hoàn tiền trong 3-5 ngày làm việc.' },
                  ].map((s) => (
                    <div className="policy-step" key={s.step}>
                      <div className="policy-step-num">{s.step}</div>
                      <div>
                        <h4>{s.title}</h4>
                        <p>{s.desc}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </article>

              <article className="policy-article" id="hoan-tien">
                <div className="policy-article-header">
                  <span className="policy-article-num">04</span>
                  <h2>Hoàn Tiền</h2>
                </div>
                <div className="policy-highlight">
                  <span className="material-symbols-outlined">account_balance_wallet</span>
                  <div>
                    <h3>Hình thức hoàn tiền</h3>
                    <p>Hoàn tiền qua hình thức thanh toán ban đầu hoặc chuyển khoản ngân hàng trong 3-5 ngày làm việc kể từ khi nhận được hàng trả.</p>
                  </div>
                </div>
              </article>

              <article className="policy-article" id="khong-ap-dung">
                <div className="policy-article-header">
                  <span className="policy-article-num">05</span>
                  <h2>Trường Hợp Không Áp Dụng</h2>
                </div>
                <div className="policy-card">
                  <h3>Không đổi trả khi</h3>
                  <ul>
                    <li>Sản phẩm đã qua sử dụng, giặt, là hoặc bị hư hỏng do người mua.</li>
                    <li>Không còn nguyên tem, mác, bao bì gốc.</li>
                    <li>Sản phẩm thuộc chương trình khuyến mãi / giảm giá đặc biệt (ghi rõ "Không đổi trả").</li>
                    <li>Phụ kiện (tất, khẩu trang, đồ lót) vì lý do vệ sinh.</li>
                    <li>Quá thời hạn đổi trả quy định.</li>
                  </ul>
                </div>
                <div className="policy-contact-box">
                  <div className="policy-contact-item">
                    <span className="material-symbols-outlined">mail</span>
                    <span>support@monowear.vn</span>
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
