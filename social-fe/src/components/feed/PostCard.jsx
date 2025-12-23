import React, { useEffect, useState } from 'react';
import { fetchComments, createComment } from '../../services/comment';
import { likePost, unlikePost } from '../../services/post';
import { API_ORIGIN } from '../../config/HttpClient';

const resolveImageUrl = (url, fallback = '') => {
  if (!url || url.trim().length === 0) return fallback;
  const trimmed = url.trim();

  if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
    return trimmed;
  }

  if (trimmed.startsWith('//')) {
    return `https:${trimmed}`;
  }

  if (trimmed.startsWith('/')) {
    return `${API_ORIGIN}${trimmed}`;
  }

  return `${API_ORIGIN}/uploads/${trimmed}`;
};

const buildGalleryLayout = (attachments, reactions) => {
  const images = attachments || [];
  const count = images.length;
  if (count === 0) return null;

  const renderImage = (img, extraClass = '') => (
    <img
      key={img.id || img.fileUrl}
      src={resolveImageUrl(img.fileUrl)}
      alt={img.fileName || 'image'}
      className={`w-full h-full object-cover rounded-2xl ${extraClass}`}
    />
  );

  // For new UI, use grid-cols-3 layout
  if (count >= 3) {
    const visibleImages = images.slice(0, 3);
    return (
      <div className="grid grid-cols-3 gap-2 mb-4 relative">
        {visibleImages.map((img, i) => (
          <div key={img.id || img.fileUrl} className="relative">
            {renderImage(img, 'h-48')}
          </div>
        ))}
        {reactions && reactions.length > 0 && (
          <div className="absolute bottom-4 right-4 bg-white/90 backdrop-blur rounded-full px-3 py-2 flex gap-1">
            {reactions.map((emoji, i) => (
              <span key={i} className="text-lg">{emoji}</span>
            ))}
          </div>
        )}
      </div>
    );
  }

  if (count === 2) {
    return (
      <div className="grid grid-cols-2 gap-2 mb-4 relative">
        {images.map((img) => (
          <div key={img.id || img.fileUrl} className="relative">
            {renderImage(img, 'h-48')}
          </div>
        ))}
        {reactions && reactions.length > 0 && (
          <div className="absolute bottom-4 right-4 bg-white/90 backdrop-blur rounded-full px-3 py-2 flex gap-1">
            {reactions.map((emoji, i) => (
              <span key={i} className="text-lg">{emoji}</span>
            ))}
          </div>
        )}
      </div>
    );
  }

  if (count === 1) {
    return (
      <div className="mb-4 relative">
        {renderImage(images[0], 'h-48 rounded-2xl')}
        {reactions && reactions.length > 0 && (
          <div className="absolute bottom-4 right-4 bg-white/90 backdrop-blur rounded-full px-3 py-2 flex gap-1">
            {reactions.map((emoji, i) => (
              <span key={i} className="text-lg">{emoji}</span>
            ))}
          </div>
        )}
      </div>
    );
  }

  return null;
};

