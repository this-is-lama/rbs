import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, regularOptions } from '../config.js';

export const options = regularOptions(2000, 0.05, 0.95);

export default function () {
  const response = http.get(`${BASE_URL}/actuator/health`);
  check(response, {
    'health status is 200': (r) => r.status === 200,
    'health response is not empty': (r) => Boolean(r.body),
  });
  sleep(1);
}
