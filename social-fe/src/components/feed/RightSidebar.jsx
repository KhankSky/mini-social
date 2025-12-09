import React from 'react';

const RightSidebar = () => {
  const suggestions = [
    { name: 'Nick Shelburne', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Nick' },
    { name: 'Brittni Lando', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Brittni' },
    { name: 'Ivan Shevchenko', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Ivan' }
  ];

  return (
    <>
      <div className="mb-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-2xl font-bold">Suggestions</h2>
          <button className="text-sm text-gray-500 hover:text-black">See all</button>
        </div>
        <div className="space-y-3">
          {suggestions.map((user, i) => (
            <div key={i} className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <img src={user.avatar} alt={user.name} className="w-10 h-10 rounded-full" />
                <span className="font-semibold">{user.name}</span>
              </div>
              <button className="bg-black text-white px-4 py-1 rounded-full text-sm hover:bg-gray-800">
                Add Friends
              </button>
            </div>
          ))}
        </div>
      </div>
    </>
  );
};

export default RightSidebar;