const CommentItem = ({ comment, onReply }) => {
  const [showReply, setShowReply] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmitReply = async () => {
    if (!replyContent.trim()) return;
    try {
      setSubmitting(true);
      await onReply(comment.id, replyContent);
      setReplyContent('');
      setShowReply(false);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mt-2">
      <div className="flex items-start gap-2">
        <img
          src={resolveImageUrl(comment.userAvatarUrl || '/uploads/default-avatar.png')}
          alt={comment.username}
          className="w-8 h-8 rounded-full object-cover"
        />
        <div className="bg-gray-100 rounded-2xl px-3 py-2 flex-1">
          <div className="font-semibold text-sm">{comment.username}</div>
          <div className="text-sm text-gray-800 whitespace-pre-line">{comment.content}</div>
          <div className="mt-1 flex items-center gap-4 text-xs text-gray-500">
            <button
              type="button"
              className="hover:underline"
              onClick={() => setShowReply((prev) => !prev)}
            >
              Trả lời
            </button>
          </div>
        </div>
      </div>

      {showReply && (
        <div className="ml-10 mt-2 flex gap-2">
          <input
            type="text"
            className="flex-1 rounded-full border px-3 py-1 text-sm"
            placeholder={`Trả lời ${comment.username}...`}
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
          />
          <button
            type="button"
            onClick={handleSubmitReply}
            disabled={submitting || !replyContent.trim()}
            className="text-sm px-3 py-1 rounded-full bg-blue-600 text-white disabled:bg-blue-300"
          >
            Gửi
          </button>
        </div>
      )}

      {comment.replies && comment.replies.length > 0 && (
        <div className="ml-10 mt-1 space-y-1">
          {comment.replies.map((child) => (
            <CommentItem key={child.id} comment={child} onReply={onReply} />
          ))}
        </div>
      )}
    </div>
  );
};

const PostCard = ({ post }) => {
  const [comments, setComments] = useState([]);
  const [showComments, setShowComments] = useState(false);
  const [loadingComments, setLoadingComments] = useState(false);
  const [newComment, setNewComment] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);

  // Like state
  const [isLiked, setIsLiked] = useState(post.isLikedByCurrentUser || false);
  const [likeCount, setLikeCount] = useState(post.likeCount || 0);
  const [likeAnimating, setLikeAnimating] = useState(false);

  const loadComments = async () => {
    if (loadingComments) return;
    try {
      setLoadingComments(true);
      const res = await fetchComments(post.id);
      setComments(res.data || []);
    } catch (err) {
      console.error('Failed to load comments', err);
    } finally {
      setLoadingComments(false);
    }
  };

  useEffect(() => {
    if (showComments && comments.length === 0) {
      loadComments();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [showComments]);

  const handleToggleComments = () => {
    setShowComments((prev) => !prev);
  };

  const handleCreateComment = async () => {
    if (!newComment.trim()) return;
    try {
      setSubmittingComment(true);
      const res = await createComment(post.id, { content: newComment.trim(), parentCommentId: null });
      setComments((prev) => [...prev, res.data]);
      setNewComment('');
    } catch (err) {
      console.error('Failed to create comment', err);
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleReply = async (parentId, content) => {
    const res = await createComment(post.id, { content: content.trim(), parentCommentId: parentId });
    const newReply = res.data;

    const addReply = (items) =>
      items.map((c) => {
        if (c.id === parentId) {
          return {
            ...c,
            replies: [...(c.replies || []), newReply],
          };
        }
        if (c.replies && c.replies.length > 0) {
          return {
            ...c,
            replies: addReply(c.replies),
          };
        }
        return c;
      });

    setComments((prev) => addReply(prev));
  };

  const handleToggleLike = async () => {
    const prevLiked = isLiked;
    const prevCount = likeCount;

    // Optimistic update
    setIsLiked(!prevLiked);
    setLikeCount(prevLiked ? prevCount - 1 : prevCount + 1);
    setLikeAnimating(true);
    setTimeout(() => setLikeAnimating(false), 300); // Reset animation state

    try {
      if (prevLiked) {
        await unlikePost(post.id);
      } else {
        await likePost(post.id);
      }
    } catch (error) {
      console.error("Failed to toggle like", error);
      // Revert if failed
      setIsLiked(prevLiked);
      setLikeCount(prevCount);
    }
  };


  // Ưu tiên lấy danh sách attachments từ API (database).
  // Nếu không có, fallback sang imageUrl (trường cũ) để hiển thị ảnh đơn.
  const attachments = (post.attachments && post.attachments.length > 0)
    ? post.attachments
    : (post.imageUrl
      ? [{ id: post.id || 'imageUrl', fileUrl: post.imageUrl, fileName: 'image' }]
      : []);

  // const likeCount = post.likeCount ?? 0; // Removed, using state
  const commentCount = post.commentCount ?? comments.length;


  const bgClass = post.content && post.content.includes('mountain') ? 'bg-blue-100' :
    post.content && post.content.includes('coffee') ? 'bg-amber-50' :
      'bg-white';

  const reactions = attachments.length > 0 ? ['🔥', '😍', '😱', '👍', '❤️'] : null;

  const extractTags = (content) => {
    if (!content) return [];
    const tagMatches = content.match(/@(\w+)/g);
    return tagMatches ? tagMatches.map(tag => tag.substring(1)) : [];
  };
  const tags = extractTags(post.content);


  const getTimeAgo = (dateString) => {
    if (!dateString) return '2 hours ago';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    if (diffHours < 1) return 'Just now';
    if (diffHours === 1) return '1 hour ago';
    return `${diffHours} hours ago`;
  };

  return (
    <article className={`${bgClass} rounded-3xl p-6 mb-6 transition-all duration-300`}>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <img
            src={resolveImageUrl(
              post.userAvatarUrl,
              'https://api.dicebear.com/7.x/avataaars/svg?seed=' + (post.username || 'User')
            )}
            alt={post.username || 'User'}
            className="w-12 h-12 rounded-full"
          />
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold">{post.username || 'User'}</span>
              {post.privacy && (
                <span className="text-xs text-gray-500">
                  {post.privacy === 'PUBLIC' ? '🌐' : '👥'}
                </span>
              )}
            </div>
            <div className="text-sm text-gray-500">{getTimeAgo(post.createdAt)}</div>
          </div>
        </div>
        <button className="p-2 hover:bg-white/50 rounded-full">
          <span className="text-xl">⋮</span>
        </button>
      </div>

      {post.content && (
        <p className="mb-4 leading-relaxed">
          {post.content}
          {tags.map((tag, i) => (
            <span key={i} className="text-blue-600 ml-1">@{tag}</span>
          ))}
          {post.content.includes('mountain') && '!'}
        </p>
      )}

      {post.location && (
        <div className="mb-4 flex items-center gap-2 text-gray-600">
          <span className="text-lg">📍</span>
          <span className="text-sm">{post.location}</span>
        </div>
      )}

      {attachments.length > 0 && buildGalleryLayout(attachments, reactions)}

      <div className="flex items-center gap-6 text-gray-600 border-t pt-3 mt-2">
        <button
          className={`flex items-center gap-2 transition-transform duration-200 ${likeAnimating ? 'scale-125' : 'scale-100'} ${isLiked ? 'text-red-500 font-bold' : 'hover:text-red-500'}`}
          onClick={handleToggleLike}
        >
          <span className="text-lg">{isLiked ? '❤️' : '🤍'}</span>
          <span>{likeCount > 0 ? likeCount : 'Like'}</span>
        </button>
        <button
          className="flex items-center gap-2 hover:text-blue-500"
          type="button"
          onClick={handleToggleComments}
        >
          <span className="text-lg">💬</span>
          <span>{comments.length > 0 ? comments.length : 'Comment'}</span>
        </button>
        {attachments.length > 0 && (
          <button className="ml-auto bg-gradient-to-r from-orange-400 to-pink-500 text-white px-4 py-2 rounded-full text-sm flex items-center gap-2 shadow-sm hover:shadow-md transition-shadow">
            🔥 Wooow!!!
          </button>
        )}
      </div>

      {showComments && (
        <div className="mt-4 border-t pt-4">
          <div className="flex items-start gap-2 mb-3">
            <input
              type="text"
              className="flex-1 rounded-full border px-3 py-1 text-sm bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-100 transition-all"
              placeholder="Write a comment..."
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleCreateComment();
                }
              }}
            />
            <button
              type="button"
              onClick={handleCreateComment}
              disabled={submittingComment || !newComment.trim()}
              className="text-sm px-4 py-1.5 rounded-full bg-blue-600 text-white disabled:bg-indigo-300 hover:bg-blue-700 transition-colors"
            >
              Send
            </button>
          </div>

          {loadingComments ? (
            <p className="mt-2 text-xs text-gray-500 animate-pulse">Loading comments...</p>
          ) : (
            <div className="mt-2 space-y-3">
              {comments.map((c) => (
                <CommentItem key={c.id} comment={c} onReply={handleReply} />
              ))}
              {comments.length === 0 && (
                <p className="mt-2 text-xs text-gray-400">No comments yet, be the first!</p>
              )}
            </div>
          )}
        </div>
      )}
    </article>
  );
};

export default PostCard;
