import React, { useRef, useEffect } from 'react';
import { FiPhone, FiMic, FiMicOff, FiVideo, FiVideoOff, FiPhoneOff } from 'react-icons/fi';

const ActiveCallWindow = ({
    peer,
    isVideo,
    localStream,
    remoteStream,
    isMuted,
    isVideoOff,
    onToggleMute,
    onToggleVideo,
    onEndCall
}) => {
    const localVideoRef = useRef();
    const remoteVideoRef = useRef();

    useEffect(() => {
        if (localVideoRef.current && localStream) {
            localVideoRef.current.srcObject = localStream;
        }
    }, [localStream]);

    useEffect(() => {
        if (remoteVideoRef.current && remoteStream) {
            remoteVideoRef.current.srcObject = remoteStream;
        }
    }, [remoteStream]);

    if (!peer) return null;

    return (
        <div className="fixed inset-0 bg-gray-900 z-[100] flex flex-col">
            {/* Header */}
            <div className="h-16 bg-gray-800/50 backdrop-blur-sm px-6 flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <img
                        src={peer.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${peer.username}`}
                        alt={peer.username}
                        className="w-10 h-10 rounded-full"
                    />
                    <div>
                        <h3 className="text-white font-semibold">{peer.username}</h3>
                        <p className="text-gray-400 text-xs">
                            {remoteStream ? 'Connected' : 'Connecting...'}
                        </p>
                    </div>
                </div>
                <div className="text-white text-sm">
                    {isVideo ? '📹 Video Call' : '🎤 Voice Call'}
                </div>
            </div>

            {/* Video Container */}
            <div className="flex-1 relative bg-gray-900">
                {isVideo ? (
                    <>
                        {/* Remote Video (Full Screen) */}
                        <video
                            ref={remoteVideoRef}
                            autoPlay
                            playsInline
                            className="w-full h-full object-cover"
                        />

                        {/* Local Video (Picture in Picture) */}
                        <div className="absolute top-4 right-4 w-48 h-36 bg-gray-800 rounded-xl overflow-hidden shadow-2xl border-2 border-white/20">
                            <video
                                ref={localVideoRef}
                                autoPlay
                                playsInline
                                muted
                                className="w-full h-full object-cover mirror"
                            />
                        </div>

                        {/* No stream placeholder */}
                        {!remoteStream && (
                            <div className="absolute inset-0 flex items-center justify-center">
                                <div className="text-center">
                                    <div className="w-24 h-24 bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-4 animate-pulse">
                                        <FiVideo size={40} className="text-gray-400" />
                                    </div>
                                    <p className="text-white text-lg">Waiting for {peer.username}...</p>
                                </div>
                            </div>
                        )}
                    </>
                ) : (
                    /* Voice Call - Avatar Display */
                    <div className="flex items-center justify-center h-full">
                        <div className="text-center">
                            <div className="w-32 h-32 rounded-full overflow-hidden mx-auto mb-6 ring-4 ring-blue-500 shadow-2xl">
                                <img
                                    src={peer.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${peer.username}`}
                                    alt={peer.username}
                                    className="w-full h-full object-cover"
                                />
                            </div>
                            <h2 className="text-white text-2xl font-bold mb-2">{peer.username}</h2>
                            <p className="text-gray-400">
                                {remoteStream ? 'Call in progress...' : 'Connecting...'}
                            </p>
                        </div>
                    </div>
                )}
            </div>

            {/* Controls */}
            <div className="h-24 bg-gray-800/80 backdrop-blur-sm px-6 flex items-center justify-center gap-6">
                <button
                    onClick={onToggleMute}
                    className={`w-14 h-14 rounded-full flex items-center justify-center transition-all shadow-lg ${isMuted
                        ? 'bg-red-500 hover:bg-red-600 text-white'
                        : 'bg-gray-700 hover:bg-gray-600 text-white'
                        }`}
                    title={isMuted ? 'Unmute' : 'Mute'}
                >
                    {isMuted ? <FiMicOff size={24} /> : <FiMic size={24} />}
                </button>

                {isVideo && (
                    <button
                        onClick={onToggleVideo}
                        className={`w-14 h-14 rounded-full flex items-center justify-center transition-all shadow-lg ${isVideoOff
                            ? 'bg-red-500 hover:bg-red-600 text-white'
                            : 'bg-gray-700 hover:bg-gray-600 text-white'
                            }`}
                        title={isVideoOff ? 'Turn on camera' : 'Turn off camera'}
                    >
                        {isVideoOff ? <FiVideoOff size={24} /> : <FiVideo size={24} />}
                    </button>
                )}

                <button
                    onClick={onEndCall}
                    className="w-16 h-16 rounded-full bg-red-600 hover:bg-red-700 text-white flex items-center justify-center transition-all shadow-2xl transform hover:scale-110"
                    title="End call"
                >
                    <FiPhoneOff size={28} />
                </button>
            </div>

            <style jsx>{`
                .mirror {
                    transform: scaleX(-1);
                }
            `}</style>
        </div>
    );
};

export default ActiveCallWindow;
