import { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginForm from './components/Auth/LoginForm';
import RegisterForm from './components/Auth/RegisterForm';
import ConnectLeagueForm from './components/Leagues/ConnectLeagueForm';
import './App.css';

function AppContent() {
  const { isLoggedIn, onLogout } = useAuth();
  const [showRegister, setShowRegister] = useState(false);

  if (isLoggedIn) {
    return (
      <div className="dashboard">
        <header className="dashboard-header">
          <h1>One Stop Fantasy</h1>
          <button onClick={onLogout}>Sign out</button>
        </header>
        <main>
          <ConnectLeagueForm />
        </main>
      </div>
    );
  }

  return showRegister
    ? <RegisterForm onSwitch={() => setShowRegister(false)} />
    : <LoginForm onSwitch={() => setShowRegister(true)} />;
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
