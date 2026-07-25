import { useContext, useEffect, useState } from 'react'
import { useDispatch } from 'react-redux'
import { Box, Button } from '@mui/material'
import { AuthContext } from 'react-oauth2-code-pkce'
import {BrowserRouter as Router, Navigate, Route,Routes,useLocation} from 'react-router'
import { setCredentials } from './store/authSlice';
import ActivityForm from './components/ActivityForm'
import ActivityDetail from './components/ActivityDetail'
import ActivityList from './components/ActivityList'

const ActivitiesPage = () =>{
  return (
  <Box component="section" sx={{ p: 2, border: '1px dashed grey' }}>
    
      <ActivityForm onActivitiesAdded = {() => {} }/>
          {/* window.location.reload() }/> */}
          {/* <ActivityForm onActivitiesAdded={() => window.location.reload()} /> */}
      <ActivityList />
    </Box>
  )
}
function App() {

  const {token,tokenData,logIn,logOut,isAuthenticated} = useContext(AuthContext);
  const dispatch = useDispatch();
  const [authReady,setAuthReady] = useState(false);

  
  useEffect(() => {
    if(token){
      dispatch(setCredentials({token,user: tokenData}));
      setAuthReady(true);
    }
  },[token,tokenData,dispatch]);

  const handleLogout = () => {
    dispatch(clearAuth())
    logOut()
  }


  return (
    <Router>
      {!token ? ( 
      <Button variant="contained"
                onClick={() => {
                  logIn();
                }}>login</Button>
                ) :(
                  // <div>
                  //   <pre>{JSON.stringify(tokenData,null,2)}</pre>
                  //    <pre>{JSON.stringify(token,null,2)}</pre>

                  // </div>

                  <Box component="section" sx={{ p: 2, border: '1px dashed grey' }}>
                    <Button variant="contained" color="secondary" onClick={() => {logOut();}}>Logout</Button>
                    <Routes>
                      <Route path="/activities" element={<ActivitiesPage />}/>
                      <Route path="/activities/:id" element={<ActivityDetail />}/>
                      <Route path="/" element={token ? <Navigate to="/activities" replace/> : <div>Welcome. Please login!</div>}/>

                    </Routes>
                  </Box>
                )}
    </Router>
  )
}

export default App
 //color="#dc004e"