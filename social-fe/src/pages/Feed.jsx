import React, { useEffect, useState } from 'react';
import Sidebar from '../components/feed/Sidebar.jsx';
import RightSidebar from '../components/feed/RightSidebar.jsx';
import ComposeBox from '../components/feed/ComposeBox.jsx';
import PostCard from '../components/feed/PostCard.jsx';
import httpClient from '../config/HttpClient';

const samplePosts = [
  {
    id: 1,
    name: 'Nguyễn Văn A',
    avatar: '/uploads/default-avatar.png',
    time: '2h',
    position: 'Software Engineer • MiniSocial',
    content: 'Today I learned how to build a LinkedIn-like UI with React and Tailwind. Sharing a small demo!',
    image: '',
    likes: 12,
    comments: 3,
  },
  {
    id: 2,
    name: 'Trần Thị B',
    avatar: '/uploads/default-avatar.png',
    time: '1d',
    position: 'Frontend Developer',
    content: 'Check out this UI kit for social feeds — makes prototyping fast.',
    image: '',
    likes: 8,
    comments: 1,
  },
];

const Feed = () => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const loadMe = async () => {
      try {
        const res = await httpClient.get('/auth/me');
        setUser(res.data);
      } catch (err) {
        console.warn('Could not load current user', err);
      }
    };
    loadMe();
  }, []);

  return (
    <div className="max-w-6xl mx-auto px-4">
      <div className="grid grid-cols-12 gap-6">
        <div className="col-span-12 md:col-span-3">
          <Sidebar user={user} />
        </div>

        <div className="col-span-12 md:col-span-6">
          <div className="space-y-4">
            <ComposeBox user={user} />
            {samplePosts.map((p) => (
              <PostCard key={p.id} post={p} />
            ))}
          </div>
        </div>

        <div className="col-span-12 md:col-span-3 hidden lg:block">
          <RightSidebar />
        </div>
      </div>
    </div>
  );
};

export default Feed;