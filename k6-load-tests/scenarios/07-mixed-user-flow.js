import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, RESTAURANT_ID, TABLE_ID, BOOKING_DATE, regularOptions } from '../config.js';
import { authParams, createPricingOffer, bookingPayload } from '../helpers.js';

export const options = regularOptions(9000, 0.15, 0.80);

export default function () {
  const params = authParams();

  const listResponse = http.get(`${BASE_URL}/api/v1/restaurants`);
  check(listResponse, {
    'mixed restaurants list status is 200': (r) => r.status === 200,
  });

  const detailsResponse = http.get(`${BASE_URL}/api/v1/restaurants/${RESTAURANT_ID}`);
  check(detailsResponse, {
    'mixed restaurant details status is 200': (r) => r.status === 200,
  });

  const availabilityUrl = `${BASE_URL}/api/v1/bookings/public/restaurants/${RESTAURANT_ID}/tables/${TABLE_ID}/availability?date=${BOOKING_DATE}`;
  const availabilityResponse = http.get(availabilityUrl);
  check(availabilityResponse, {
    'mixed availability status is 200': (r) => r.status === 200,
  });

  const offerId = createPricingOffer(params);
  if (offerId) {
    const bookingResponse = http.post(`${BASE_URL}/api/v1/bookings`, JSON.stringify(bookingPayload(offerId, true)), params);
    check(bookingResponse, {
      'mixed booking status is 200 or 201 or conflict': (r) => r.status === 200 || r.status === 201 || r.status === 409,
    });
  }

  sleep(1);
}
