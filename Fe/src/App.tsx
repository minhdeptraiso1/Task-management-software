import { useEffect, useState } from 'react'
import { LoginController } from './features/auth/controllers/LoginController'
import { RoleRouterController } from './features/user/controllers/RoleRouterController'
import { logout } from './features/auth/services/auth.service'
import { tokenStore } from './services/apiClient'

function App() {
  const [authenticated, setAuthenticated] = useState(Boolean(tokenStore.access()))
  useEffect(() => { const expired = () => setAuthenticated(false); window.addEventListener('auth:expired', expired); return () => window.removeEventListener('auth:expired', expired) }, [])
  const handleLogout = async () => { await logout(); setAuthenticated(false) }
  return authenticated ? <RoleRouterController onLogout={handleLogout} /> : <LoginController onAuthenticated={() => setAuthenticated(true)} />
}

export default App
