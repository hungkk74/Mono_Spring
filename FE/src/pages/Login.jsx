import { useState } from 'react';
import { Link, useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { signInWithGooglePopup } from '../firebase';

export default function Login() {
  const { login, loginWithGoogle, isLoggedIn } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPwd, setShowPwd] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  if (isLoggedIn) {
    return <Navigate to="/account" replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password) { setError('Vui lòng nhập đầy đủ email và mật khẩu.'); return; }
    setLoading(true);
    setError('');
    try {
      const res = await login(email, password);
      if (res.ok) {
        setSuccess('Đăng nhập thành công! Đang chuyển hướng...');
        setTimeout(() => navigate('/'), 1000);
      } else {
        setError(res.message || 'Đăng nhập thất bại.');
      }
    } catch {
      setError('Không thể kết nối đến máy chủ.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    setLoading(true);
    setError('');
    setSuccess('');
    try {
      const result = await signInWithGooglePopup();
      const idToken = await result.user.getIdToken();
      const res = await loginWithGoogle(idToken);
      if (res.ok) {
        setSuccess('Đăng nhập thành công! Đang chuyển hướng...');
        setTimeout(() => navigate('/'), 1000);
      } else {
        setError(res.message || 'Đăng nhập bằng Google thất bại.');
      }
    } catch (err) {
      console.error(err);
      if (err.code !== 'auth/popup-closed-by-user') {
        setError('Đăng nhập bằng Google thất bại hoặc bị hủy.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-tabs">
          <button className="auth-tab active">ĐĂNG NHẬP</button>
          <Link to="/register" className="auth-tab">ĐĂNG KÝ</Link>
        </div>

        <h1 className="auth-heading">CHÀO MỪNG TRỞ LẠI</h1>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="login-email">Email</label>
            <input id="login-email" type="email" placeholder="EXAMPLE@DOMAIN.COM" value={email}
              onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="form-group">
            <label htmlFor="login-password">Mật khẩu</label>
            <div className="input-password-wrap">
              <input id="login-password" type={showPwd ? 'text' : 'password'} placeholder="••••••••"
                value={password} onChange={(e) => setPassword(e.target.value)} required />
              <button type="button" className="pwd-toggle" onClick={() => setShowPwd(!showPwd)}>
                <span className="material-symbols-outlined">{showPwd ? 'visibility' : 'visibility_off'}</span>
              </button>
            </div>
          </div>

          <div className="auth-options">
            <label className="checkbox-label">
              <input type="checkbox" /> Ghi nhớ đăng nhập
            </label>
            <Link to="/forgot-password" className="forgot-link">Quên mật khẩu?</Link>
          </div>

          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? 'ĐANG XỬ LÝ...' : 'ĐĂNG NHẬP'}
          </button>
        </form>

        <div className="auth-divider">
          <span>Hoặc</span>
        </div>

        <button
          type="button"
          className="btn btn-outline btn-full btn-social"
          onClick={handleGoogleLogin}
          disabled={loading}
        >
          <span className="material-symbols-outlined">public</span>
          ĐĂNG NHẬP VỚI GOOGLE
        </button>

        <div className="auth-footer-link">
          <Link to="/register">Chưa có tài khoản? Đăng ký</Link>
        </div>
      </div>
    </div>
  );
}
