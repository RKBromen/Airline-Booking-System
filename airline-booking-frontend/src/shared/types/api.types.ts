export interface ApiError {
  status: number
  title: string
  detail: string
  type?: string
  instance?: string
  errorCode?: string
  fieldErrors?: Record<string, string[]>
}
