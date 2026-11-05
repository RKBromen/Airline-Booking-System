/** Matches the UUID path parameter accepted by Spring's AirportController. */
export const isAirportId = (id?: string): id is string =>
  Boolean(id && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id))

export function airportPage(value: string | null): number {
  if (!value || !/^[1-9]\d*$/.test(value)) return 1
  const page = Number(value)
  return Number.isSafeInteger(page) ? page : 1
}
