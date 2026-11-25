# Phase 5: Booking notifications

The notification module consumes booking events and writes notification work to
`notification_history`. Registration also publishes a `user.registered` event
after its database transaction commits. The Kafka listener does not call the
booking service or join its database transaction. The `EmailService` interface
is the seam for a provider; `mock` logs acceptance without sending mail, while
`smtp` sends through the configured mail server.

## Topics and event contract

- `booking.confirmed` carries `BOOKING_CONFIRMED` events.
- `booking.cancelled` carries `BOOKING_CANCELLED` events.
- `user.registered` carries `ACCOUNT_REGISTERED` events.
- Event version `1` is currently supported.
- `eventId` identifies one source event. `bookingId` identifies its booking.
- The consumer accepts a direct event JSON object and the generic envelope
  `{eventId, aggregateId, eventType, eventVersion, occurredAt, payload}`. For an
  envelope, `aggregateId` becomes `bookingId` and `payload` provides the booking
  details below.

Example direct event:

```json
{
  "eventId": "a73a98de-d302-48df-a993-d136e8c75ccb",
  "eventVersion": 1,
  "bookingId": "384c2274-6402-4e04-89cf-5d31de800d42",
  "bookingCode": "AB12CD",
  "recipientEmail": "customer@example.com",
  "recipientName": "Nguyen Van A",
  "flightNumber": "AB123",
  "departureTime": "2026-12-10T08:30:00Z"
}
```

The event producer/outbox has not been wired to booking changes yet. Once Phase
4 publishes the two topics, it can use these fields without requiring the
notification module to read booking tables. The topic names and consumer group
can be overridden with `BOOKING_CONFIRMED_TOPIC`, `BOOKING_CANCELLED_TOPIC`,
`USER_REGISTERED_TOPIC`, and `NOTIFICATION_KAFKA_CONSUMER_GROUP`. Registration
events currently publish directly to Kafka after the user transaction commits;
an outbox should replace this best-effort bridge when Phase 4 is implemented.

## Email provider

`NOTIFICATION_EMAIL_PROVIDER` defaults to `mock`. Set it to `smtp` and provide
`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_FROM` to
enable SMTP. The worker marks a message `SENT` when the
configured provider accepts the send request; with the mock provider this means
only that the notification flow was exercised. Delivery retries and stuck
`PROCESSING` recovery belong to Phase 6.

The database migration is version `6`, after payment (`4`) and role-seed (`5`)
migrations in the active checkout.
