import React from 'react';
import {Outlet} from "react-router-dom";
import Header from "./Header.jsx";

const DefaultLayout = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="container mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
};

export default DefaultLayout;