import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const { register, isLoggedIn, user } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '', fullName: '', phoneNumber: '' });
  const [showPwd, setShowPwd] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (isLoggedIn) {
    return (
      <div className="auth-page">
        <div className="auth-card" style={{ textAlign: 'center' }}>
          <span className="material-symbols-outlined" style={{ fontSize: 56, color: 'var(--primary)', marginBottom: 16 }}>check_circle</span>
          <h1 className="auth-heading">BẠN ĐÃ LÀ THÀNH VIÊN</h1>
          <p style={{ color: 'var(--text-muted)', marginBottom: 24 }}>
            Chào <strong>{user?.fullName || 'bạn'}</strong>, bạn đã là thành viên MW Club rồi!
          </p>
          <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
            <Link to="/account" className="btn btn-primary">TÀI KHOẢN</Link>
            <Link to="/shop" className="btn btn-outline">MUA SẮM</Link>
          </div>
        </div>
      </div>
    );
  }

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.email || !form.password || !form.fullName) {
      setError('Vui lòng nhập đầy đủ thông tin bắt buộc.');
      return;
    }
    if (form.password.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const res = await register(form);
      if (res.ok) {
        navigate('/');
      } else {
        setError(res.message || 'Đăng ký thất bại.');
      }
    } catch {
      setError('Không thể kết nối đến máy chủ.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-tabs">
          <Link to="/login" className="auth-tab">ĐĂNG NHẬP</Link>
          <button className="auth-tab active">ĐĂNG KÝ</button>
        </div>

        <h1 className="auth-heading">TẠO TÀI KHOẢN</h1>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>Họ và tên *</label>
            <input name="fullName" placeholder="NGUYỄN VĂN A" value={form.fullName} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Email *</label>
            <input name="email" type="email" placeholder="EXAMPLE@DOMAIN.COM" value={form.email} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Mật khẩu *</label>
            <div className="input-password-wrap">
              <input name="password" type={showPwd ? 'text' : 'password'} placeholder="••••••••"
                value={form.password} onChange={handleChange} required minLength={6} />
              <button type="button" className="pwd-toggle" onClick={() => setShowPwd(!showPwd)}>
                <span className="material-symbols-outlined">{showPwd ? 'visibility' : 'visibility_off'}</span>
              </button>
            </div>
          </div>
          <div className="form-group">
            <label>Số điện thoại</label>
            <input name="phoneNumber" type="tel" placeholder="0901 234 567" value={form.phoneNumber} onChange={handleChange} />
          </div>

          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? 'ĐANG XỬ LÝ...' : 'ĐĂNG KÝ'}
          </button>
        </form>

        <div className="auth-footer-link">
          <Link to="/login">Đã có tài khoản? Đăng nhập</Link>
        </div>
      </div>
    </div>
  );
}
