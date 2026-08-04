import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, RESTAURANT_ID, TABLE_ID, BOOKING_DATE, regularOptions } from '../config.js';

export const options = regularOptions(4000, 0.05, 0.90);

export default function () {
  const url = `${BASE_URL}/api/v1/bookings/public/restaurants/${RESTAURANT_ID}/tables/${TABLE_ID}/availability?date=${BOOKING_DATE}`;
  const response = http.get(url);
  check(response, {
    'availability status is 200': (r) => r.status === 200,
    'availability response is not empty': (r) => Boolean(r.body),
  });
  sleep(1);
}
