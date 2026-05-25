import { useState } from 'react';
import PropTypes from 'prop-types';
import { login } from '../../api/auth';
import { useAuth } from '../../context/AuthContext';

export default function LoginForm({ onSwitch }) {
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
      const token = await login(email, password);
      onLogin(token);
    } catch (err) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-card">
      <h2>Sign in</h2>
      <form onSubmit={handleSubmit}>
        <label>Email
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        </label>
        <label>Password
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        </label>
        {error && <p className="auth-error">{error}</p>}
        <button type="submit" disabled={loading}>{loading ? 'Signing in…' : 'Sign in'}</button>
      </form>
      <p>No account? <button className="link-btn" onClick={onSwitch}>Register</button></p>
    </div>
  );
}

LoginForm.propTypes = {
  onSwitch: PropTypes.func.isRequired,
};
