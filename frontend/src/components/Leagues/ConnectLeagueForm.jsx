import { useState, useEffect } from 'react';
import { getLeagues, connectSleeper, deleteLeague } from '../../api/leagues';

export default function ConnectLeagueForm() {
  const [leagues, setLeagues] = useState([]);
  const [username, setUsername] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    getLeagues().then(setLeagues).catch(() => {});
  }, []);

  async function handleConnect(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const added = await connectSleeper(username.trim());
      setLeagues(prev => [...prev, ...added]);
      setUsername('');
      if (added.length === 0) setError('No leagues found for that username, or they are already connected.');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleDisconnect(id) {
    try {
      await deleteLeague(id);
      setLeagues(prev => prev.filter(l => l.id !== id));
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="league-panel">
      <h2>Connect a Sleeper Account</h2>
      <form onSubmit={handleConnect} className="connect-form">
        <input
          type="text"
          placeholder="Sleeper username"
          value={username}
          onChange={e => setUsername(e.target.value)}
          required
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Connecting…' : 'Connect'}
        </button>
      </form>
      {error && <p className="form-error">{error}</p>}

      {leagues.length > 0 && (
        <div className="league-list">
          <h3>Your Leagues</h3>
          <ul>
            {leagues.map(league => (
              <li key={league.id} className="league-item">
                <span>{league.leagueName}</span>
                <button onClick={() => handleDisconnect(league.id)} className="disconnect-btn">
                  Disconnect
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
