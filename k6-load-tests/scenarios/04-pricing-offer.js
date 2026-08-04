import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, regularOptions } from '../config.js';
import { authParams, pricingPayload } from '../helpers.js';

export const options = regularOptions(5000, 0.10, 0.85);

export default function () {
  const params = authParams();
  const response = http.post(`${BASE_URL}/api/v1/bookings/pricing/offers`, JSON.stringify(pricingPayload()), params);
  check(response, {
    'pricing offer status is 200': (r) => r.status === 200,
    'pricing offer response is not empty': (r) => Boolean(r.body),
  });
  sleep(1);
}
