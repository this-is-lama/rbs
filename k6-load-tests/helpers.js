import http from 'k6/http';
import { check, fail } from 'k6';
import {
  BASE_URL,
  USER_EMAIL,
  USER_PASSWORD,
  AUTH_PATH,
  RESTAURANT_ID,
  TABLE_ID,
  DISH_ID,
  VISIT_START,
  VISIT_END,
  GUESTS,
} from './config.js';

export function jsonParams(token = null) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return { headers };
}

export function login() {
  const response = http.post(
    `${BASE_URL}${AUTH_PATH}`,
    JSON.stringify({ email: USER_EMAIL, password: USER_PASSWORD }),
    jsonParams()
  );

  check(response, {
    'login status is 200': (r) => r.status === 200,
  });

  if (response.status !== 200) {
    fail(`Login failed. Status=${response.status}. Body=${response.body}`);
  }

  let body;
  try {
    body = response.json();
  } catch (e) {
    fail(`Login response is not JSON. Body=${response.body}`);
  }

  const token = body.accessToken || body.access_token || body.token || body.jwt || body?.data?.accessToken;
  if (!token) {
    fail(`Access token was not found in login response. Body=${response.body}`);
  }

  return token;
}

let cachedToken = null;

export function authParams() {
  if (!cachedToken) {
    cachedToken = login();
  }

  return jsonParams(cachedToken);
}

export function pricingPayload() {
  return {
    restaurantId: RESTAURANT_ID,
    tableId: TABLE_ID,
    startAt: VISIT_START,
    endAt: VISIT_END,
    preorderItems: [
      { dishId: DISH_ID, quantity: 1 },
    ],
  };
}

export function bookingPayload(pricingOfferId = null, withPreorder = true) {
  const payload = {
    restaurantId: RESTAURANT_ID,
    tableId: TABLE_ID,
    startAt: VISIT_START,
    endAt: VISIT_END,
    guests: GUESTS,
  };

  if (withPreorder) {
    if (pricingOfferId) {
      payload.pricingOfferId = pricingOfferId;
    }
    payload.dishes = [
      { dishId: DISH_ID, quantity: 1 },
    ];
  } else {
    payload.dishes = [];
  }

  return payload;
}

export function createPricingOffer(params) {
  const response = http.post(
    `${BASE_URL}/api/v1/bookings/pricing/offers`,
    JSON.stringify(pricingPayload()),
    params
  );

  check(response, {
    'pricing offer status is 200': (r) => r.status === 200,
    'pricing offer has id': (r) => {
      try {
        const body = r.json();
        return Boolean(body.offerId || body.id || body.pricingOfferId || body?.data?.offerId);
      } catch (e) {
        return false;
      }
    },
  });

  if (response.status !== 200) {
    return null;
  }

  try {
    const body = response.json();
    return body.offerId || body.id || body.pricingOfferId || body?.data?.offerId || null;
  } catch (e) {
    return null;
  }
}
