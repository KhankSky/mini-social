import './App.css'
import { routes } from "./constants/routes.jsx";
import { useRoutes } from "react-router-dom";
import { ToastProvider } from "./context/ToastContext";

function App() {
  const element = useRoutes(routes);

  return (
    <ToastProvider>
      {element}
    </ToastProvider>
  );
}

export default App
