import { test, expect } from '@playwright/test'

const id = '11111111-1111-4111-8111-111111111111'
const root = '/management/aircrafts'
const detail = `${root}/${id}`
const initial = { id, registrationNo: 'VN-A321', status: 'ACTIVE' }
const session = role => ({ accessToken: 'mock-token', tokenType: 'Bearer', expiresIn: 3600,
  userId: 'user', email: 'staff@example.com', roles: [role] })
const respond = (route, json, status = 200) => route.fulfill({ status, json })

async function setup(page, role = 'ADMIN') {
  const state = { aircraft: { ...initial }, list: [{ ...initial }], seats: [
    { id: 'seat-1', seatNumber: '1A', seatClass: 'BUSINESS' },
    { id: 'seat-2', seatNumber: '12B', seatClass: 'ECONOMY' },
  ], calls: [], failure: 0, writes: 0, writeError: 0, fieldErrors: undefined }
  await page.route('**/api/v1/auth/refresh-token', route => role ? respond(route, session(role)) : respond(route, {}, 401))
  await page.route('**/api/v1/aircrafts**', async route => {
    const request = route.request()
    const url = new URL(request.url())
    state.calls.push({ method: request.method(), url: url.pathname, params: url.search })
    if (state.failure) return respond(route, { message: 'Service unavailable' }, state.failure)
    if (request.method() === 'POST' || request.method() === 'PUT') {
      state.writes++
      if (state.writeError) return respond(route, { message: 'Số đăng ký đã tồn tại.', fieldErrors: state.fieldErrors }, state.writeError)
      state.aircraft = { ...state.aircraft, ...request.postDataJSON() }
      state.list = [state.aircraft]
      return respond(route, state.aircraft, request.method() === 'POST' ? 201 : 200)
    }
    if (url.pathname.endsWith('/seats')) return respond(route, state.seats)
    if (url.pathname.endsWith(id)) return respond(route, state.aircraft)
    return respond(route, state.list)
  })
  return state
}
const noOverflow = async page => expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)

for (const role of ['STAFF', 'ADMIN']) {
  test(`${role} lists, filters and paginates aircrafts with role-specific actions`, async ({ page }, testInfo) => {
    const state = await setup(page, role)
    state.list = [initial, ...Array.from({ length: 11 }, (_, i) => ({ ...initial,
      id: `22222222-2222-4222-8222-${String(i).padStart(12, '0')}`, registrationNo: `VN-B${i + 1}` }))]
    await page.goto(root)
    await expect(page.getByRole('heading', { name: 'Danh sách máy bay' })).toBeVisible()
    await expect(page.getByRole('link', { name: 'Thêm máy bay', exact: true })).toHaveCount(role === 'ADMIN' ? 1 : 0)
    await page.locator('.ant-pagination-item-2').click()
    await expect(page.getByRole('link', { name: 'VN-B11', exact: true })).toBeVisible()
    await page.getByRole('searchbox', { name: 'Tìm máy bay' }).fill('a321')
    await expect(page.getByRole('link', { name: 'VN-A321', exact: true })).toBeVisible()
    expect(state.calls.every(call => call.params === '')).toBe(true)
    await noOverflow(page)
    await page.screenshot({ path: testInfo.outputPath('aircraft-list.png'), fullPage: true })
    await page.getByRole('link', { name: 'VN-A321', exact: true }).click()
    await expect(page).toHaveURL(detail)
  })
}

test('CUSTOMER is forbidden without requesting aircraft data', async ({ page }) => {
  const state = await setup(page, 'CUSTOMER')
  await page.goto(root)
  await expect(page).toHaveURL('/forbidden')
  await expect(page.getByText('Bạn không có quyền truy cập', { exact: true })).toBeVisible()
  expect(state.calls).toHaveLength(0)
})

test('guest redirects to login; login resumes the requested management route', async ({ page }) => {
  const state = await setup(page, null)
  await page.route('**/api/v1/auth/login', route => respond(route, session('STAFF')))
  await page.goto(detail)
  await expect(page).toHaveURL('/login')
  expect(state.calls).toHaveLength(0)
  await page.getByLabel('Email', { exact: true }).fill('staff@example.com')
  await page.getByLabel('Mật khẩu', { exact: true }).fill('password123')
  await page.getByRole('button', { name: 'Đăng nhập', exact: true }).click()
  await expect(page).toHaveURL(detail)
  await expect(page.getByRole('heading', { name: 'VN-A321', exact: true })).toBeVisible()
})

