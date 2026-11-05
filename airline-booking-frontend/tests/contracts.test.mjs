import { test, beforeEach } from 'node:test'
import assert from 'node:assert/strict'
import axios, { AxiosError } from 'axios'

let handler
axios.defaults.adapter = config => handler(config)
const { axiosClient, refreshSession } = await import('../src/shared/api/axiosClient.ts')
const { useAuthStore } = await import('../src/features/auth/store/auth.store.ts')
const { airportApi } = await import('../src/features/airports/api/airport.api.ts')
const { aircraftApi } = await import('../src/features/aircrafts/api/aircraft.api.ts')
const { userApi } = await import('../src/features/users/api/user.api.ts')
const { authApi } = await import('../src/features/auth/api/auth.api.ts')
const { normalizeApiError } = await import('../src/shared/api/apiError.ts')
const { createBookingSchema } = await import('../src/features/bookings/schemas/booking.schema.ts')
const { createPaymentSchema } = await import('../src/features/payments/schemas/payment.schema.ts')
const { flightScheduleCreateSchema } = await import('../src/features/flight-schedules/schemas/flight-schedule.schema.ts')
const { userListSchema } = await import('../src/features/users/schemas/user.schema.ts')
const { loginSchema, registerSchema } = await import('../src/features/auth/schemas/auth.schema.ts')
const { canTransitionSchedule, canEditSeatPrice } = await import('../src/features/flight-schedules/utils/scheduleRules.ts')

const response = (config, data, status = 200) => ({ config, data, status, statusText: '', headers: {} })
const fail = (config, status, data) => Promise.reject(new AxiosError('Rejected', 'ERR_BAD_REQUEST', config, {}, response(config, data, status)))
const session = token => ({ accessToken: token, tokenType: 'Bearer', expiresIn: 3600, userId: 'user', email: 'user@example.com', roles: ['CUSTOMER'] })
beforeEach(() => { useAuthStore.getState().clearSession(); handler = () => { throw new Error('Unexpected request') } })

test('real API methods, unwrapped arrays/pages, distinct seat list and create DTOs', async () => {
  const calls = []
  const results = [[{ id: 'airport' }], { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 },
    { id: 'user', fullName: 'Updated' }, [{ id: 'seat', seatNumber: '1A', seatClass: 'BUSINESS' }],
    { id: 'seat', aircraftId: 'plane', seatNumber: '1B', seatClass: 'ECONOMY' }, '', { id: 'airport' }]
  handler = async config => { calls.push(config); return response(config, results[calls.length - 1]) }
  assert.deepEqual(await airportApi.list({ status: 'ACTIVE' }), results[0])
  assert.deepEqual(await userApi.list({ sort: 'fullName', size: 20 }), results[1])
  await userApi.update('user', { fullName: 'Updated' })
  assert.deepEqual(await aircraftApi.seats('plane'), results[3])
  assert.equal((await aircraftApi.addSeat('plane', { seatNumber: '1B', seatClass: 'ECONOMY' })).aircraftId, 'plane')
  assert.equal(await userApi.delete('user'), undefined)
  await airportApi.create({ code: 'sgn', name: 'Airport', city: 'City', country: 'VN' })
  assert.deepEqual(calls.map(c => [c.method, c.url]), [
    ['get', '/airports'], ['get', '/users'], ['patch', '/users/user'], ['get', '/aircrafts/plane/seats'],
    ['post', '/aircrafts/plane/seats'], ['delete', '/users/user'], ['post', '/airports'],
  ])
  assert.equal(calls[1].params.sort, 'fullName')
  assert.equal(JSON.parse(calls[6].data).code, 'SGN')
})

test('normalizes current Java messages, ProblemDetail and network errors', () => {
  const error = (status, data) => new AxiosError('', '', {}, {}, response({}, data, status))
  assert.equal(normalizeApiError(error(409, { message: 'Seat conflict' })).detail, 'Seat conflict')
  const problem = normalizeApiError(error(422, { title: 'Invalid', detail: 'Cannot book', errorCode: 'SEAT_UNAVAILABLE' }))
  assert.equal(problem.status, 422)
  assert.equal(problem.errorCode, 'SEAT_UNAVAILABLE')
  assert.equal(normalizeApiError(new AxiosError('Network')).status, 0)
  assert.equal(normalizeApiError(error(403, '<html>Forbidden</html>')).status, 403)
})

