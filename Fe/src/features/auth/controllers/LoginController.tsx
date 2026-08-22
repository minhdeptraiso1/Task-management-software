import { useState } from 'react'
import { LoginView } from '../views/LoginView'
import { login } from '../services/auth.service'

export function LoginController({
  onAuthenticated,
  isLoaded = true,
}: {
  onAuthenticated: () => void
  isLoaded?: boolean
}) {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const handleSubmit = async (email: string, password: string) => {
    setLoading(true); setError('')
    try { await login({ email, password }); onAuthenticated() }
    catch (error) { setError(error instanceof Error ? error.message : 'Đăng nhập thất bại') }
    finally { setLoading(false) }
  }
  return <LoginView loading={loading} error={error} onSubmit={handleSubmit} isLoaded={isLoaded} />
}
