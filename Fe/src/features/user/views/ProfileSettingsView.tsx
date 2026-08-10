import { useState } from 'react'
import { AlertCircle, ArrowLeft, KeyRound, LogOut, BookOpen } from 'lucide-react'
import { Button, Input, ConfirmDialog } from '../../../components/ui'
import { changePassword, logoutAll } from '../../auth/services/auth.service'
import type { User } from '../models/user.model'
import { UserGuideModal } from './UserGuideModal'

interface ProfileSettingsViewProps {
  user: User
  onBack: () => void
  onSuccessLogoutAll: () => void
  onOpenGuide?: () => void
}

export function ProfileSettingsView({ user, onBack, onSuccessLogoutAll, onOpenGuide }: ProfileSettingsViewProps) {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false)
  const [showGuide, setShowGuide] = useState(false)

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault()
    if (newPassword !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp')
      return
    }
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      await changePassword({ currentPassword, newPassword, confirmPassword })
      setSuccess('Đổi mật khẩu thành công')
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi khi đổi mật khẩu')
    } finally {
      setLoading(false)
    }
  }

  const handleLogoutAll = async () => {
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      await logoutAll()
      onSuccessLogoutAll() // triggers redirect to login via controller
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi khi đăng xuất')
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-canvas">
      <header className="flex h-14 items-center gap-4 bg-brand-black px-5 text-white shadow-sm md:px-6">
        <Button
          as="button"
          variant="ghost"
          iconOnly
          aria-label="Quay lại"
          title="Quay lại"
          onClick={onBack}
          className="!text-white hover:!bg-white/20"
          size="sm"
        >
          <ArrowLeft size={18} />
        </Button>
        <h1 className="text-lg font-semibold tracking-wide">Đặt cài đặt cá nhân</h1>
      </header>

      <main className="mx-auto max-w-2xl p-6 md:p-10">
        <div className="rounded-2xl border border-line bg-white p-6 shadow-sm sm:p-8">
          <div className="mb-8 flex items-center gap-4">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-brand text-2xl font-bold text-white uppercase">
              {user.username.charAt(0)}
            </div>
            <div>
              <h2 className="text-xl font-bold">{user.username}</h2>
              <p className="text-sm text-muted">{user.email}</p>
            </div>
          </div>

          <div className="space-y-8">
            {error && (
              <div className="flex items-center gap-2 rounded-lg bg-danger/10 p-3 text-sm text-danger">
                <AlertCircle size={16} />
                <p>{error}</p>
              </div>
            )}
            {success && (
              <div className="flex items-center gap-2 rounded-lg bg-success/10 p-3 text-sm text-success border border-success/20">
                <KeyRound size={16} />
                <p>{success}</p>
              </div>
            )}

            <form onSubmit={handleChangePassword} className="space-y-4">
              <h4 className="font-semibold border-b border-line pb-2">Đổi mật khẩu</h4>
              <Input
                type="password"
                label="Mật khẩu hiện tại"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                required
              />
              <Input
                type="password"
                label="Mật khẩu mới"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
                minLength={6}
              />
              <Input
                type="password"
                label="Xác nhận mật khẩu mới"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
              <div className="flex justify-end pt-2">
                <Button type="submit" variant="primary" loading={loading} disabled={loading || !currentPassword || !newPassword || !confirmPassword}>
                  Cập nhật mật khẩu
                </Button>
              </div>
            </form>

            <div className="space-y-4 pt-4 border-t border-line">
              <h4 className="font-semibold border-b border-line pb-2">
                Trợ giúp & Hướng dẫn sử dụng
              </h4>
              <p className="text-sm text-muted">
                Xem quy trình vận hành dự án từ lúc khởi tạo đến khi đóng Sprint (A → Z), phân quyền từng vai trò và chi tiết các tính năng hệ thống.
              </p>
              <Button
                type="button"
                variant="secondary"
                className="w-full justify-center"
                leadingIcon={<BookOpen size={16} />}
                onClick={() => onOpenGuide ? onOpenGuide() : setShowGuide(true)}
              >
                Xem Hướng dẫn sử dụng dự án (A → Z)
              </Button>
            </div>

            <div className="space-y-4 pt-4">
              <h4 className="font-semibold border-b border-line pb-2">Bảo mật</h4>
              <p className="text-sm text-body">
                Đăng xuất tài khoản khỏi tất cả các thiết bị và trình duyệt khác. Bạn sẽ phải đăng nhập lại trên các thiết bị đó.
              </p>
              <Button
                type="button"
                variant="danger"
                className="w-full justify-center"
                leadingIcon={<LogOut size={16} />}
                onClick={() => setShowLogoutConfirm(true)}
              >
                Đăng xuất khỏi tất cả thiết bị
              </Button>
            </div>
          </div>
        </div>
      </main>

      <UserGuideModal
        open={showGuide}
        onClose={() => setShowGuide(false)}
      />

      <ConfirmDialog
        open={showLogoutConfirm}
        onCancel={() => setShowLogoutConfirm(false)}
        title="Xác nhận đăng xuất"
        description="Bạn có chắc chắn muốn đăng xuất khỏi tất cả thiết bị không? Thao tác này sẽ yêu cầu bạn đăng nhập lại ở mọi nơi."
        confirmLabel="Đăng xuất ngay"
        onConfirm={handleLogoutAll}
      />
    </div>
  )
}
