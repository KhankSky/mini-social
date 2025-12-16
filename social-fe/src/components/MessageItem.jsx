import React, { useState } from 'react';
import dayjs from "dayjs";
import { FiCheck, FiMoreVertical, FiEdit2, FiX, FiCheck as FiCheckIcon } from "react-icons/fi";

const MessageItem = ({ message, isOwn, userAvatar, myAvatar, onEditMessage }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [editContent, setEditContent] = useState(message.content);
    const [showActions, setShowActions] = useState(false);

    const handleSave = () => {
        if (editContent.trim() !== message.content) {
            onEditMessage(message.id, editContent);
        }
        setIsEditing(false);
    };

    const handleCancel = () => {
        setEditContent(message.content);
        setIsEditing(false);
    };

    return (
        <div
            className={`flex w-full ${isOwn ? "justify-end" : "justify-start"} group relative`}
            onMouseEnter={() => setShowActions(true)}
            onMouseLeave={() => setShowActions(false)}
        >
            <div className={`flex max-w-[75%] gap-3 ${isOwn ? "flex-row-reverse" : "flex-row"}`}>
                {/* Avatar */}
                <div className="flex-shrink-0 self-end">
                    <img
                        src={isOwn ? myAvatar : userAvatar}
                        alt="avatar"
                        className="w-8 h-8 rounded-full object-cover shadow-sm bg-gray-200"
                    />
                </div>

                {/* Bubble Container */}
                <div className={`flex flex-col ${isOwn ? "items-end" : "items-start"}`}>

                    {/* Bubble */}
                    <div
                        className={`relative px-5 py-3 shadow-sm text-[15px] leading-relaxed break-words ${isOwn
                            ? "bg-gradient-to-tr from-blue-600 to-indigo-600 text-white rounded-2xl rounded-br-none"
                            : "bg-gray-100 text-gray-800 rounded-2xl rounded-bl-none"
                            }`}
                    >
                        {isEditing ? (
                            <div className="flex flex-col gap-2 min-w-[200px]">
                                <textarea
                                    value={editContent}
                                    onChange={(e) => setEditContent(e.target.value)}
                                    className="w-full bg-white/10 text-white border border-white/20 rounded p-2 focus:outline-none focus:ring-1 focus:ring-white/50 text-sm"
                                    rows={2}
                                />
                                <div className="flex justify-end gap-2">
                                    <button onClick={handleCancel} className="p-1 hover:bg-white/10 rounded"><FiX size={14} /></button>
                                    <button onClick={handleSave} className="p-1 hover:bg-white/10 rounded"><FiCheckIcon size={14} /></button>
                                </div>
                            </div>
                        ) : (
                            <>
                                {message.content}
                                {/* Attachments */}
                                {message.attachments && message.attachments.length > 0 && (
                                    <div className="mt-3 flex flex-col gap-2">
                                        {message.attachments.map((url, index) => (
                                            <img
                                                key={index}
                                                src={`http://localhost:9090${url}`}
                                                alt="attachment"
                                                className="max-w-full rounded-lg border border-white/20"
                                            />
                                        ))}
                                    </div>
                                )}
                            </>
                        )}
                    </div>

                    {/* Meta (Time + Status) */}
                    <div className={`flex items-center gap-1 mt-1.5 ${isOwn ? "mr-1" : "ml-1"}`}>
                        <span className="text-[11px] font-medium text-gray-400">
                            {dayjs(message.sentAt).format("hh:mm A")}
                        </span>
                        {isOwn && (
                            <span className={`text-[12px] flex items-center ${message.isRead ? "text-blue-500" : "text-gray-300"}`}>
                                {message.isRead ? <div className="flex -space-x-1"><FiCheck /><FiCheck /></div> : <FiCheck />}
                            </span>
                        )}
                    </div>
                </div>

                {/* Actions Button */}
                {isOwn && !isEditing && (
                    <div className={`self-center opacity-0 group-hover:opacity-100 transition-opacity ${showActions ? 'visible' : 'invisible'}`}>
                        <button
                            onClick={() => setIsEditing(true)}
                            className="p-2 text-gray-400 hover:text-blue-600 hover:bg-gray-100 rounded-full transition-colors"
                            title="Edit"
                        >
                            <FiEdit2 size={16} />
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default MessageItem;
