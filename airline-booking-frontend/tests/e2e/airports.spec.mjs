import { test, expect } from '@playwright/test'

const airport = {
  id: '11111111-1111-4111-8111-111111111111', code: 'SGN',
  name: 'Tan Son Nhat International Airport', city: 'Ho Chi Minh City', country: 'Vietnam', status: 'ACTIVE',
}
const airports = [airport, ...Array.from({ length: 11 }, (_, index) => ({
  ...airport, id: `22222222-2222-4222-8222-${String(index + 1).padStart(12, '0')}`,
  code: `A${String(index + 1).padStart(2, '0')}`, name: `Airport ${index + 1}`,
}))]
const api = '**/api/v1/airports**'
const respond = (route, json, status = 200) => route.fulfill({ status, json })
const noOverflow = async page => {
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
}

test('public list paginates, searches, opens details, reloads and returns', async ({ page }, testInfo) => {
  const calls = []
  await page.route(api, route => {
    const url = new URL(route.request().url())
    calls.push(url)
    if (url.pathname.endsWith(airport.id)) return respond(route, airport)
    return respond(route, url.searchParams.has('keyword') ? [airport] : airports)
  })
  await page.goto('/airports')
  await expect(page.getByRole('heading', { name: 'Danh sách sân bay' })).toBeVisible()
  await expect(page.getByRole('link', { name: 'SGN', exact: true })).toBeVisible()
  expect(calls.every(url => !url.searchParams.has('page') && !url.searchParams.has('size'))).toBe(true)
  await noOverflow(page)
  await page.screenshot({ path: testInfo.outputPath('airports-list.png'), fullPage: true })
  await page.locator('.ant-pagination-item-2').click()
  await expect(page).toHaveURL(/page=2/)
  await expect(page.getByRole('link', { name: 'A11', exact: true })).toBeVisible()
  await expect(page.getByRole('link', { name: 'SGN', exact: true })).toHaveCount(0)
  await page.getByRole('searchbox', { name: 'Tìm sân bay' }).fill('SGN')
  await page.getByRole('button', { name: 'Tìm kiếm', exact: true }).click()
  await expect(page).toHaveURL(/keyword=SGN$/)
  await expect(page.getByRole('link', { name: 'SGN', exact: true })).toBeVisible()
  expect(calls.some(url => url.searchParams.get('keyword') === 'SGN')).toBe(true)
  await page.getByRole('link', { name: 'SGN', exact: true }).click()
  await expect(page).toHaveURL(`/airports/${airport.id}`)
  await expect(page.getByRole('heading', { name: airport.name })).toBeVisible()
  await noOverflow(page)
  await page.screenshot({ path: testInfo.outputPath('airport-detail.png'), fullPage: true })
  await page.reload()
  await expect(page.getByRole('heading', { name: airport.name })).toBeVisible()
  await page.getByRole('link', { name: 'Quay lại danh sách sân bay' }).click()
  await expect(page).toHaveURL('/airports')
})

test('empty response and empty search results are distinct, clearing restores results', async ({ page }) => {
  let empty = true
  await page.route(api, route => respond(route,
    empty || new URL(route.request().url()).searchParams.has('keyword') ? [] : [airport]))
  await page.goto('/airports')
  await expect(page.getByText('Chưa có sân bay trong hệ thống.')).toBeVisible()
  empty = false
  await page.reload()
  await expect(page.getByRole('link', { name: 'SGN', exact: true })).toBeVisible()
  await page.getByRole('searchbox', { name: 'Tìm sân bay' }).fill('not-found')
  await page.getByRole('button', { name: 'Tìm kiếm', exact: true }).click()
  await expect(page.getByText('Không tìm thấy sân bay phù hợp.', { exact: false })).toBeVisible()
  await page.getByRole('searchbox', { name: 'Tìm sân bay' }).clear()
  await page.getByRole('button', { name: 'Tìm kiếm', exact: true }).click()
  await expect(page.getByRole('link', { name: 'SGN', exact: true })).toBeVisible()
})

test('list errors show retry and recover', async ({ page }) => {
  let failed = true
  await page.route(api, route => failed ? respond(route, { message: 'Server unavailable' }, 500) : respond(route, [airport]))
  await page.goto('/airports')
  await expect(page.getByRole('alert')).toContainText('Server unavailable', { timeout: 15_000 })
  failed = false
  await page.getByRole('button', { name: 'Thử lại' }).click()
  await expect(page.getByRole('link', { name: 'SGN', exact: true })).toBeVisible()
})

test('network failure can be retried', async ({ page }) => {
  let failed = true
  await page.route(api, route => failed ? route.abort('failed') : respond(route, [airport]))
  await page.goto('/airports')
  await expect(page.getByRole('alert')).toContainText('Không thể kết nối', { timeout: 15_000 })
  failed = false
  await page.getByRole('button', { name: 'Thử lại' }).click()
  await expect(page.getByRole('link', { name: 'SGN', exact: true })).toBeVisible()
})

test('direct detail URL renders loading then data without requiring login', async ({ page }) => {
  let release
  const pending = new Promise(resolve => { release = resolve })
  await page.route(api, async route => { await pending; await respond(route, airport) })
  await page.goto(`/airports/${airport.id}`)
  await expect(page.getByRole('status')).toHaveAttribute('aria-label', 'Đang tải thông tin sân bay…')
  release()
  await expect(page.getByRole('heading', { name: airport.name })).toBeVisible()
  await noOverflow(page)
})

test('missing airport returns 404 and malformed IDs never call API', async ({ page }) => {
  let requests = 0
  await page.route(api, route => { requests++; return respond(route, { message: 'Not found' }, 404) })
  await page.goto(`/airports/${airport.id}`)
  await expect(page.getByText('Không tìm thấy sân bay', { exact: true })).toBeVisible()
  expect(requests).toBe(1)
  requests = 0
  await page.goto('/airports/not-a-uuid')
  await expect(page.getByText('Không tìm thấy sân bay', { exact: true })).toBeVisible()
  expect(requests).toBe(0)
})

test('detail server error has retry and recovers', async ({ page }) => {
  let failed = true
  await page.route(api, route => failed ? respond(route, {}, 503) : respond(route, airport))
  await page.goto(`/airports/${airport.id}`)
  await expect(page.getByRole('button', { name: 'Thử lại' })).toBeVisible({ timeout: 15_000 })
  failed = false
  await page.getByRole('button', { name: 'Thử lại' }).click()
  await expect(page.getByRole('heading', { name: airport.name })).toBeVisible()
})
