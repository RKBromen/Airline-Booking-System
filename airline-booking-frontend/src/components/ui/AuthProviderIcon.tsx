import type { AuthProvider } from '../../features/auth/types/auth.enums'

export function AuthProviderIcon({ provider }: { provider: AuthProvider }) {
  if (provider === 'GOOGLE') return <svg className="provider-mark" viewBox="0 0 20 20" aria-hidden="true">
    <path fill="#4285F4" d="M19.6 10.23c0-.68-.06-1.36-.18-2.01H10v3.8h5.38a4.6 4.6 0 0 1-2 3.02v2.47h3.25c1.9-1.75 2.97-4.33 2.97-7.28z" />
    <path fill="#34A853" d="M10 20c2.7 0 4.97-.9 6.63-2.49l-3.25-2.47c-.9.6-2.04.96-3.38.96-2.6 0-4.8-1.75-5.59-4.1H1.06v2.55A10 10 0 0 0 10 20z" />
    <path fill="#FBBC05" d="M4.41 11.9a6 6 0 0 1 0-3.8V5.55H1.06a10 10 0 0 0 0 8.9z" />
    <path fill="#EA4335" d="M10 3.98c1.47 0 2.8.5 3.84 1.5l2.88-2.88C14.97.99 12.7 0 10 0a10 10 0 0 0-8.94 5.55L4.41 8.1C5.2 5.73 7.4 3.98 10 3.98z" />
  </svg>
  return <svg className="provider-mark" viewBox="0 0 20 20" aria-hidden="true">
    <rect width="20" height="20" rx="4" fill="#1877F2" />
    <path fill="#fff" d="M11.5 17v-6h2l.3-2.3h-2.3V7.2c0-.7.2-1.1 1.2-1.1h1.2V4.1c-.2 0-.9-.1-1.8-.1-1.8 0-3.1 1.1-3.1 3.2v1.5H7v2.3h2V17z" />
  </svg>
}
