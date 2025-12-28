import React from 'react';
import { FiPhone, FiPhoneOff, FiVideo } from 'react-icons/fi';

const IncomingCallModal = ({ caller, isVideo, onAccept, onReject }) => {
    if (!caller) return null;

    return (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-[100] backdrop-blur-sm animate-fadeIn">
            <div className="bg-white rounded-3xl shadow-2xl p-8 max-w-sm w-full mx-4 text-center">
                {/* Caller Avatar */}
                <div className="mb-6">
                    <div className="w-24 h-24 mx-auto rounded-full overflow-hidden shadow-lg ring-4 ring-blue-100 animate-pulse">
                        <img
                            src={caller.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${caller.username}`}
                            alt={caller.username}
                            className="w-full h-full object-cover"
                        />
                    </div>
                </div>

                {/* Call Info */}
                <h2 className="text-2xl font-bold text-gray-800 mb-2">{caller.username}</h2>
                <div className="flex items-center justify-center gap-2 mb-8">
                    {isVideo ? <FiVideo className="text-blue-600" /> : <FiPhone className="text-blue-600" />}
                    <span className="text-gray-600 text-sm">
                        Incoming {isVideo ? 'video' : 'voice'} call...
                    </span>
                </div>

                {/* Actions */}
                <div className="flex gap-4 justify-center">
                    <button
                        onClick={onReject}
                        className="w-16 h-16 rounded-full bg-red-500 hover:bg-red-600 text-white flex items-center justify-center shadow-lg transform hover:scale-110 transition-all"
                    >
                        <FiPhoneOff size={24} />
                    </button>
                    <button
                        onClick={onAccept}
                        className="w-16 h-16 rounded-full bg-green-500 hover:bg-green-600 text-white flex items-center justify-center shadow-lg transform hover:scale-110 transition-all"
                    >
                        <FiPhone size={24} />
                    </button>
                </div>
            </div>
        </div>
    );
};

export default IncomingCallModal;