test('concurrent 401s share one refresh, credentials and CSRF options are present', async () => {
  useAuthStore.getState().setSession(session('old'))
  let refreshes = 0
  handler = async config => {
    assert.equal(config.withCredentials, true)
    assert.equal(config.withXSRFToken, true)
    if (config.url === '/auth/refresh-token') {
      refreshes++
      assert.equal(config.data, undefined)
      await new Promise(resolve => setTimeout(resolve, 10))
      return response(config, session('new'))
    }
    if (config.headers.get('Authorization') === 'Bearer old') return fail(config, 401, {})
    assert.equal(config.headers.get('Authorization'), 'Bearer new')
    assert.equal(config.headers.has('X-Auth-Retry'), false)
    return response(config, { ok: true })
  }
  await Promise.all([axiosClient.get('/users'), axiosClient.get('/aircrafts'), authApi.me()])
  assert.equal(refreshes, 1)
})

test('invalid login never triggers refresh and failed refresh clears the session', async () => {
  useAuthStore.getState().setSession(session('old'))
  const urls = []
  handler = config => { urls.push(config.url); return fail(config, 401, { message: 'Expired' }) }
  await assert.rejects(authApi.login({ email: 'user@example.com', password: 'wrong' }), { status: 401 })
  assert.deepEqual(urls, ['/auth/login'])
  urls.length = 0
  await assert.rejects(axiosClient.get('/users'), { status: 401 })
  assert.deepEqual(urls, ['/users', '/auth/refresh-token'])
  assert.equal(useAuthStore.getState().accessToken, null)
})

test('a request still unauthorized after refresh is retried only once', async () => {
  useAuthStore.getState().setSession(session('old'))
  let refreshes = 0
  let requests = 0
  handler = async config => {
    if (config.url === '/auth/refresh-token') { refreshes++; return response(config, session('new')) }
    requests++
    return fail(config, 401, {})
  }
  await assert.rejects(axiosClient.get('/users'), { status: 401 })
  assert.equal(refreshes, 1)
  assert.equal(requests, 2)
  assert.equal(useAuthStore.getState().accessToken, null)
})

test('logout during initial refresh cannot resurrect the local session', async () => {
  let release
  handler = config => new Promise(resolve => { release = () => resolve(response(config, session('new'))) })
  const pending = refreshSession()
  await new Promise(resolve => setTimeout(resolve, 0))
  useAuthStore.getState().clearSession()
  release()
  await assert.rejects(pending)
  assert.equal(useAuthStore.getState().accessToken, null)
})

test('booking rejects empty/duplicate seats and client-managed totals; payment rejects amount and unsupported checkout', () => {
  const passenger = { fullName: 'Nguyen Van A', dateOfBirth: '1998-05-20', gender: 'MALE',
    documentNumber: '012345678', flightSeatId: '11111111-1111-4111-8111-111111111111' }
  const booking = { flightScheduleId: '22222222-2222-4222-8222-222222222222', passengers: [passenger] }
  assert.equal(createBookingSchema.safeParse(booking).success, true)
  assert.equal(createBookingSchema.safeParse({ ...booking, passengers: [] }).success, false)
  assert.equal(createBookingSchema.safeParse({ ...booking, passengers: [passenger, passenger] }).success, false)
  assert.equal(createBookingSchema.safeParse({ ...booking, totalAmount: 1 }).success, false)
  assert.equal(createPaymentSchema.safeParse({ paymentMethod: 'VNPAY' }).success, true)
  assert.equal(createPaymentSchema.safeParse({ paymentMethod: 'VNPAY', amount: 1 }).success, false)
  assert.equal(createPaymentSchema.safeParse({ paymentMethod: 'CREDIT_CARD' }).success, false)
})

test('validates schedule time/price, terminal transitions, held seats and backend user limits', () => {
  const schedule = { flightId: '11111111-1111-4111-8111-111111111111', aircraftId: '22222222-2222-4222-8222-222222222222',
    departureTime: '2026-11-20T08:00:00', arrivalTime: '2026-11-20T10:00:00', basePrice: 0 }
  assert.equal(flightScheduleCreateSchema.safeParse(schedule).success, true)
  assert.equal(flightScheduleCreateSchema.safeParse({ ...schedule, arrivalTime: schedule.departureTime }).success, false)
  assert.equal(flightScheduleCreateSchema.safeParse({ ...schedule, basePrice: -1 }).success, false)
  assert.equal(canTransitionSchedule('DEPARTED', 'CANCELLED'), false)
  assert.equal(canTransitionSchedule('COMPLETED', 'SCHEDULED'), false)
  assert.equal(canTransitionSchedule('SCHEDULED', 'BOARDING'), true)
  assert.equal(canEditSeatPrice('HELD'), false)
  assert.equal(userListSchema.safeParse({ size: 101 }).success, false)
  assert.equal(userListSchema.safeParse({ sort: 'createdAt,desc' }).success, false)
  assert.equal(loginSchema.safeParse({ email: 'a@example.com', password: 'short' }).success, true)
  assert.equal(registerSchema.safeParse({ email: 'a@example.com', password: 'short', fullName: 'User' }).success, false)
})
