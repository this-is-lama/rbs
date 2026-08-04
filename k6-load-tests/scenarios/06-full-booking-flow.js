import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, regularOptions } from '../config.js';
import { authParams, createPricingOffer, bookingPayload } from '../helpers.js';

export const options = regularOptions(9000, 0.15, 0.80);

export default function () {
  const params = authParams();
  const offerId = createPricingOffer(params);

  if (!offerId) {
    sleep(1);
    return;
  }

  const response = http.post(`${BASE_URL}/api/v1/bookings`, JSON.stringify(bookingPayload(offerId, true)), params);
  check(response, {
    'full booking flow status is 200 or 201 or conflict': (r) => r.status === 200 || r.status === 201 || r.status === 409,
  });
  sleep(1);
}
