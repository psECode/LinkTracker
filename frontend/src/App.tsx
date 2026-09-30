import type { ReactNode } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import { useAuth } from './auth/AuthContext';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import LinksPage from './pages/LinksPage';
import NotificationsPage from './pages/NotificationsPage';
import AccountPage from './pages/AccountPage';

function RedirectIfAuthed({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="loading-screen">
        <p className="loading">Загрузка…</p>
      </div>
    );
  }

  if (user !== null) {
    return <Navigate to="/links" replace />;
  }

  return <>{children}</>;
}

export default function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <RedirectIfAuthed>
            <LoginPage />
          </RedirectIfAuthed>
        }
      />
      <Route
        path="/register"
        element={
          <RedirectIfAuthed>
            <RegisterPage />
          </RedirectIfAuthed>
        }
      />
      <Route element={<Layout />}>
        <Route path="/links" element={<LinksPage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/account" element={<AccountPage />} />
        <Route path="/" element={<Navigate to="/links" replace />} />
        <Route path="*" element={<Navigate to="/links" replace />} />
      </Route>
    </Routes>
  );
}