test('list empty and error/retry states', async ({ page }) => {
  const state = await setup(page)
  state.list = []
  await page.goto(root)
  await expect(page.getByText('Không có máy bay phù hợp.')).toBeVisible()
  state.failure = 500
  await page.reload()
  await expect(page.getByRole('alert')).toContainText('Service unavailable', { timeout: 15000 })
  state.failure = 0
  state.list = [initial]
  await page.getByRole('button', { name: 'Thử lại' }).click()
  await expect(page.getByRole('link', { name: 'VN-A321', exact: true })).toBeVisible()
})

test('direct detail reload restores session; STAFF cannot see edit', async ({ page }, testInfo) => {
  await setup(page, 'STAFF')
  await page.goto(detail)
  await expect(page.getByRole('heading', { name: 'VN-A321', exact: true })).toBeVisible()
  await expect(page.getByRole('link', { name: 'Chỉnh sửa máy bay' })).toHaveCount(0)
  await page.reload()
  await expect(page.getByRole('heading', { name: 'VN-A321', exact: true })).toBeVisible()
  await noOverflow(page)
  await page.screenshot({ path: testInfo.outputPath('aircraft-detail.png'), fullPage: true })
  await page.getByRole('link', { name: 'Xem ghế vật lý' }).click()
  await expect(page).toHaveURL(`${detail}/seats`)
})

test('unknown aircraft and malformed ID show 404 without fetching malformed ID', async ({ page }) => {
  const state = await setup(page)
  state.failure = 404
  await page.goto(detail)
  await expect(page.getByText('Không tìm thấy máy bay', { exact: true })).toBeVisible()
  state.calls = []
  await page.goto(`${root}/invalid`)
  await expect(page.getByText('Không tìm thấy máy bay', { exact: true })).toBeVisible()
  expect(state.calls).toHaveLength(0)
})

test('physical seats show supplied numbers/classes and total; empty seats are supported', async ({ page }, testInfo) => {
  const state = await setup(page, 'STAFF')
  await page.goto(`${detail}/seats`)
  await expect(page.getByText('Danh sách ghế vật lý (2)')).toBeVisible()
  await expect(page.getByRole('cell', { name: '1A', exact: true })).toBeVisible()
  await expect(page.getByRole('cell', { name: 'Thương gia', exact: true })).toBeVisible()
  await noOverflow(page)
  await page.screenshot({ path: testInfo.outputPath('aircraft-seats.png'), fullPage: true })
  state.seats = []
  await page.reload()
  await expect(page.getByText('Máy bay chưa có ghế vật lý.')).toBeVisible()
  await page.getByRole('link', { name: 'Quay lại chi tiết máy bay' }).click()
  await expect(page).toHaveURL(detail)
})

for (const path of [`${root}/new`, `${detail}/edit`]) {
  test(`STAFF cannot access ${path}`, async ({ page }) => {
    const state = await setup(page, 'STAFF')
    await page.goto(path)
    await expect(page).toHaveURL('/forbidden')
    expect(state.calls).toHaveLength(0)
  })
}

test('ADMIN create validates, blocks repeated submit and invalidates list', async ({ page }, testInfo) => {
  const state = await setup(page)
  await page.goto(root)
  await page.getByRole('link', { name: 'Thêm máy bay', exact: true }).click()
  const submit = page.locator('form button[type="submit"]')
  await submit.click()
  await expect(page.getByText('Vui lòng nhập số đăng ký máy bay.')).toBeVisible()
  expect(state.writes).toBe(0)
  await page.getByLabel('Số đăng ký máy bay', { exact: false }).fill('VN-A999')
  await noOverflow(page)
  await page.screenshot({ path: testInfo.outputPath('aircraft-create.png'), fullPage: true })
  let release
  const pending = new Promise(resolve => { release = resolve })
  let posts = 0
  await page.route('**/api/v1/aircrafts', async route => {
    if (route.request().method() !== 'POST') return route.fallback()
    posts++
    expect(route.request().postDataJSON()).toEqual({ registrationNo: 'VN-A999' })
    await pending
    state.aircraft = { ...initial, registrationNo: 'VN-A999' }
    state.list = [state.aircraft]
    return respond(route, state.aircraft, 201)
  })
  await submit.click()
  await expect(submit).toBeDisabled()
  await page.locator('form').dispatchEvent('submit')
  await expect.poll(() => posts).toBe(1)
  release()
  await expect(page).toHaveURL(detail)
  await expect(page.getByRole('heading', { name: 'VN-A999', exact: true })).toBeVisible()
  await page.getByRole('link', { name: 'Quay lại danh sách máy bay' }).click()
  await expect(page.getByRole('link', { name: 'VN-A999', exact: true })).toBeVisible()
})

