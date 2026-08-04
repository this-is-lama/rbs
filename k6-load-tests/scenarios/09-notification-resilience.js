import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, regularOptions } from '../config.js';
import { authParams, bookingPayload } from '../helpers.js';

export const options = regularOptions(8000, 0.15, 0.80);

export default function () {
  const params = authParams();
  const response = http.post(`${BASE_URL}/api/v1/bookings`, JSON.stringify(bookingPayload(null, false)), params);
  check(response, {
    'notification resilience status is 200 or 201 or conflict': (r) => r.status === 200 || r.status === 201 || r.status === 409,
  });
  sleep(1);
}
