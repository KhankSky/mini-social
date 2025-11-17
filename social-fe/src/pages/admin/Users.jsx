import React, { useEffect, useState, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, Popconfirm, notification } from 'antd';
import UserService from '../../services/user';

const Users = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [form] = Form.useForm();

  const openNotification = (type, message, description) => {
    notification[type]({ message, description });
  };

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const res = await UserService.list();
      const data = res?.content ? res.content : res;
      setUsers(data || []);
    } catch (err) {
      console.error(err);
      openNotification('error', 'Load failed', err.response?.data || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const handleCreate = () => {
    setEditingUser(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setEditingUser(record);
    form.setFieldsValue({ email: record.email, username: record.username });
    setModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await UserService.remove(id);
      openNotification('success', 'Deleted', 'User removed');
      fetchUsers();
    } catch (err) {
      console.error(err);
      openNotification('error', 'Delete failed', err.response?.data || 'Could not delete user');
    }
  };

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      if (editingUser) {
        const payload = { ...editingUser, ...values };
        await UserService.update(payload);
        openNotification('success', 'Updated', 'User updated successfully');
      } else {
        await UserService.create(values);
        openNotification('success', 'Created', 'User created successfully');
      }
      setModalVisible(false);
      fetchUsers();
    } catch (err) {
      console.error(err);
      if (err.errorFields) {
        // validation error from form, ignore
      } else {
        openNotification('error', 'Save failed', err.response?.data || 'Could not save user');
      }
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Username', dataIndex: 'username', key: 'username' },
    {
      title: 'Actions',
      key: 'actions',
      width: 200,
      render: (_, record) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <Button onClick={() => handleEdit(record)}>Edit</Button>
          <Popconfirm title="Delete this user?" onConfirm={() => handleDelete(record.id)}>
            <Button danger>Delete</Button>
          </Popconfirm>
        </div>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h1 className="text-xl font-semibold">Users</h1>
        <Button type="primary" onClick={handleCreate}>New user</Button>
      </div>

      <Table rowKey="id" dataSource={users} columns={columns} loading={loading} />

      <Modal
        title={editingUser ? 'Edit User' : 'New User'}
        visible={modalVisible}
        onOk={handleOk}
        onCancel={() => setModalVisible(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" initialValues={{ email: '', username: '' }}>
          <Form.Item name="email" label="Email" rules={[{ required: true, message: 'Please input email' }, { type: 'email', message: 'Invalid email' }]}>
            <Input />
          </Form.Item>

          <Form.Item name="username" label="Username">
            <Input />
          </Form.Item>

          <Form.Item name="password" label="Password" rules={[{ min: 6, message: 'Password must be at least 6 characters' }]}>
            <Input.Password placeholder={editingUser ? 'Leave empty to keep current password' : ''} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Users;
