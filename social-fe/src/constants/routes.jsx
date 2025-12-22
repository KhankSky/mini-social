import DefaultLayout from "../layouts/DefaultLayout.jsx";
import Feed from "../pages/Feed.jsx";
import Friends from "../pages/Friends.jsx";
import Login from "../pages/Login.jsx";
import Register from "../pages/Register.jsx";
import AdminLayout from "../layouts/AdminLayout.jsx";
import Users from "../pages/admin/Users.jsx";
import ProtectedRoute from "../components/ProtectedRoute.jsx";
import Messages from "../pages/Messages.jsx";
import Messages from "../pages/Messages.jsx";
import CallWindowPage from "../pages/CallWindowPage.jsx";
import SettingsPage from "../pages/SettingsPage.jsx";

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
      {
        path: "messages",
        element: <Messages />,
      },
      {
        path: "friends",
        element: <Friends />,
      },
      {
        path: "settings",
        element: <SettingsPage />,
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
    path: "/call-window",
    element: <CallWindowPage />,
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

