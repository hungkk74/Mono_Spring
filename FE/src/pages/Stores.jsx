import { Link } from 'react-router-dom';

const STORES = [
  {
    city: 'TP. Hồ Chí Minh',
    branches: [
      { name: 'Mono Wear Nguyễn Huệ', address: '123 Nguyễn Huệ, Quận 1', phone: '028 3823 1234', hours: '9:00 - 22:00' },
      { name: 'Mono Wear Lê Văn Sỹ', address: '456 Lê Văn Sỹ, Quận 3', phone: '028 3930 5678', hours: '9:00 - 21:30' },
      { name: 'Mono Wear Nguyễn Trãi', address: '789 Nguyễn Trãi, Quận 5', phone: '028 3855 9012', hours: '9:00 - 21:30' },
      { name: 'Mono Wear Landmark 81', address: 'Tầng 2, Landmark 81, Bình Thạnh', phone: '028 3636 3456', hours: '10:00 - 22:00' },
    ],
  },
  {
    city: 'Hà Nội',
    branches: [
      { name: 'Mono Wear Tràng Tiền', address: '12 Tràng Tiền, Hoàn Kiếm', phone: '024 3826 7890', hours: '9:00 - 22:00' },
      { name: 'Mono Wear Cầu Giấy', address: '88 Xuân Thủy, Cầu Giấy', phone: '024 3793 1234', hours: '9:00 - 21:30' },
      { name: 'Mono Wear Times City', address: 'Tầng 1, Times City, Hai Bà Trưng', phone: '024 3200 5678', hours: '10:00 - 22:00' },
    ],
  },
  {
    city: 'Đà Nẵng',
    branches: [
      { name: 'Mono Wear Bạch Đằng', address: '36 Bạch Đằng, Hải Châu', phone: '0236 382 9012', hours: '9:00 - 21:30' },
      { name: 'Mono Wear Vincom Đà Nẵng', address: 'Tầng 2, Vincom, Ngô Quyền', phone: '0236 365 3456', hours: '10:00 - 22:00' },
    ],
  },
  {
    city: 'Các tỉnh thành khác',
    branches: [
      { name: 'Mono Wear Cần Thơ', address: '15 Hòa Bình, Ninh Kiều, Cần Thơ', phone: '0292 381 7890', hours: '9:00 - 21:00' },
      { name: 'Mono Wear Nha Trang', address: '22 Trần Phú, Nha Trang, Khánh Hòa', phone: '0258 352 1234', hours: '9:00 - 21:00' },
      { name: 'Mono Wear Huế', address: '8 Trần Hưng Đạo, TP. Huế', phone: '0234 382 5678', hours: '9:00 - 21:00' },
      { name: 'Mono Wear Hải Phòng', address: '45 Lạch Tray, Ngô Quyền, Hải Phòng', phone: '0225 382 9012', hours: '9:00 - 21:00' },
    ],
  },
];

export default function Stores() {
  return (
    <div className="policy-page">
      <section className="policy-hero">
        <div className="container">
          <div className="breadcrumb">
            <Link to="/">TRANG CHỦ</Link>
            <span className="breadcrumb-sep">›</span>
            <span className="breadcrumb-current">HỆ THỐNG CỬA HÀNG</span>
          </div>
          <h1 className="policy-title">Hệ Thống Cửa Hàng</h1>
          <p className="policy-updated">15+ cửa hàng trên toàn quốc</p>
        </div>
      </section>

      <section className="policy-content">
        <div className="container">
          <div className="stores-intro">
            <p>Ghé thăm cửa hàng Mono Wear gần nhất để trải nghiệm trực tiếp chất liệu và phong cách tối giản đẳng cấp.</p>
          </div>

          <div className="stores-list">
            {STORES.map((region) => (
              <div className="stores-region" key={region.city}>
                <h2 className="stores-region-title">
                  <span className="material-symbols-outlined">location_on</span>
                  {region.city}
                  <span className="stores-region-count">{region.branches.length} cửa hàng</span>
                </h2>
                <div className="stores-grid">
                  {region.branches.map((b) => (
                    <div className="store-card" key={b.name}>
                      <h3>{b.name}</h3>
                      <div className="store-info">
                        <div className="store-info-row">
                          <span className="material-symbols-outlined">pin_drop</span>
                          <span>{b.address}</span>
                        </div>
                        <div className="store-info-row">
                          <span className="material-symbols-outlined">call</span>
                          <span>{b.phone}</span>
                        </div>
                        <div className="store-info-row">
                          <span className="material-symbols-outlined">schedule</span>
                          <span>{b.hours}</span>
                        </div>
                      </div>
                      <button className="btn btn-outline btn-sm store-direction-btn">
                        <span className="material-symbols-outlined" style={{ fontSize: '16px', marginRight: '6px' }}>directions</span>
                        CHỈ ĐƯỜNG
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
