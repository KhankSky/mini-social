import DefaultLayout from "../layouts/DefaultLayout.jsx";
import Feed from "../pages/Feed.jsx";
import Login from "../pages/Login.jsx";
import Register from "../pages/Register.jsx";
import AdminLayout from "../layouts/AdminLayout.jsx";
import Users from "../pages/admin/Users.jsx";
import ProtectedRoute from "../components/ProtectedRoute.jsx";

export const routes = [
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <DefaultLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <Feed />,
      },
    ],
  },
  {
    path: "/login",
    element: <Login />,
  },
  {
    path: "/register",
    element: <Register />,
  },
  {
    path: "/admin",
    element: (
      <ProtectedRoute>
        <AdminLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <div>Admin Home</div> },
      { path: "users", element: <Users /> },
    ],
  },
];

