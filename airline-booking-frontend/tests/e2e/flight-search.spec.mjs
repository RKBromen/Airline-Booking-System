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
  await expect(page).not.toHaveURL(/undefined/)
  await expect(page.getByRole('heading', { name: 'Kết quả tìm kiếm' })).toBeVisible()
  await expect(page.locator('.flight-result-card')).toHaveCount(1)
  expect(requests).toContain('date=2026-11-01')
  await page.getByRole('button', { name: 'Xóa bộ lọc' }).click()
  await expect(page.getByRole('heading', { name: 'Tất cả chuyến bay' })).toBeVisible()
  await expect(page.locator('.flight-result-card')).toHaveCount(2)
  await expect(page.getByLabel('Ngày khởi hành')).toHaveValue('')
})

test('allows selecting only available seats and shows the selected total', async ({ page }) => {
  await page.route('**/api/v1/search/flights/schedule-1/seats', route => route.fulfill({
    json: {
      scheduleId: 'schedule-1',
      seats: [
        { id: 'seat-1a', seatNumber: '1A', seatClass: 'BUSINESS', price: 1800000, status: 'BOOKED' },
        { id: 'seat-1b', seatNumber: '1B', seatClass: 'BUSINESS', price: 1800000, status: 'AVAILABLE' },
        { id: 'seat-2a', seatNumber: '2A', seatClass: 'ECONOMY', price: 1200000, status: 'AVAILABLE' },
      ],
    },
  }))

  await page.goto('/flights/schedule-1/seats')

  const bookedSeat = page.getByRole('button', { name: /1A, BUSINESS, Đã đặt/ })
  const availableSeat = page.getByRole('button', { name: /1B, BUSINESS, Còn trống/ })
  await expect(bookedSeat).toBeDisabled()
  await expect(availableSeat).toBeEnabled()
  await availableSeat.click()
  await expect(page.getByText('Đã chọn 1 ghế')).toBeVisible()
  await expect(page.getByText('Ghế: 1B')).toBeVisible()
  await expect(page.getByText('Tổng tiền: 1.800.000 ₫')).toBeVisible()
  await availableSeat.click()
  await expect(page.getByText('Chưa chọn ghế')).toBeVisible()
})

test('shows a not-found state instead of an empty schedule for an unknown id', async ({ page }) => {
  await page.route('**/api/v1/search/flights/missing-schedule', route => route.fulfill({
    status: 404,
    json: { message: 'Flight schedule not found with id: missing-schedule' },
  }))

  await page.goto('/flights/missing-schedule')

  await expect(page.getByText('Không tìm thấy lịch bay', { exact: true })).toBeVisible()
  await expect(page.getByRole('link', { name: 'Quay lại tìm chuyến bay' })).toBeVisible()
  await expect(page.getByRole('link', { name: 'Chọn ghế' })).toHaveCount(0)
})
