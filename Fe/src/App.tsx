import { useEffect, useState } from 'react'
import { LoginController } from './features/auth/controllers/LoginController'
import { RoleRouterController } from './features/user/controllers/RoleRouterController'
import { logout } from './features/auth/services/auth.service'
import { isAuthSyncStorageKey, tokenStore } from './services/apiClient'
import { ToastContainer, toast } from './components/ui'

// Global override for native window.alert to ensure zero browser alert popups system-wide
if (typeof window !== 'undefined') {
  window.alert = (msg?: any) => {
    if (msg) {
      toast.warning(String(msg))
    }
  }
}

function App() {
  const [authenticated, setAuthenticated] = useState(Boolean(tokenStore.access() || tokenStore.refresh()))

  useEffect(() => {
    const expireCurrentTab = () => setAuthenticated(false)
    const expireSyncedTab = (event: StorageEvent) => {
      if (!isAuthSyncStorageKey(event.key)) return
      tokenStore.clear()
      setAuthenticated(false)
    }

    window.addEventListener('auth:expired', expireCurrentTab)
    window.addEventListener('storage', expireSyncedTab)

    return () => {
      window.removeEventListener('auth:expired', expireCurrentTab)
      window.removeEventListener('storage', expireSyncedTab)
    }
  }, [])

  const handleLogout = async () => { await logout(); setAuthenticated(false) }
  return (
    <>
      <ToastContainer />
      {authenticated ? <RoleRouterController onLogout={handleLogout} /> : <LoginController onAuthenticated={() => setAuthenticated(true)} />}
    </>
  )
}

export default App
