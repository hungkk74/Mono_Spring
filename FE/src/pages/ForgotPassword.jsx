import { useState, useRef, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api';

// ===================== STEP 1: Nhập Email =====================
function StepEmail({ onNext }) {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email.trim()) { setError('Vui lòng nhập email.'); return; }
    setLoading(true);
    setError('');
    const res = await authApi.forgotPassword(email.trim().toLowerCase());
    setLoading(false);
    // Luôn chuyển sang step 2 (tránh email enumeration attack)
    if (res.ok || res.message) {
      onNext(email.trim().toLowerCase());
    } else {
      setError(res.message || 'Có lỗi xảy ra. Vui lòng thử lại.');
    }
  };

  return (
    <>
      <div className="forgot-step-icon">
        <span className="material-symbols-outlined" style={{ fontSize: '40px', color: '#111' }}>lock_reset</span>
      </div>
      <h1 className="auth-heading" style={{ marginBottom: '8px' }}>QUÊN MẬT KHẨU</h1>
      <p className="forgot-subtitle">Nhập email tài khoản của bạn. Chúng tôi sẽ gửi mã xác thực.</p>

      {error && <div className="alert alert-error">{error}</div>}

      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-group">
          <label htmlFor="forgot-email">Email</label>
          <input
            id="forgot-email"
            type="email"
            placeholder="EXAMPLE@DOMAIN.COM"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoFocus
          />
        </div>
        <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
          {loading ? 'ĐANG GỬI...' : 'GỬI MÃ XÁC THỰC'}
        </button>
      </form>

      <div className="auth-footer-link" style={{ marginTop: '20px' }}>
        <Link to="/login">← Quay lại đăng nhập</Link>
      </div>
    </>
  );
}

// ===================== STEP 2: Nhập OTP =====================
const OTP_LENGTH = 6;
const OTP_TTL = 5 * 60; // 300 giây

