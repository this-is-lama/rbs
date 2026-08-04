export const BASE_URL = 'http://localhost:8080';

export const USER_EMAIL = 'test-user@mail.com';
export const USER_PASSWORD = '123456';
export const AUTH_PATH = '/api/v1/auth/login';

export const RESTAURANT_ID = 'd7ffb5c2-e070-4795-9ddb-2b32d0afbeba';
export const TABLE_ID = 'c638d519-3ffc-40ff-a872-c0b4c0e58a12';
export const DISH_ID = '022a0769-7237-45d6-a577-12393673faa4';

export const BOOKING_DATE = '2026-06-15';
export const VISIT_START = '2026-06-15T15:00:00Z';
export const VISIT_END = '2026-06-15T17:00:00Z';
export const GUESTS = 2;

export const REGULAR_VUS = 100;
export const REGULAR_DURATION = '2m';
export const STRESS_MAX_VUS = 1000;

export function regularOptions(p95 = 5000, errorRate = 0.10, checksRate = 0.85) {
  return {
    vus: REGULAR_VUS,
    duration: REGULAR_DURATION,
    thresholds: {
      http_req_failed: [`rate<${errorRate}`],
      http_req_duration: [`p(95)<${p95}`],
      checks: [`rate>${checksRate}`],
    },
  };
}