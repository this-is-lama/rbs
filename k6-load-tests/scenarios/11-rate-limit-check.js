import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { BASE_URL, AUTH_PATH } from '../config.js';

// Один VU, 20 запросов подряд почти без пауз — специально бьём в burst,
// чтобы гарантированно упереться в лимит на /api/v1/auth/** (5 req/s, burst 10).
export const options = {
  vus: 1,
  iterations: 20,
  thresholds: {
    // ждём, что хотя бы часть запросов после burst словит 429 —
    // если rate == 0, значит лимитер не сработал вообще
    'rate_limited{route:auth}': ['rate>0'],
  },
};

const rateLimited = new Rate('rate_limited');

export default function () {
  const res = http.post(
    `${BASE_URL}${AUTH_PATH}`,
    JSON.stringify({ email: 'rate-limit-test@mail.com', password: 'wrong-password' }),
    { headers: { 'Content-Type': 'application/json' }, tags: { route: 'auth' } }
  );

  const is429 = res.status === 429;
  rateLimited.add(is429, { route: 'auth' });

  check(res, {
    'status is 4xx (not 5xx, not 2xx for wrong creds)': (r) => r.status >= 400 && r.status < 500,
  });

  console.log(`iteration status=${res.status}${is429 ? ' <- rate limited' : ''}`);

  sleep(0.05); // ~20 req/sec суммарно — быстрее устойчивого rate в 5/сек
}
