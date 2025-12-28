import React, { useState } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { Layout, Menu, Breadcrumb, Button } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  MailOutlined,
  BellOutlined,
  QuestionCircleOutlined,
  UserOutlined,
  AppstoreOutlined,
} from '@ant-design/icons';

const { Header, Sider, Content, Footer } = Layout;

const AdminLayout = () => {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();

  const pathSnippets = location.pathname.split('/').filter((i) => i);
  const breadcrumbItems = [
    <Breadcrumb.Item key="home">
      <Link to="/">Home</Link>
    </Breadcrumb.Item>,
    ...pathSnippets.map((snippet, index) => {
      const url = `/${pathSnippets.slice(0, index + 1).join('/')}`;
      const isLast = index === pathSnippets.length - 1;
      const title = snippet.charAt(0).toUpperCase() + snippet.slice(1);
      return (
        <Breadcrumb.Item key={url}>
          {isLast ? title : <Link to={url}>{title}</Link>}
        </Breadcrumb.Item>
      );
    }),
  ];

  const menuItems = [
    { key: 'users', icon: <UserOutlined />, label: <Link to="/admin/users">Users</Link> },
    { key: 'posts', icon: <AppstoreOutlined />, label: <Link to="/admin/posts">Posts</Link> },
  ];

  // derive selected key from pathname
  const selectedKey = pathSnippets[1] || pathSnippets[0] || 'users';

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={(val) => setCollapsed(val)}>
        <div className="logo" style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 700 }}>
          <Link to="/admin" style={{ color: 'inherit' }}>{collapsed ? 'AD' : 'Admin'}</Link>
        </div>
        <Menu theme="dark" mode="inline" selectedKeys={[selectedKey]} items={menuItems} />
      </Sider>

      <Layout>
        <Header style={{ padding: 0, background: '#fff', borderBottom: '1px solid #f0f0f0' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <Button type="text" onClick={() => setCollapsed(!collapsed)} icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />} />
              <Breadcrumb style={{ margin: '0 8px' }}>{breadcrumbItems}</Breadcrumb>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: 16, fontSize: 18 }}>
              <MailOutlined />
              <BellOutlined />
              <QuestionCircleOutlined />
            </div>
          </div>
        </Header>

        <Content style={{ margin: '24px 16px' }}>
          <div style={{ padding: 24, minHeight: 360, background: '#fff' }}>
            <Outlet />
          </div>
        </Content>

        <Footer style={{ textAlign: 'center' }}>MiniSocial Admin ©{new Date().getFullYear()}</Footer>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;