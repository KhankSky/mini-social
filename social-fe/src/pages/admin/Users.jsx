import React, { useEffect, useState } from 'react';
import UserService from '../../services/user';

const UserForm = ({ user = {}, onSave, onCancel }) => {
  const [email, setEmail] = useState(user.email || '');
  const [username, setUsername] = useState(user.username || '');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    const payload = { email, username };
    if (user.id) payload.id = user.id; // backend may ignore
    if (password) payload.password = password;
    onSave(payload);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <div>
        <label className="block text-sm">Email</label>
        <input value={email} onChange={e=>setEmail(e.target.value)} className="w-full border px-2 py-1 rounded" />
      </div>
      <div>
        <label className="block text-sm">Username</label>
        <input value={username} onChange={e=>setUsername(e.target.value)} className="w-full border px-2 py-1 rounded" />
      </div>
      <div>
        <label className="block text-sm">Password (leave empty to keep)</label>
        <input type="password" value={password} onChange={e=>setPassword(e.target.value)} className="w-full border px-2 py-1 rounded" />
      </div>
      <div className="flex gap-2">
        <button className="bg-blue-600 text-white px-3 py-1 rounded" type="submit">Save</button>
        <button type="button" onClick={onCancel} className="px-3 py-1 border rounded">Cancel</button>
      </div>
    </form>
  );
};

const Users = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState(null);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await UserService.list();
      // backend returns pagination wrapper or list; handle both
      const data = res?.content ? res.content : res;
      setUsers(data || []);
    } catch (err) {
      console.error(err);
      setError(err.response?.data || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, []);

  const handleCreate = () => {
    setEditing(null);
    setShowForm(true);
  };

  const handleEdit = (u) => {
    setEditing(u);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this user?')) return;
    try {
      await UserService.remove(id);
      fetchUsers();
    } catch (err) {
      console.error(err);
      setError(err.response?.data || 'Delete failed');
    }
  };

  const handleSave = async (payload) => {
    try {
      if (editing) {
        await UserService.update(payload);
      } else {
        await UserService.create(payload);
      }
      setShowForm(false);
      fetchUsers();
    } catch (err) {
      console.error(err);
      setError(err.response?.data || 'Save failed');
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-semibold">Users</h1>
        <div>
          <button onClick={handleCreate} className="bg-blue-600 text-white px-3 py-1 rounded">New user</button>
        </div>
      </div>

      {error && <div className="text-red-600 mb-3">{String(error)}</div>}

      {showForm && (
        <div className="mb-4 bg-white p-4 rounded shadow">
          <UserForm user={editing} onSave={handleSave} onCancel={() => setShowForm(false)} />
        </div>
      )}

      <div className="bg-white rounded shadow overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="p-2 text-left">ID</th>
              <th className="p-2 text-left">Email</th>
              <th className="p-2 text-left">Username</th>
              <th className="p-2 text-left">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={4} className="p-4">Loading...</td></tr>
            ) : users.length === 0 ? (
              <tr><td colSpan={4} className="p-4">No users</td></tr>
            ) : users.map(u => (
              <tr key={u.id} className="border-t">
                <td className="p-2">{u.id}</td>
                <td className="p-2">{u.email}</td>
                <td className="p-2">{u.username}</td>
                <td className="p-2">
                  <div className="flex gap-2">
                    <button onClick={() => handleEdit(u)} className="px-2 py-1 border rounded">Edit</button>
                    <button onClick={() => handleDelete(u.id)} className="px-2 py-1 border rounded text-red-600">Delete</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Users;
