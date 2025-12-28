import './App.css'
import {routes} from "./constants/routes.jsx";
import {useRoutes} from "react-router-dom";

function App() {
  const element = useRoutes(routes);

  return (
    <>
      {element}
    </>
  );
}

export default App
