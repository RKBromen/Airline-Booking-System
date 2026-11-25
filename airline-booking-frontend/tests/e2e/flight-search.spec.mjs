import { test, expect } from '@playwright/test'

test('loads all flights, applies an optional date filter on submit, and clears it', async ({ page }) => {
  const flights = [
    { id: 'one', flight_id: 'VN101', departure_id: 'SGN', arrival_id: 'HAN', departure_time: '2026-11-01T10:00:00', arrival_time: '2026-11-01T12:00:00', base_price: 1800000, seatAvailable: 5 },
    { id: 'two', flight_id: 'VN201', departure_id: 'SGN', arrival_id: 'DAD', departure_time: '2026-11-03T08:00:00', arrival_time: '2026-11-03T09:25:00', base_price: 950000, seatAvailable: 0 },
  ]
  const requests = []
  await page.route('**/api/v1/airports**', route => route.fulfill({ json: [] }))
  await page.route('**/api/v1/search/flights**', route => {
    const params = new URL(route.request().url()).searchParams
    requests.push(params.toString())
    return route.fulfill({ json: params.has('date') ? [flights[0]] : flights })
  })
  await page.goto('/')
  await expect(page.getByRole('heading', { name: 'Tất cả chuyến bay' })).toBeVisible()
  await expect(page.locator('.flight-result-card')).toHaveCount(2)
  expect(requests).toEqual([''])
  await page.getByLabel('Ngày khởi hành').fill('2026-11-01')
  expect(requests).toEqual([''])
  await page.getByRole('button', { name: 'Tìm chuyến bay', exact: true }).click()
  await expect(page).toHaveURL(/date=2026-11-01/)
  await expect(page.locator('.flight-result-card')).toHaveCount(1)
  expect(requests).toContain('date=2026-11-01')
  await page.getByRole('button', { name: 'Xóa bộ lọc' }).click()
  await expect(page.getByRole('heading', { name: 'Tất cả chuyến bay' })).toBeVisible()
  await expect(page.locator('.flight-result-card')).toHaveCount(2)
  await expect(page.getByLabel('Ngày khởi hành')).toHaveValue('')
})
