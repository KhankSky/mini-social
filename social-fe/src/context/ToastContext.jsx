import React, { createContext, useContext, useState } from 'react';
import ToastNotification from '../components/common/ToastNotification';

const ToastContext = createContext();

export const useToast = () => {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useToast must be used within ToastProvider');
    }
    return context;
};

export const ToastProvider = ({ children }) => {
    const [toasts, setToasts] = useState([]);

    const showToast = (notification) => {
        const id = Date.now() + Math.random();
        const newToast = { ...notification, id };

        setToasts((prev) => {
            // Keep max 3 toasts
            const updated = [newToast, ...prev].slice(0, 3);
            return updated;
        });

        return id;
    };

    const removeToast = (id) => {
        setToasts((prev) => prev.filter((toast) => toast.id !== id));
    };

    const handleToastClick = (notification) => {
        // Navigate based on notification type
        if (notification.type === 'COMMENT' && notification.referenceId) {
            window.location.href = `/post/${notification.referenceId}`;
        } else if (notification.type === 'FRIEND_REQUEST') {
            window.location.href = '/friends';
        } else if (notification.type === 'MESSAGE') {
            window.location.href = '/messages';
        }
    };

    return (
        <ToastContext.Provider value={{ showToast, removeToast }}>
            {children}

            {/* Render toasts */}
            <div className="fixed top-0 right-0 z-[9999] pointer-events-none">
                <div className="flex flex-col gap-3 p-4 pointer-events-auto">
                    {toasts.map((toast, index) => (
                        <div
                            key={toast.id}
                            style={{
                                transform: `translateY(${index * 10}px)`,
                                zIndex: 9999 - index,
                            }}
                        >
                            <ToastNotification
                                notification={toast}
                                onClose={() => removeToast(toast.id)}
                                onClick={handleToastClick}
                            />
                        </div>
                    ))}
                </div>
            </div>
        </ToastContext.Provider>
    );
};
