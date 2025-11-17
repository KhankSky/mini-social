import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import AuthService from '../services/auth';

const Register = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await AuthService.register({ email, password, username });
      // after register, redirect to login
      navigate('/login');
    } catch (err) {
      console.error(err);
      setError(err.response?.data || 'Register failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="w-full max-w-md bg-white p-6 rounded shadow">
        <h2 className="text-2xl font-semibold mb-4">Create account</h2>
        {error && <div className="text-red-600 mb-2">{String(error)}</div>}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm text-gray-700">Email</label>
            <input value={email} onChange={e=>setEmail(e.target.value)} className="mt-1 w-full border px-3 py-2 rounded" />
          </div>
          <div>
            <label className="block text-sm text-gray-700">Password</label>
            <input type="password" value={password} onChange={e=>setPassword(e.target.value)} className="mt-1 w-full border px-3 py-2 rounded" />
          </div>
          <div>
            <label className="block text-sm text-gray-700">Username (optional)</label>
            <input value={username} onChange={e=>setUsername(e.target.value)} className="mt-1 w-full border px-3 py-2 rounded" />
          </div>

          <div className="flex items-center justify-between">
            <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded">Register</button>
            <Link to="/login" className="text-sm text-blue-600">Sign in</Link>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Register;
