import { useEffect, useState } from 'react'
import { Button } from '../../../components/ui'
import { ProjectWorkspaceController } from '../../project/controllers/ProjectWorkspaceController'
import type { User } from '../models/user.model'
import { getMe } from '../services/user.service'
import { DashboardController } from './DashboardController'

export function RoleRouterController({ onLogout }: { onLogout: () => void }) {
  const [user, setUser] = useState<User | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getMe()
      .then(setUser)
      .catch(error => setError(error instanceof Error ? error.message : 'Không thể tải thông tin tài khoản'))
  }, [])

  if (error) {
    return <main className="grid min-h-screen place-items-center bg-canvas p-5">
      <section className="max-w-md rounded-2xl border border-line bg-white p-8 text-center">
        <h1 className="text-xl font-bold">Không thể mở workspace</h1>
        <p className="mt-2 text-sm text-muted">{error}</p>
        <Button className="mt-6" onClick={onLogout}>Về trang đăng nhập</Button>
      </section>
    </main>
  }

  if (!user) {
    return <div className="grid min-h-screen place-items-center bg-canvas">
      <div className="text-center">
        <span className="mx-auto block size-8 animate-spin rounded-full border-3 border-brand border-r-transparent" />
        <p className="mt-3 text-sm text-muted">Đang xác định quyền truy cập...</p>
      </div>
    </div>
  }

  return user.role === 'ADMIN'
    ? <DashboardController me={user} onLogout={onLogout} />
    : <ProjectWorkspaceController user={user} onLogout={onLogout} />
}
