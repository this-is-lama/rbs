import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, RESTAURANT_ID, regularOptions } from '../config.js';

export const options = regularOptions(3000, 0.05, 0.90);

export default function () {
  const response = http.get(`${BASE_URL}/api/v1/restaurants/${RESTAURANT_ID}`);
  check(response, {
    'restaurant details status is 200': (r) => r.status === 200,
    'restaurant details response is not empty': (r) => Boolean(r.body),
  });
  sleep(1);
}
