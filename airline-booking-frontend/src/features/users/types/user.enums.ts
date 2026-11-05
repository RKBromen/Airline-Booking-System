// The actual Java enum is Users.UserStatus, not the empty user.enums.UserStatus.
export const USER_STATUSES = ['ACTIVE', 'BLOCKED', 'DELETED'] as const
export type UserStatus = typeof USER_STATUSES[number]
