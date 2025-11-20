import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import AuthService from '../services/auth';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = await AuthService.login({ username, password });
      // backend returns token string
      localStorage.setItem('social_app_token', token);
      navigate('/');
    } catch (err) {
      console.error(err);
      setError(err.response?.data || 'Login failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="w-full max-w-md bg-white p-6 rounded shadow">
        <h2 className="text-2xl font-semibold mb-4">Sign in</h2>
        {error && <div className="text-red-600 mb-2">{String(error)}</div>}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm text-gray-700">Email or Username</label>
            <input value={username} onChange={e=>setUsername(e.target.value)} className="mt-1 w-full border px-3 py-2 rounded" />
          </div>
          <div>
            <label className="block text-sm text-gray-700">Password</label>
            <input type="password" value={password} onChange={e=>setPassword(e.target.value)} className="mt-1 w-full border px-3 py-2 rounded" />
          </div>

          <div className="flex items-center justify-between">
            <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded">Sign in</button>
            <Link to="/register" className="text-sm text-blue-600">Register</Link>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Login;
