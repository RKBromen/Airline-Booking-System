import type { ReactNode } from 'react'

export function PageHeader({ title, description, actions, eyebrow = 'Quản lý máy bay' }: {
  title: string; description?: string; actions?: ReactNode; eyebrow?: string
}) {
  return <div className="management-page-header"><div><p className="eyebrow">{eyebrow}</p>
    <h1>{title}</h1>{description && <p className="muted">{description}</p>}</div>
    {actions && <div className="page-actions">{actions}</div>}
  </div>
}
