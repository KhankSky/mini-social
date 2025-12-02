import React, { useEffect, useState } from 'react';
import { FiThumbsUp, FiMessageCircle, FiShare2 } from 'react-icons/fi';
import { fetchComments, createComment } from '../../services/comment';
import { API_ORIGIN } from '../../config/HttpClient';

const resolveImageUrl = (url) => {
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/")) return `${API_ORIGIN}${url}`;
  return `${API_ORIGIN}/${url}`;
};

const buildGalleryLayout = (attachments) => {
  const images = attachments || [];
  const count = images.length;
  if (count === 0) return null;

  const renderImage = (img, extraClass = '') => (
    <img
      key={img.id || img.fileUrl}
      src={resolveImageUrl(img.fileUrl)}
      alt={img.fileName || 'image'}
      className={`w-full h-full object-cover ${extraClass}`}
    />
  );

  if (count === 1) {
    return (
      <div className="mt-3 rounded-lg overflow-hidden">
        {renderImage(images[0], 'max-h-[500px]')}
      </div>
    );
  }

  if (count === 2) {
    return (
      <div className="mt-3 grid grid-cols-2 gap-1 rounded-lg overflow-hidden max-h-[400px]">
        {images.map((img) => (
          <div key={img.id || img.fileUrl} className="relative">
            {renderImage(img)}
          </div>
        ))}
      </div>
    );
  }

  if (count === 3) {
    return (
      <div className="mt-3 grid grid-rows-2 gap-1 rounded-lg overflow-hidden max-h-[450px]">
        <div className="row-span-1">
          {renderImage(images[0])}
        </div>
        <div className="grid grid-cols-2 gap-1 row-span-1">
          {images.slice(1).map((img) => (
            <div key={img.id || img.fileUrl}>{renderImage(img)}</div>
          ))}
        </div>
      </div>
    );
  }

  const visibleImages = images.slice(0, 4);
  const remaining = count - 4;

  return (
    <div className="mt-3 grid grid-cols-2 gap-1 rounded-lg overflow-hidden max-h-[450px]">
      {visibleImages.map((img, index) => {
        const isLast = index === 3 && remaining > 0;
        return (
          <div key={img.id || img.fileUrl} className="relative">
            {renderImage(img)}
            {isLast && (
              <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                <span className="text-white font-semibold text-lg">+{remaining} xem thêm</span>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
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

  const attachments = post.attachments || [];
  const likeCount = post.likeCount ?? 0;
  const commentCount = post.commentCount ?? comments.length;

  return (
    <article className="bg-white rounded-lg shadow-sm p-4">
      <header className="flex items-start gap-3">
        <img
          src={resolveImageUrl(post.userAvatarUrl || '/uploads/default-avatar.png')}
          alt="avatar"
          className="w-10 h-10 rounded-full object-cover"
        />
        <div>
          <div className="flex items-center gap-2">
            <h3 className="font-medium text-gray-800">{post.username}</h3>
            {post.createdAt && (
              <span className="text-xs text-gray-500">
                · {new Date(post.createdAt).toLocaleString()}
              </span>
            )}
          </div>
        </div>
      </header>

      {post.content && <div className="mt-3 text-gray-800 text-sm whitespace-pre-line">{post.content}</div>}

      {attachments.length > 0 && buildGalleryLayout(attachments)}

      <footer className="mt-3">
        <div className="flex items-center justify-between text-gray-600 text-sm">
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <FiThumbsUp />
              <span>{likeCount}</span>
            </div>
            <button
              type="button"
              className="flex items-center gap-2"
              onClick={handleToggleComments}
            >
              <FiMessageCircle />
              <span>
                {commentCount} bình luận
              </span>
            </button>
          </div>
          <div className="flex items-center gap-4">
            <button className="flex items-center gap-2 text-gray-600 hover:text-blue-600">
              <FiThumbsUp />
              <span className="hidden sm:inline">Thích</span>
            </button>
            <button
              className="flex items-center gap-2 text-gray-600 hover:text-blue-600"
              type="button"
              onClick={handleToggleComments}
            >
              <FiMessageCircle />
              <span className="hidden sm:inline">Bình luận</span>
            </button>
            <button className="flex items-center gap-2 text-gray-600 hover:text-blue-600">
              <FiShare2 />
              <span className="hidden sm:inline">Chia sẻ</span>
            </button>
          </div>
        </div>

        {showComments && (
          <div className="mt-3 border-t pt-3">
            <div className="flex items-start gap-2">
              <input
                type="text"
                className="flex-1 rounded-full border px-3 py-1 text-sm"
                placeholder="Viết bình luận..."
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
              />
              <button
                type="button"
                onClick={handleCreateComment}
                disabled={submittingComment || !newComment.trim()}
                className="text-sm px-3 py-1 rounded-full bg-blue-600 text-white disabled:bg-blue-300"
              >
                Gửi
              </button>
            </div>

            {loadingComments ? (
              <p className="mt-2 text-xs text-gray-500">Đang tải bình luận...</p>
            ) : (
              <div className="mt-2">
                {comments.map((c) => (
                  <CommentItem key={c.id} comment={c} onReply={handleReply} />
                ))}
                {comments.length === 0 && (
                  <p className="mt-2 text-xs text-gray-400">Chưa có bình luận nào, hãy là người đầu tiên!</p>
                )}
              </div>
            )}
          </div>
        )}
      </footer>
    </article>
  );
};

export default PostCard;
