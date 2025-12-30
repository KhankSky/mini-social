import { Link } from 'react-router-dom';
import { API_ORIGIN } from '../../config/HttpClient';
import { useState, useEffect, useRef } from 'react';
import NotificationDropdown from '../layout/NotificationDropdown';
import { getMyNotifications, markAsRead, markAllAsRead } from '../../services/notification';
import notificationWebSocket from '../../services/notificationWebSocket';
import AuthService from '../../services/auth';
import SearchModal from '../common/SearchModal';
import { useToast } from '../../context/ToastContext';

const resolveImageUrl = (url) => {
  if (!url) return 'https://api.dicebear.com/7.x/avataaars/svg?seed=User';
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/")) return `${API_ORIGIN}${url}`;
  return `${API_ORIGIN}/${url}`;
};

const Sidebar = ({ user }) => {
  const avatar = user ? resolveImageUrl(user.avatar || user.avatarUrl || user.photo) : 'https://api.dicebear.com/7.x/avataaars/svg?seed=User';
  const name = user && (user.fullName || user.name || user.username) ? (user.fullName || user.name || user.username) : 'Bogdan Nikitin';
  const username = user && user.username ? `@${user.username}` : '@nikitinteam';

  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const { showToast } = useToast();
  const audioRef = useRef(null);

  useEffect(() => {
    // Request browser notification permission
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }

    loadNotifications();
    notificationWebSocket.connect((newNotification) => {
      setNotifications(prev => [newNotification, ...prev]);
      setUnreadCount(prev => prev + 1);

      // Show toast notification
      showToast(newNotification);

      // Play notification sound
      playNotificationSound();

      // Show browser notification if tab is not active
      if (document.hidden && 'Notification' in window && Notification.permission === 'granted') {
        const notifText = getNotificationText(newNotification);
        new Notification(newNotification.actorName, {
          body: notifText,
          icon: newNotification.actorAvatar || `https://ui-avatars.com/api/?name=${newNotification.actorName}`,
          tag: `notification-${newNotification.id}`,
        });
      }
    });

    return () => {
      notificationWebSocket.disconnect();
    };
  }, [showToast]);

  const playNotificationSound = () => {
    try {
      // Create audio element if it doesn't exist
      if (!audioRef.current) {
        audioRef.current = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBjOM0fDTgjMGHm7A7+OZSA0PVqzn7bBlGw1Mn9vvwmUeByKE0e/glkIKE1yw5+2sTxINTKXe77pnHQYlhtLu4p1BCxBXrubtpFgVChBI');
        audioRef.current.volume = 0.5;
      }
      audioRef.current.play().catch(err => console.log('Audio play failed:', err));
    } catch (error) {
      console.error('Failed to play notification sound:', error);
    }
  };

  const getNotificationText = (notification) => {
    switch (notification.type) {
      case 'COMMENT':
        return 'commented on your post';
      case 'FRIEND_REQUEST':
        return 'sent you a friend request';
      case 'FRIEND_ACCEPT':
        return 'accepted your friend request';
      case 'MESSAGE':
        return 'sent you a message';
      default:
        return notification.content || 'sent a notification';
    }
  };

  const loadNotifications = async () => {
    try {
      const data = await getMyNotifications();
      setNotifications(data);
      setUnreadCount(data.filter(n => !n.isRead).length);
    } catch (error) {
      console.error("Failed to load notifications", error);
    }
  };

  const handleMarkRead = async (id) => {
    try {
      if (id === "all") {
        await markAllAsRead();
        setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
        setUnreadCount(0);
      } else {
        await markAsRead(id);
        setNotifications(prev => prev.map(n => n.id === id ? ({ ...n, isRead: true }) : n));
        setUnreadCount(prev => Math.max(0, prev - 1));
      }
    } catch (error) {
      console.error("Failed to mark read", error);
    }
  };

  return (
    <>
      <div className="flex items-center gap-4 mb-8">
        <img
          src={avatar}
          alt={name}
          className="w-16 h-16 rounded-full object-cover"
        />
        <div className="flex-1">
          <div className="font-bold">{name}</div>
          <div className="text-sm text-gray-500">{username}</div>
        </div>
      </div>

      <nav className="space-y-2 flex-1">
        <Link to="/" className="w-full flex items-center gap-3 px-4 py-3 bg-black text-white rounded-full">
          <div className="w-6 h-6 bg-white rounded-full flex items-center justify-center text-black text-xs">📰</div>
          <span>News Feed</span>
        </Link>
        <Link to="/messages" className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-100 rounded-full relative">
          <span className="text-xl">✉️</span>
          <span>Messages</span>
          <span className="ml-auto bg-black text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">6</span>
        </Link>

        <div className="relative">
          <button
            onClick={() => setShowDropdown(!showDropdown)}
            className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-100 rounded-full"
          >
            <span className="text-xl">🔔</span>
            <span>Notifications</span>
            {unreadCount > 0 && (
              <span className="ml-auto bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>
          {showDropdown && (
            <div className="absolute left-full ml-4 top-0 z-[100]">
              <NotificationDropdown
                notifications={notifications}
                onMarkRead={handleMarkRead}
                onClose={() => setShowDropdown(false)}
              />
            </div>
          )}
        </div>

        <Link to="/friends" className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-100 rounded-full relative">
          <span className="text-xl">👥</span>
          <span>Friends</span>
          <span className="ml-auto bg-black text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">3</span>
        </Link>




        <Link to="/settings" className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-100 rounded-full">
          <span className="text-xl">⚙️</span>
          <span>Settings</span>
        </Link>

        <div className="mt-auto pt-4 border-t border-gray-100">
          <button
            onClick={() => AuthService.logout()}
            className="w-full flex items-center gap-3 px-4 py-3 hover:bg-red-50 text-red-600 rounded-full"
          >
            <span className="text-xl">🚪</span>
            <span>Logout</span>
          </button>
        </div>
      </nav>

      {showSearch && <SearchModal onClose={() => setShowSearch(false)} />}
    </>
  );
};

export default Sidebar; 