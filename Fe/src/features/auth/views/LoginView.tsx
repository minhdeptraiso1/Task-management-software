import { LoginPage, type LoginPageProps } from './LoginPage'

export type LoginViewProps = LoginPageProps

export function LoginView(props: LoginViewProps) {
  return <LoginPage {...props} />
}

