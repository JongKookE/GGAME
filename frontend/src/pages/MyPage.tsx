// import Navbar from "../components/commonUseComponents/Navbar";
import LikeComponent from "../components/MyPageComponents/Like";
import Profile from "../components/MyPageComponents/Profile"
import Statistics from '../components/MyPageComponents/Statistics'

  function MyPage() {
    return (
        // <><Navbar /><Profile userId={userId}/><h1>MyPage</h1></>
        <>
          {/* <Navbar /> */}
          <Profile />
          <LikeComponent />
          <Statistics />
          <h1>YyPage</h1>
        </>
    )
  }
  export default MyPage;