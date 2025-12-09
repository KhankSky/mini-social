import React, { useRef, useState, useEffect, useCallback } from 'react';
import { createPost } from '../../services/post';
import { searchLocations, getCurrentLocation } from '../../services/location';
import { API_ORIGIN } from '../../config/HttpClient';

const resolveImageUrl = (url) => {
  if (!url) return "https://api.dicebear.com/7.x/avataaars/svg?seed=Current";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/")) return `${API_ORIGIN}${url}`;
  return `${API_ORIGIN}/${url}`;
};

const ComposeBox = ({ user, onPostCreated }) => {
  const [content, setContent] = useState('');
  const [images, setImages] = useState([]);
  const [previewUrls, setPreviewUrls] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [privacy, setPrivacy] = useState('PUBLIC'); 
  const [location, setLocation] = useState('');
  const [locationInput, setLocationInput] = useState('');
  const [showLocationInput, setShowLocationInput] = useState(false);
  const [locationSuggestions, setLocationSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [loadingLocations, setLoadingLocations] = useState(false);
  const fileInputRef = useRef(null);
  const locationInputRef = useRef(null);
  const suggestionsRef = useRef(null);

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

  // Tìm kiếm địa điểm khi người dùng nhập
  const searchLocationSuggestions = useCallback(async (query) => {
    if (!query || query.trim().length < 2) {
      setLocationSuggestions([]);
      setShowSuggestions(false);
      return;
    }

    setLoadingLocations(true);
    try {
      const results = await searchLocations(query);
      setLocationSuggestions(results);
      setShowSuggestions(results.length > 0);
    } catch (error) {
      console.error('Error searching locations:', error);
      setLocationSuggestions([]);
      setShowSuggestions(false);
    } finally {
      setLoadingLocations(false);
    }
  }, []);

  // Debounce timer ref
  const debounceTimerRef = useRef(null);

  // Debounced search function
  const debouncedSearch = useCallback((query) => {
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }
    debounceTimerRef.current = setTimeout(() => {
      searchLocationSuggestions(query);
    }, 500);
  }, [searchLocationSuggestions]);

  // Xử lý khi người dùng nhập địa điểm
  const handleLocationInputChange = (e) => {
    const value = e.target.value;
    setLocationInput(value);
    debouncedSearch(value);
  };

  // Chọn địa điểm từ suggestions
  const handleSelectLocation = (suggestion) => {
    setLocation(suggestion.displayName);
    setLocationInput(suggestion.displayName);
    setShowSuggestions(false);
    setLocationSuggestions([]);
  };

  // Lấy vị trí hiện tại
  const handleGetCurrentLocation = async () => {
    try {
      const currentLocation = await getCurrentLocation();
      setLocation(currentLocation.displayName);
      setLocationInput(currentLocation.displayName);
      setShowSuggestions(false);
    } catch (error) {
      console.error('Error getting current location:', error);
      alert('Không thể lấy vị trí hiện tại. Vui lòng nhập thủ công.');
    }
  };

  // Đóng suggestions khi click bên ngoài
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        suggestionsRef.current &&
        !suggestionsRef.current.contains(event.target) &&
        locationInputRef.current &&
        !locationInputRef.current.contains(event.target)
      ) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      // Cleanup debounce timer
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, []);

  const handleSubmit = async () => {
    if (!content.trim() && images.length === 0) return;
    try {
      setSubmitting(true);
      const res = await createPost(content, images, privacy, location);
      setContent('');
      setImages([]);
      setPreviewUrls([]);
      setLocation('');
      setLocationInput('');
      setShowLocationInput(false);
      setShowSuggestions(false);
      setLocationSuggestions([]);
      setPrivacy('PUBLIC');
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      if (onPostCreated) {
        onPostCreated(res.data);
      }
    } catch (err) {
      console.error('Failed to create post', err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="bg-white rounded-3xl p-6 mb-6">
      <div className="flex items-center gap-3 mb-4">
        <img 
          src={resolveImageUrl(user?.avatarUrl || '/uploads/default-avatar.png')} 
          alt="You" 
          className="w-10 h-10 rounded-full" 
        />
        <input
          type="text"
          placeholder="Share something"
          className="flex-1 bg-transparent outline-none"
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <button className="p-2 hover:bg-gray-100 rounded-full">
          <span className="text-xl">😊</span>
        </button>
      </div>

      {previewUrls.length > 0 && (
        <div className="mb-4 grid grid-cols-3 gap-2">
          {previewUrls.map((img, index) => (
            <div key={index} className="relative group">
              <img src={img.url} alt={img.name} className="w-full h-32 object-cover rounded-2xl" />
              <button
                type="button"
                onClick={() => handleRemoveImage(index)}
                className="absolute top-2 right-2 bg-black/60 text-white text-xs px-2 py-1 rounded-full opacity-0 group-hover:opacity-100 transition"
              >
                ×
              </button>
            </div>
          ))}
        </div>
      )}

      {showLocationInput && (
        <div className="mb-4 relative">
          <div className="flex gap-2">
            <div className="flex-1 relative">
              <input
                ref={locationInputRef}
                type="text"
                placeholder="Tìm kiếm địa điểm..."
                className="w-full px-4 py-2 border border-gray-300 rounded-full outline-none focus:border-black"
                value={locationInput}
                onChange={handleLocationInputChange}
                onFocus={() => {
                  if (locationSuggestions.length > 0) {
                    setShowSuggestions(true);
                  }
                }}
              />
              
              {/* Dropdown suggestions */}
              {showSuggestions && locationSuggestions.length > 0 && (
                <div
                  ref={suggestionsRef}
                  className="absolute z-50 w-full mt-2 bg-white border border-gray-200 rounded-2xl shadow-lg max-h-60 overflow-y-auto"
                >
                  {loadingLocations && (
                    <div className="px-4 py-2 text-sm text-gray-500 text-center">
                      Đang tìm kiếm...
                    </div>
                  )}
                  {locationSuggestions.map((suggestion) => (
                    <button
                      key={suggestion.id}
                      type="button"
                      onClick={() => handleSelectLocation(suggestion)}
                      className="w-full px-4 py-3 text-left hover:bg-gray-100 transition-colors border-b border-gray-100 last:border-b-0"
                    >
                      <div className="flex items-start gap-2">
                        <span className="text-lg mt-0.5">📍</span>
                        <div className="flex-1">
                          <div className="font-semibold text-sm text-gray-900">
                            {suggestion.shortName}
                          </div>
                          <div className="text-xs text-gray-500 mt-0.5 line-clamp-1">
                            {suggestion.displayName}
                          </div>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
            
            {/* Nút lấy vị trí hiện tại */}
            <button
              type="button"
              onClick={handleGetCurrentLocation}
              className="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-full text-sm font-medium transition-colors"
              title="Lấy vị trí hiện tại"
            >
              📍 Vị trí
            </button>
          </div>
          
          {/* Hiển thị địa điểm đã chọn */}
          {location && (
            <div className="mt-2 flex items-center gap-2 text-sm text-gray-600">
              <span>📍</span>
              <span className="flex-1">{location}</span>
              <button
                type="button"
                onClick={() => {
                  setLocation('');
                  setLocationInput('');
                  setShowSuggestions(false);
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                ×
              </button>
            </div>
          )}
        </div>
      )}

      <div className="flex items-center justify-between">
        <div className="flex gap-4">
          <button 
            type="button"
            className="flex items-center gap-2 text-gray-600 hover:text-black"
            onClick={() => fileInputRef.current && fileInputRef.current.click()}
          >
            <span className="text-xl">📎</span>
            <span>File</span>
          </button>
          <button 
            type="button"
            className="flex items-center gap-2 text-gray-600 hover:text-black"
            onClick={() => fileInputRef.current && fileInputRef.current.click()}
          >
            <span className="text-xl">📷</span>
            <span>Image</span>
          </button>
          <button 
            type="button"
            onClick={() => setShowLocationInput(!showLocationInput)}
            className={`flex items-center gap-2 ${showLocationInput ? 'text-black' : 'text-gray-600 hover:text-black'}`}
          >
            <span className="text-xl">📍</span>
            <span>Location</span>
          </button>
          <button 
            type="button"
            onClick={() => setPrivacy(privacy === 'PUBLIC' ? 'FRIENDS' : 'PUBLIC')}
            className={`flex items-center gap-2 ${privacy === 'PUBLIC' ? 'text-black' : 'text-gray-600 hover:text-black'}`}
          >
            <span className="text-xl">👥</span>
            <span>{privacy === 'PUBLIC' ? 'Public' : 'Friends'}</span>
          </button>
        </div>
        <button 
          type="button"
          onClick={handleSubmit}
          disabled={submitting || (!content.trim() && images.length === 0)}
          className="bg-black text-white px-8 py-2 rounded-full hover:bg-gray-800 disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          {submitting ? 'Sending...' : 'Send'}
        </button>
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
