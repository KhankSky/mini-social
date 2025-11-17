import DefaultLayout from "../layouts/DefaultLayout.jsx";
import Feed from "../pages/Feed.jsx";
import Login from "../pages/Login.jsx";
import Register from "../pages/Register.jsx";

export const routes = [
  {
    path: "/",
    element: <DefaultLayout />,
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
];