function StepOtp({ email, onNext, onResend }) {
  const [otp, setOtp] = useState(Array(OTP_LENGTH).fill(''));
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [timeLeft, setTimeLeft] = useState(OTP_TTL);
  const [resending, setResending] = useState(false);
  const inputRefs = useRef([]);

  // Countdown timer
  useEffect(() => {
    if (timeLeft <= 0) return;
    const timer = setInterval(() => setTimeLeft((t) => t - 1), 1000);
    return () => clearInterval(timer);
  }, [timeLeft]);

  const formatTime = (s) => `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;

  const handleChange = (index, val) => {
    // Chỉ nhận chữ số
    const digit = val.replace(/\D/g, '').slice(-1);
    const next = [...otp];
    next[index] = digit;
    setOtp(next);
    // Auto-focus sang ô tiếp theo
    if (digit && index < OTP_LENGTH - 1) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
    if (e.key === 'Enter') handleSubmit();
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, OTP_LENGTH);
    if (!pasted) return;
    const next = [...otp];
    pasted.split('').forEach((ch, i) => { next[i] = ch; });
    setOtp(next);
    // Focus ô cuối đã paste
    const focusIdx = Math.min(pasted.length, OTP_LENGTH - 1);
    inputRefs.current[focusIdx]?.focus();
  };

  const handleSubmit = useCallback(async () => {
    const code = otp.join('');
    if (code.length < OTP_LENGTH) { setError('Vui lòng nhập đủ 6 chữ số.'); return; }
    setLoading(true);
    setError('');
    const res = await authApi.verifyOtp(email, code);
    setLoading(false);
    if (res.ok && res.data?.resetToken) {
      onNext(res.data.resetToken);
    } else {
      setError(res.message || 'Mã OTP không hợp lệ hoặc đã hết hạn.');
      setOtp(Array(OTP_LENGTH).fill(''));
      inputRefs.current[0]?.focus();
    }
  }, [otp, email, onNext]);

  const handleResend = async () => {
    setResending(true);
    setError('');
    await authApi.forgotPassword(email);
    setResending(false);
    setTimeLeft(OTP_TTL);
    setOtp(Array(OTP_LENGTH).fill(''));
    inputRefs.current[0]?.focus();
  };

  return (
    <>
      <div className="forgot-step-icon">
        <span className="material-symbols-outlined" style={{ fontSize: '40px', color: '#111' }}>mark_email_read</span>
      </div>
      <h1 className="auth-heading" style={{ marginBottom: '8px' }}>NHẬP MÃ OTP</h1>
      <p className="forgot-subtitle">
        Mã xác thực đã được gửi đến<br />
        <strong>{email}</strong>
      </p>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="otp-inputs" onPaste={handlePaste}>
        {otp.map((digit, i) => (
          <input
            key={i}
            ref={(el) => (inputRefs.current[i] = el)}
            id={`otp-${i}`}
            type="text"
            inputMode="numeric"
            maxLength={1}
            value={digit}
            className={`otp-cell ${digit ? 'otp-cell-filled' : ''}`}
            onChange={(e) => handleChange(i, e.target.value)}
            onKeyDown={(e) => handleKeyDown(i, e)}
            autoFocus={i === 0}
          />
        ))}
      </div>

      <div className="otp-timer">
        {timeLeft > 0 ? (
          <span>Mã hết hạn sau <strong>{formatTime(timeLeft)}</strong></span>
        ) : (
          <span style={{ color: '#e53e3e' }}>Mã đã hết hạn</span>
        )}
      </div>

      <button
        className="btn btn-primary btn-full"
        onClick={handleSubmit}
        disabled={loading || otp.join('').length < OTP_LENGTH}
        style={{ marginTop: '16px' }}
      >
        {loading ? 'ĐANG XÁC THỰC...' : 'XÁC THỰC'}
      </button>

      <button
        className="btn btn-outline btn-full"
        onClick={handleResend}
        disabled={resending || timeLeft > 0}
        style={{ marginTop: '10px' }}
      >
        {resending ? 'ĐANG GỬI LẠI...' : 'GỬI LẠI MÃ'}
      </button>

      <div className="auth-footer-link" style={{ marginTop: '16px' }}>
        <Link to="/login">← Quay lại đăng nhập</Link>
      </div>
    </>
  );
}

// ===================== STEP 3: Mật khẩu mới =====================
function StepNewPassword({ resetToken, onSuccess }) {
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password.length < 6) { setError('Mật khẩu phải có ít nhất 6 ký tự.'); return; }
    if (password !== confirm) { setError('Mật khẩu xác nhận không khớp.'); return; }
    setLoading(true);
    setError('');
    const res = await authApi.resetPassword(resetToken, password);
    setLoading(false);
    if (res.ok) {
      onSuccess();
    } else {
      setError(res.message || 'Có lỗi xảy ra. Vui lòng thử lại từ đầu.');
    }
  };

  return (
    <>
      <div className="forgot-step-icon">
        <span className="material-symbols-outlined" style={{ fontSize: '40px', color: '#111' }}>key</span>
      </div>
      <h1 className="auth-heading" style={{ marginBottom: '8px' }}>MẬT KHẨU MỚI</h1>
      <p className="forgot-subtitle">Tạo mật khẩu mới cho tài khoản của bạn.</p>

      {error && <div className="alert alert-error">{error}</div>}

      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-group">
          <label htmlFor="new-password">Mật khẩu mới</label>
          <div className="input-password-wrap">
            <input
              id="new-password"
              type={showPwd ? 'text' : 'password'}
              placeholder="Ít nhất 6 ký tự"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoFocus
            />
            <button type="button" className="pwd-toggle" onClick={() => setShowPwd(!showPwd)}>
              <span className="material-symbols-outlined">{showPwd ? 'visibility' : 'visibility_off'}</span>
            </button>
          </div>
        </div>
        <div className="form-group">
          <label htmlFor="confirm-password">Xác nhận mật khẩu</label>
          <div className="input-password-wrap">
            <input
              id="confirm-password"
              type={showPwd ? 'text' : 'password'}
              placeholder="Nhập lại mật khẩu"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              required
            />
          </div>
        </div>
        <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
          {loading ? 'ĐANG CẬP NHẬT...' : 'ĐẶT LẠI MẬT KHẨU'}
        </button>
      </form>
    </>
  );
}

// ===================== STEP SUCCESS =====================
function StepSuccess() {
  const navigate = useNavigate();

  useEffect(() => {
    const timer = setTimeout(() => navigate('/login'), 3000);
    return () => clearTimeout(timer);
  }, [navigate]);

  return (
    <div style={{ textAlign: 'center' }}>
      <div className="forgot-step-icon forgot-step-success">
        <span className="material-symbols-outlined" style={{ fontSize: '48px', color: '#38a169' }}>check_circle</span>
      </div>
      <h1 className="auth-heading" style={{ marginBottom: '12px' }}>THÀNH CÔNG!</h1>
      <p className="forgot-subtitle" style={{ marginBottom: '24px' }}>
        Mật khẩu đã được cập nhật.<br />Đang chuyển đến trang đăng nhập...
      </p>
      <div className="forgot-progress-bar">
        <div className="forgot-progress-fill" />
      </div>
      <div style={{ marginTop: '20px' }}>
        <Link to="/login" className="btn btn-primary">ĐĂNG NHẬP NGAY</Link>
      </div>
    </div>
  );
}

// ===================== MAIN COMPONENT =====================
export default function ForgotPassword() {
  const [step, setStep] = useState(1); // 1 | 2 | 3 | 4(success)
  const [email, setEmail] = useState('');
  const [resetToken, setResetToken] = useState('');

  const STEPS = [
    { label: 'Email', icon: 'mail' },
    { label: 'OTP', icon: 'pin' },
    { label: 'Mật khẩu', icon: 'key' },
  ];

  return (
    <div className="auth-page">
      <div className="auth-card forgot-card">
        {/* Step indicator — chỉ hiện khi chưa success */}
        {step <= 3 && (
          <div className="forgot-steps">
            {STEPS.map((s, i) => {
              const idx = i + 1;
              const status = idx < step ? 'done' : idx === step ? 'active' : 'pending';
              return (
                <div key={idx} className={`forgot-step-item ${status}`}>
                  <div className="forgot-step-dot">
                    {status === 'done'
                      ? <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>check</span>
                      : <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>{s.icon}</span>
                    }
                  </div>
                  <span className="forgot-step-label">{s.label}</span>
                  {i < STEPS.length - 1 && <div className="forgot-step-line" />}
                </div>
              );
            })}
          </div>
        )}

        <div className="forgot-body">
          {step === 1 && (
            <StepEmail
              onNext={(mail) => { setEmail(mail); setStep(2); }}
            />
          )}
          {step === 2 && (
            <StepOtp
              email={email}
              onNext={(token) => { setResetToken(token); setStep(3); }}
            />
          )}
          {step === 3 && (
            <StepNewPassword
              resetToken={resetToken}
              onSuccess={() => setStep(4)}
            />
          )}
          {step === 4 && <StepSuccess />}
        </div>
      </div>
    </div>
  );
}
