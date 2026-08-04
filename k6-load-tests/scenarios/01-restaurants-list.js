import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, regularOptions } from '../config.js';

export const options = regularOptions(3000, 0.05, 0.90);

export default function () {
  const response = http.get(`${BASE_URL}/api/v1/restaurants`);
  check(response, {
    'restaurants list status is 200': (r) => r.status === 200,
    'restaurants list response is not empty': (r) => Boolean(r.body),
  });
  sleep(1);
}
