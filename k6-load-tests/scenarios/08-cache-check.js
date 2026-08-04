import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, regularOptions } from '../config.js';
import { authParams, pricingPayload } from '../helpers.js';

export const options = regularOptions(6000, 0.10, 0.85);

export default function () {
  const params = authParams();
  const body = JSON.stringify(pricingPayload());

  const first = http.post(`${BASE_URL}/api/v1/bookings/pricing/offers`, body, params);
  const second = http.post(`${BASE_URL}/api/v1/bookings/pricing/offers`, body, params);

  check(first, {
    'cache first pricing status is 200': (r) => r.status === 200,
  });
  check(second, {
    'cache second pricing status is 200': (r) => r.status === 200,
    'cache second duration below 5 sec': (r) => r.timings.duration < 5000,
  });
  sleep(1);
}
