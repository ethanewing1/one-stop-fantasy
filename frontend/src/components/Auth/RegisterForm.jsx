import { useState } from 'react';
import { register } from '../../api/auth';
import { useAuth } from '../../context/AuthContext';

export default function RegisterForm({ onSwitch }) {
  const { onLogin } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const token = await register(email, password);
      onLogin(token);
    } catch (err) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-card">
      <h2>Create account</h2>
      <form onSubmit={handleSubmit}>
        <label>Email
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        </label>
        <label>Password
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={8} />
        </label>
        {error && <p className="auth-error">{error}</p>}
        <button type="submit" disabled={loading}>{loading ? 'Creating account…' : 'Create account'}</button>
      </form>
      <p>Already have an account? <button className="link-btn" onClick={onSwitch}>Sign in</button></p>
    </div>
  );
}
