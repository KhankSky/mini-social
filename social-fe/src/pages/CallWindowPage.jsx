import React, { useEffect, useState, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import webrtcService from '../services/webrtc';
import { API_ORIGIN } from '../config/HttpClient';

const CallWindowPage = () => {
    const [searchParams] = useSearchParams();
    const type = searchParams.get('type'); // 'offer' or 'answer'
    const to = searchParams.get('to');
    const isVideo = searchParams.get('video') === 'true';
    const offerSdp = searchParams.get('sdp');

    const [callStatus, setCallStatus] = useState('Initializing...');
    const [localStream, setLocalStream] = useState(null);
    const [remoteStream, setRemoteStream] = useState(null);
    const localVideoRef = useRef();
    const remoteVideoRef = useRef();

    useEffect(() => {
        const initCall = async () => {
            try {
                setCallStatus('Connecting to signaling...');
                webrtcService.connectSignaling(
                    null, // we handle offer/answer manually here
                    (data) => webrtcService.handleAnswer(data.sdp),
                    (data) => webrtcService.addIceCandidate(data.candidate),
                    () => {
                        setCallStatus('Call Rejected');
                        setTimeout(() => window.close(), 2000);
                    },
                    () => {
                        setCallStatus('Call Ended');
                        setTimeout(() => window.close(), 2000);
                    }
                );

                setCallStatus('Accessing media...');
                const stream = await webrtcService.getMediaStream(true, isVideo);
                setLocalStream(stream);
                if (localVideoRef.current) localVideoRef.current.srcObject = stream;

                webrtcService.onRemoteStream = (rStream) => {
                    setRemoteStream(rStream);
                    if (remoteVideoRef.current) remoteVideoRef.current.srcObject = rStream;
                    setCallStatus('Connected');
                };

                if (type === 'offer') {
                    setCallStatus('Calling...');
                    await webrtcService.createOffer(to, isVideo);
                } else if (type === 'answer') {
                    setCallStatus('Connecting...');
                    await webrtcService.createAnswer(to, offerSdp);
                }
            } catch (err) {
                console.error(err);
                setCallStatus('Error: ' + err.message);
            }
        };

        initCall();

        return () => {
            webrtcService.disconnect();
        };
    }, []);

    const handleEndCall = () => {
        webrtcService.endCall(to);
        window.close();
    };

    return (
        <div className="flex flex-col h-screen bg-gray-900 text-white p-4">
            <div className="flex-1 flex flex-col items-center justify-center relative">
                {/* Remote Video (Full Screen) */}
                <div className="w-full h-full bg-black rounded-2xl overflow-hidden relative">
                    {isVideo ? (
                        <video ref={remoteVideoRef} autoPlay playsInline className="w-full h-full object-cover" />
                    ) : (
                        <div className="w-full h-full flex flex-col items-center justify-center">
                            <div className="w-32 h-32 bg-indigo-600 rounded-full flex items-center justify-center text-4xl font-bold mb-4">
                                {to?.charAt(0).toUpperCase()}
                            </div>
                            <h2 className="text-2xl font-semibold">{to}</h2>
                            <p className="text-gray-400 mt-2">{callStatus}</p>
                        </div>
                    )}

                    {/* Status Overlay */}
                    <div className="absolute top-4 left-4 bg-black/50 px-3 py-1 rounded-full text-xs">
                        {callStatus}
                    </div>

                    {/* Local Video (PIP) */}
                    {isVideo && (
                        <div className="absolute bottom-4 right-4 w-48 h-32 bg-gray-800 rounded-xl overflow-hidden border-2 border-indigo-500 shadow-xl">
                            <video ref={localVideoRef} autoPlay playsInline muted className="w-full h-full object-cover" />
                        </div>
                    )}
                </div>
            </div>

            {/* Controls */}
            <div className="h-24 flex items-center justify-center gap-8">
                <button
                    onClick={() => webrtcService.toggleAudio()}
                    className="w-12 h-12 rounded-full bg-gray-700 hover:bg-gray-600 flex items-center justify-center transition-all"
                >
                    🎤
                </button>
                <button
                    onClick={handleEndCall}
                    className="w-16 h-16 rounded-full bg-red-500 hover:bg-red-600 flex items-center justify-center text-2xl transition-all shadow-lg hover:shadow-red-500/50"
                >
                    📞
                </button>
                {isVideo && (
                    <button
                        onClick={() => webrtcService.toggleVideo()}
                        className="w-12 h-12 rounded-full bg-gray-700 hover:bg-gray-600 flex items-center justify-center transition-all"
                    >
                        📹
                    </button>
                )}
            </div>
        </div>
    );
};

export default CallWindowPage;
