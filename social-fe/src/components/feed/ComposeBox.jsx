import React, { useRef, useState } from 'react';
import { FiImage, FiFileText, FiVideo } from 'react-icons/fi';
import { createPost } from '../../services/post';
import { API_ORIGIN } from '../../config/HttpClient';

const resolveImageUrl = (url) => {
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/")) return `${API_ORIGIN}${url}`;
  return `${API_ORIGIN}/${url}`;
};

const ComposeBox = ({ user, onPostCreated }) => {
  const [content, setContent] = useState('');
  const [images, setImages] = useState([]);
  const [previewUrls, setPreviewUrls] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const fileInputRef = useRef(null);

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    const newImages = [...images, ...files];
    setImages(newImages);

    const newPreviews = [
      ...previewUrls,
      ...files.map((file) => ({
        url: URL.createObjectURL(file),
        name: file.name,
      })),
    ];
    setPreviewUrls(newPreviews);
  };

  const handleRemoveImage = (index) => {
    const newImages = images.filter((_, i) => i !== index);
    const newPreviews = previewUrls.filter((_, i) => i !== index);
    setImages(newImages);
    setPreviewUrls(newPreviews);
  };

  const handleSubmit = async () => {
    if (!content.trim() && images.length === 0) return;
    try {
      setSubmitting(true);
      const res = await createPost(content, images);
      setContent('');
      setImages([]);
      setPreviewUrls([]);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      if (onPostCreated) {
        onPostCreated(res.data);
      }
    } catch (err) {
      console.error('Failed to create post', err);
      // Có thể bổ sung toast thông báo lỗi ở đây
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm p-4">
      <div className="flex items-start gap-3">
        <img
          src={resolveImageUrl(user?.avatarUrl || '/uploads/default-avatar.png')}
          alt="you"
          className="w-10 h-10 rounded-full object-cover"
        />
        <textarea
          rows={3}
          className="flex-1 resize-none p-3 rounded-md bg-gray-50 border border-transparent focus:border-blue-300"
          placeholder="Hãy chia sẻ điều gì đó..."
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
      </div>

      {previewUrls.length > 0 && (
        <div className="mt-3 grid grid-cols-4 gap-2">
          {previewUrls.map((img, index) => (
            <div key={index} className="relative group">
              <img src={img.url} alt={img.name} className="w-full h-20 object-cover rounded" />
              <button
                type="button"
                onClick={() => handleRemoveImage(index)}
                className="absolute top-1 right-1 bg-black/60 text-white text-xs px-1 rounded opacity-0 group-hover:opacity-100 transition"
              >
                ×
              </button>
            </div>
          ))}
        </div>
      )}

      <div className="mt-3 flex items-center justify-between">
        <div className="flex items-center gap-4 text-gray-600">
          <button
            type="button"
            className="flex items-center gap-2 hover:text-blue-600"
            onClick={() => fileInputRef.current && fileInputRef.current.click()}
          >
            <FiImage />
            <span className="text-sm hidden sm:inline">Ảnh</span>
          </button>
          <button type="button" className="flex items-center gap-2 hover:text-blue-600">
            <FiVideo />
            <span className="text-sm hidden sm:inline">Video</span>
          </button>
          <button type="button" className="flex items-center gap-2 hover:text-blue-600">
            <FiFileText />
            <span className="text-sm hidden sm:inline">Bài viết dài</span>
          </button>
        </div>

        <div>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={submitting || (!content.trim() && images.length === 0)}
            className="bg-blue-600 disabled:bg-blue-300 text-white px-4 py-1 rounded-md"
          >
            {submitting ? 'Đang đăng...' : 'Đăng'}
          </button>
        </div>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        multiple
        className="hidden"
        onChange={handleImageChange}
      />
    </div>
  );
};

export default ComposeBox;
