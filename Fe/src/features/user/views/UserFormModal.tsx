import { useMemo, useState, type FormEvent } from 'react'
import { AtSign, CheckCircle2, KeyRound, Save, ShieldCheck, UserRound, UserPlus, X } from 'lucide-react'
import { Button, Input, Modal, Select } from '../../../components/ui'
import { roleDescriptions, roleLabels, type CreateUserData, type User, type UserRole } from '../models/user.model'

interface Props {
  open: boolean
  user: User | null
  roles: UserRole[]
  loading: boolean
  onClose: () => void
  onSave: (data: CreateUserData | { role: UserRole; enabled: boolean; password?: string }) => void
}

export function UserFormModal({ open, user, roles, loading, onClose, onSave }: Props) {
  const defaultRole = roles.includes('DEVELOPER') ? 'DEVELOPER' : roles[0]
  const [username, setUsername] = useState(user?.username ?? '')
  const [email, setEmail] = useState(user?.email ?? '')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<UserRole>(user?.role ?? defaultRole)
  const [enabled, setEnabled] = useState(String(user?.enabled ?? true))

  const roleOptions = useMemo(
    () => roles.map(role => ({ label: roleLabels[role] ?? role, value: role })),
    [roles],
  )
  const selectedRoleDescription = roleDescriptions[role] ?? 'Vai trò người dùng trong hệ thống.'

  const submit = (event: FormEvent) => {
    event.preventDefault()
    if (user) {
      onSave({ role, enabled: enabled === 'true', ...(password ? { password } : {}) })
      return
    }
    onSave({ username: username.trim(), email: email.trim(), password, role })
  }

  return <Modal
    open={open}
    onClose={onClose}
    title={user ? 'Chỉnh sửa tài khoản' : 'Tạo tài khoản mới'}
    description={user ? 'Cập nhật quyền truy cập, trạng thái hoặc đặt lại mật khẩu.' : 'Tạo tài khoản HiCAS One và gán đúng vai trò ngay từ đầu.'}
  >
    <form className="space-y-5" onSubmit={submit}>
      <section className="rounded-xl border border-line bg-canvas p-4">
        <div className="mb-4 flex items-start gap-3">
          <span className="grid size-10 shrink-0 place-items-center rounded-lg bg-[#fef3e2] text-brand">
            <UserRound size={20} />
          </span>
          <div>
            <h3 className="font-semibold text-ink">Thông tin đăng nhập</h3>
            <p className="mt-0.5 text-sm text-muted">Dùng để nhận diện người dùng khi đăng nhập hệ thống.</p>
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Tên đăng nhập"
            value={username}
            onChange={event => setUsername(event.target.value)}
            minLength={3}
            maxLength={100}
            required={!user}
            disabled={Boolean(user)}
            placeholder="developer01"
            leadingIcon={<UserRound size={17} />}
            hint={user ? 'Tên đăng nhập không đổi sau khi tạo.' : 'Từ 3 ký tự, viết liền không dấu để dễ quản trị.'}
          />
          <Input
            label="Email"
            type="email"
            value={email}
            onChange={event => setEmail(event.target.value)}
            required={!user}
            disabled={Boolean(user)}
            placeholder="name@hicas.vn"
            leadingIcon={<AtSign size={17} />}
            hint={user ? 'Email không đổi trong màn hình chỉnh sửa này.' : 'Dùng email công ty hoặc email làm việc chính.'}
          />
        </div>

        <Input
          className="mt-4"
          label={user ? 'Mật khẩu mới' : 'Mật khẩu'}
          type="password"
          value={password}
          onChange={event => setPassword(event.target.value)}
          minLength={6}
          required={!user}
          placeholder={user ? 'Để trống nếu không đổi mật khẩu' : 'Tối thiểu 6 ký tự'}
          leadingIcon={<KeyRound size={17} />}
          hint={user ? 'Chỉ nhập khi cần đặt lại mật khẩu cho tài khoản này.' : 'Nên dùng mật khẩu đủ mạnh trước khi bàn giao.'}
        />
      </section>

      <section className="rounded-xl border border-line bg-white p-4">
        <div className="mb-4 flex items-start gap-3">
          <span className="grid size-10 shrink-0 place-items-center rounded-lg bg-brand-black text-brand">
            <ShieldCheck size={20} />
          </span>
          <div>
            <h3 className="font-semibold text-ink">Quyền truy cập</h3>
            <p className="mt-0.5 text-sm text-muted">Chọn đúng role để tránh gọi nhầm API hoặc hiện sai chức năng.</p>
          </div>
        </div>

        <div className={user ? 'grid gap-4 sm:grid-cols-2' : 'grid gap-4'}>
          <Select
            label="Vai trò"
            value={role}
            onChange={event => setRole(event.target.value as UserRole)}
            required
            options={roleOptions}
          />
          {user && (
            <Select
              label="Trạng thái"
              value={enabled}
              onChange={event => setEnabled(event.target.value)}
              options={[
                { label: 'Đang hoạt động', value: 'true' },
                { label: 'Đã vô hiệu hóa', value: 'false' },
              ]}
            />
          )}
        </div>

        <div className="mt-4 rounded-lg border border-line bg-canvas p-3">
          <div className="flex items-start gap-2">
            <CheckCircle2 className="mt-0.5 text-success" size={17} />
            <div>
              <p className="text-sm font-semibold text-ink">{roleLabels[role] ?? role}</p>
              <p className="mt-0.5 text-sm leading-6 text-muted">{selectedRoleDescription}</p>
            </div>
          </div>
        </div>
      </section>

      <div className="flex flex-col-reverse gap-3 pt-1 sm:flex-row sm:justify-end">
        <Button type="button" variant="secondary" leadingIcon={<X size={17} />} onClick={onClose}>Hủy</Button>
        <Button type="submit" loading={loading} leadingIcon={user ? <Save size={17} /> : <UserPlus size={17} />}>
          {user ? 'Lưu thay đổi' : 'Tạo tài khoản'}
        </Button>
      </div>
    </form>
  </Modal>
}
