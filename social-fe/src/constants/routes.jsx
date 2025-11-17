import DefaultLayout from "../layouts/DefaultLayout.jsx";
import Feed from "../pages/Feed.jsx";

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
];
