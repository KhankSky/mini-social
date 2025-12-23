import React, { useState, useEffect } from 'react';
import { getAllPosts, deletePost } from '../../services/admin';

const ContentManagement = () => {
    const [posts, setPosts] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadPosts();
    }, [page]);

    const loadPosts = async () => {
        setLoading(true);
        try {
            const data = await getAllPosts(page, 20);
            setPosts(data.content);
            setTotalPages(data.totalPages);
        } catch (error) {
            console.error('Failed to load posts', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (postId, author) => {
        if (window.confirm(`Delete post by "${author}"? This will also delete all comments.`)) {
            try {
                await deletePost(postId);
                loadPosts();
            } catch (error) {
                alert('Failed to delete post: ' + (error.response?.data?.message || error.message));
            }
        }
    };

    if (loading && posts.length === 0) {
        return <div className="p-8 text-center">Loading posts...</div>;
    }

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold mb-6">Content Management</h2>

            <div className="space-y-4">
                {posts.map((post) => (
                    <div key={post.id} className="bg-white rounded-lg shadow p-6">
                        <div className="flex justify-between items-start">
                            <div className="flex-1">
                                <div className="flex items-center gap-3 mb-3">
                                    <img
                                        src={post.user?.avatarUrl || `https://ui-avatars.com/api/?name=${post.user?.username}`}
                                        alt={post.user?.username}
                                        className="w-10 h-10 rounded-full"
                                    />
                                    <div>
                                        <p className="font-semibold">{post.user?.username}</p>
                                        <p className="text-sm text-gray-500">
                                            {new Date(post.createdAt).toLocaleString()}
                                        </p>
                                    </div>
                                </div>
                                <p className="text-gray-800 mb-2">{post.content}</p>
                                {post.attachments && post.attachments.length > 0 && (
                                    <div className="flex gap-2 flex-wrap">
                                        {post.attachments.slice(0, 3).map((att, idx) => (
                                            <img
                                                key={idx}
                                                src={att.url}
                                                alt="attachment"
                                                className="w-20 h-20 object-cover rounded"
                                            />
                                        ))}
                                    </div>
                                )}
                                <div className="text-sm text-gray-500 mt-2">
                                    💬 {post.commentCount || 0} comments · 👍 {post.likeCount || 0} likes
                                </div>
                            </div>
                            <button
                                onClick={() => handleDelete(post.id, post.user?.username)}
                                className="ml-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 text-sm"
                            >
                                Delete Post
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
                <div className="flex justify-center gap-2 mt-6">
                    <button
                        onClick={() => setPage(p => Math.max(0, p - 1))}
                        disabled={page === 0}
                        className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 disabled:opacity-50"
                    >
                        Previous
                    </button>
                    <span className="px-4 py-2">
                        Page {page + 1} of {totalPages}
                    </span>
                    <button
                        onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                        disabled={page >= totalPages - 1}
                        className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 disabled:opacity-50"
                    >
                        Next
                    </button>
                </div>
            )}
        </div>
    );
};

export default ContentManagement;