test('duplicate registration and backend field validation stay in form', async ({ page }) => {
  const state = await setup(page)
  state.writeError = 409
  await page.goto(`${root}/new`)
  await page.getByLabel('Số đăng ký máy bay', { exact: false }).fill('VN-A321')
  await page.getByRole('button', { name: 'Thêm máy bay', exact: true }).click()
  await expect(page.getByRole('alert')).toContainText('Số đăng ký đã tồn tại.')
  await expect(page).toHaveURL(`${root}/new`)
  state.writeError = 422
  state.fieldErrors = { registrationNo: ['Số đăng ký không hợp lệ.'] }
  await page.getByRole('button', { name: 'Thêm máy bay', exact: true }).click()
  await expect(page.getByText('Số đăng ký không hợp lệ.', { exact: true })).toBeVisible()
})

test('ADMIN edit preloads values, uses PUT and preserves status; list sees new registration', async ({ page }, testInfo) => {
  const state = await setup(page)
  state.aircraft.status = 'BLOCKED'
  await page.goto(detail)
  await page.getByRole('link', { name: 'Chỉnh sửa máy bay' }).click()
  const field = page.getByLabel('Số đăng ký máy bay', { exact: false })
  await expect(field).toHaveValue('VN-A321')
  await field.fill('VN-A322')
  await noOverflow(page)
  await page.screenshot({ path: testInfo.outputPath('aircraft-edit.png'), fullPage: true })
  await page.getByRole('button', { name: 'Lưu thay đổi' }).click()
  await expect(page).toHaveURL(detail)
  await expect(page.getByRole('heading', { name: 'VN-A322', exact: true })).toBeVisible()
  await expect(page.getByText('Tạm ngưng', { exact: true })).toBeVisible()
  expect(state.calls.some(call => call.method === 'PUT' && call.url.endsWith(id))).toBe(true)
  await page.getByRole('link', { name: 'Quay lại danh sách máy bay' }).click()
  await expect(page.getByRole('link', { name: 'VN-A322', exact: true })).toBeVisible()
})

test('cancel create and edit does not write', async ({ page }) => {
  const state = await setup(page)
  await page.goto(`${root}/new`)
  await page.getByRole('button', { name: 'Hủy', exact: true }).click()
  await expect(page).toHaveURL(root)
  await page.goto(`${detail}/edit`)
  await page.getByRole('button', { name: 'Hủy', exact: true }).click()
  await expect(page).toHaveURL(detail)
  expect(state.writes).toBe(0)
})

test('detail errors and seat errors can be retried', async ({ page }) => {
  const state = await setup(page)
  state.failure = 500
  await page.goto(detail)
  await expect(page.getByRole('button', { name: 'Thử lại' })).toBeVisible({ timeout: 15000 })
  state.failure = 0
  await page.getByRole('button', { name: 'Thử lại' }).click()
  await expect(page.getByRole('heading', { name: 'VN-A321', exact: true })).toBeVisible()
  state.failure = 500
  await page.getByRole('link', { name: 'Xem ghế vật lý' }).click()
  await expect(page.getByRole('button', { name: 'Thử lại' })).toBeVisible({ timeout: 15000 })
  state.failure = 0
  await page.getByRole('button', { name: 'Thử lại' }).click()
  await expect(page.getByText('Danh sách ghế vật lý (2)')).toBeVisible()
})

test('backend 403 sends user to forbidden', async ({ page }) => {
  const state = await setup(page)
  state.failure = 403
  await page.goto(root)
  await expect(page).toHaveURL('/forbidden')
})
