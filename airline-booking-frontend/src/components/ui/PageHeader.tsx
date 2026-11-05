import type { ReactNode } from 'react'

export function PageHeader({ title, description, actions }: { title: string; description?: string; actions?: ReactNode }) {
  return <div className="management-page-header"><div><p className="eyebrow">Quản lý máy bay</p>
    <h1>{title}</h1>{description && <p className="muted">{description}</p>}</div>
    {actions && <div className="page-actions">{actions}</div>}
  </div>
}
