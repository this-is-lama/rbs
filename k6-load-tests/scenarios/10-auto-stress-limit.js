import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';
import { Gauge, Rate, Counter } from 'k6/metrics';
import {
  BASE_URL,
  RESTAURANT_ID,
  TABLE_ID,
  BOOKING_DATE,
  STRESS_MAX_VUS,
} from '../config.js';

http.setResponseCallback(http.expectedStatuses({ min: 200, max: 399 }));

export const stress_active_vus = new Gauge('stress_active_vus');
export const stress_bad_response_rate = new Rate('stress_bad_response_rate');
export const stress_server_error_rate = new Rate('stress_server_error_rate');
export const stress_bad_responses = new Counter('stress_bad_responses');
export const stress_failed_at_vus = new Gauge('stress_failed_at_vus');
export const stress_abort_at_vus = new Gauge('stress_abort_at_vus');
export const stress_scenario_success_rate = new Rate('stress_scenario_success_rate');

export const stress_restaurants_list_bad_response_rate = new Rate('stress_restaurants_list_bad_response_rate');
export const stress_restaurant_details_bad_response_rate = new Rate('stress_restaurant_details_bad_response_rate');
export const stress_availability_bad_response_rate = new Rate('stress_availability_bad_response_rate');

const MAX_RESTAURANTS_LIST_RESPONSE_TIME_MS = 3000;
const MAX_RESTAURANT_DETAILS_RESPONSE_TIME_MS = 3000;
const MAX_AVAILABILITY_RESPONSE_TIME_MS = 4000;
const MAX_BAD_SCENARIOS_IN_ROW = 5;

function buildStressStages(maxVus) {
  const max = Math.max(1, Number(maxVus || 1000));
  const step = Math.max(100, Math.floor(max / 10));
  const stages = [];

  for (let target = step; target <= max; target += step) {
    stages.push({ duration: '30s', target });
  }

  if (stages.length === 0 || stages[stages.length - 1].target !== max) {
    stages.push({ duration: '30s', target: max });
  }

  stages.push({ duration: '10s', target: 0 });
  return stages;
}

export const options = {
  stages: buildStressStages(STRESS_MAX_VUS),

  thresholds: {
    http_req_failed: [
      {
        threshold: 'rate<0.20',
        abortOnFail: true,
        delayAbortEval: '30s',
      },
    ],

    checks: [
      {
        threshold: 'rate>0.70',
        abortOnFail: true,
        delayAbortEval: '30s',
      },
    ],

    stress_bad_response_rate: [
      {
        threshold: 'rate<0.20',
        abortOnFail: true,
        delayAbortEval: '30s',
      },
    ],

    stress_server_error_rate: [
      {
        threshold: 'rate<0.10',
        abortOnFail: true,
        delayAbortEval: '30s',
      },
    ],

    stress_scenario_success_rate: [
      {
        threshold: 'rate>0.70',
        abortOnFail: true,
        delayAbortEval: '30s',
      },
    ],
  },
};

let badScenariosInRow = 0;

function hasExpectedStatus(response, expectedStatuses) {
  return expectedStatuses.includes(response.status);
}

function isTooSlow(response, maxResponseTimeMs) {
  return response.timings.duration > maxResponseTimeMs;
}

function recordStepResult({
                            response,
                            expectedStatuses,
                            maxResponseTimeMs,
                            currentVus,
                            stepBadResponseRate,
                          }) {
  const isStatusOk = hasExpectedStatus(response, expectedStatuses);
  const isServerError = response.status >= 500;
  const isBadResponse =
      !isStatusOk ||
      isTooSlow(response, maxResponseTimeMs);

  stress_bad_response_rate.add(isBadResponse);
  stress_server_error_rate.add(isServerError);
  stepBadResponseRate.add(isBadResponse);

  if (isBadResponse) {
    stress_bad_responses.add(1);
    stress_failed_at_vus.add(currentVus);
  }

  return !isBadResponse;
}

function registerScenarioResult(isScenarioSuccessful, currentVus) {
  stress_scenario_success_rate.add(isScenarioSuccessful);

  if (isScenarioSuccessful) {
    badScenariosInRow = 0;
    return;
  }

  badScenariosInRow += 1;
  stress_failed_at_vus.add(currentVus);

  if (badScenariosInRow >= MAX_BAD_SCENARIOS_IN_ROW) {
    stress_abort_at_vus.add(currentVus);

    exec.test.abort(
        `Система перестала справляться с публичным пользовательским сценарием. Примерное количество VUs: ${currentVus}. Причина: ${badScenariosInRow} неуспешных сценариев подряд.`
    );
  }
}

export default function () {
  const currentVus = exec.instance.vusActive;
  stress_active_vus.add(currentVus);

  let isScenarioSuccessful = true;

  const restaurantsListResponse = http.get(`${BASE_URL}/api/v1/restaurants`);

  const restaurantsListOk = recordStepResult({
    response: restaurantsListResponse,
    expectedStatuses: [200],
    maxResponseTimeMs: MAX_RESTAURANTS_LIST_RESPONSE_TIME_MS,
    currentVus,
    stepBadResponseRate: stress_restaurants_list_bad_response_rate,
  });

  isScenarioSuccessful = isScenarioSuccessful && restaurantsListOk;

  check(restaurantsListResponse, {
    'stress public flow restaurants list status is 200': (r) => r.status === 200,
    [`stress public flow restaurants list duration < ${MAX_RESTAURANTS_LIST_RESPONSE_TIME_MS} ms`]: (r) =>
        r.timings.duration < MAX_RESTAURANTS_LIST_RESPONSE_TIME_MS,
  });

  const restaurantDetailsResponse = http.get(
      `${BASE_URL}/api/v1/restaurants/${RESTAURANT_ID}`
  );

  const restaurantDetailsOk = recordStepResult({
    response: restaurantDetailsResponse,
    expectedStatuses: [200],
    maxResponseTimeMs: MAX_RESTAURANT_DETAILS_RESPONSE_TIME_MS,
    currentVus,
    stepBadResponseRate: stress_restaurant_details_bad_response_rate,
  });

  isScenarioSuccessful = isScenarioSuccessful && restaurantDetailsOk;

  check(restaurantDetailsResponse, {
    'stress public flow restaurant details status is 200': (r) => r.status === 200,
    [`stress public flow restaurant details duration < ${MAX_RESTAURANT_DETAILS_RESPONSE_TIME_MS} ms`]: (r) =>
        r.timings.duration < MAX_RESTAURANT_DETAILS_RESPONSE_TIME_MS,
  });

  const availabilityUrl =
      `${BASE_URL}/api/v1/bookings/public/restaurants/${RESTAURANT_ID}` +
      `/tables/${TABLE_ID}/availability?date=${BOOKING_DATE}`;

  const availabilityResponse = http.get(availabilityUrl);

  const availabilityOk = recordStepResult({
    response: availabilityResponse,
    expectedStatuses: [200],
    maxResponseTimeMs: MAX_AVAILABILITY_RESPONSE_TIME_MS,
    currentVus,
    stepBadResponseRate: stress_availability_bad_response_rate,
  });

  isScenarioSuccessful = isScenarioSuccessful && availabilityOk;

  check(availabilityResponse, {
    'stress public flow availability status is 200': (r) => r.status === 200,
    [`stress public flow availability duration < ${MAX_AVAILABILITY_RESPONSE_TIME_MS} ms`]: (r) =>
        r.timings.duration < MAX_AVAILABILITY_RESPONSE_TIME_MS,
  });

  registerScenarioResult(isScenarioSuccessful, currentVus);

  sleep(1);
}

function value(data, metricName, valueName, defaultValue = 0) {
  return data.metrics[metricName]?.values?.[valueName] ?? defaultValue;
}

function formatRate(data, metricName) {
  return `${(value(data, metricName, 'rate') * 100).toFixed(2)} %`;
}

export function handleSummary(data) {
  const firstFailedAtVus = value(
      data,
      'stress_failed_at_vus',
      'min',
      'не определено'
  );

  const maxFailedAtVus = value(
      data,
      'stress_failed_at_vus',
      'max',
      'не определено'
  );

  const abortAtVus = value(
      data,
      'stress_abort_at_vus',
      'max',
      'не определено'
  );

  const maxActiveVus =
      value(data, 'stress_active_vus', 'max', null) ??
      value(data, 'vus', 'max', 'не определено');

  const badResponseRate = value(data, 'stress_bad_response_rate', 'rate');
  const serverErrorRate = value(data, 'stress_server_error_rate', 'rate');
  const scenarioSuccessRate = value(
      data,
      'stress_scenario_success_rate',
      'rate'
  );
  const badResponses = value(data, 'stress_bad_responses', 'count');
  const avgDuration = value(data, 'http_req_duration', 'avg');
  const p95Duration = value(data, 'http_req_duration', 'p(95)');

  let stopReason = 'не определена';

  if (badResponseRate >= 0.20) {
    stopReason = 'превышение допустимой доли плохих ответов';
  } else if (serverErrorRate >= 0.10) {
    stopReason = 'превышение допустимой доли серверных ошибок 5xx';
  } else if (scenarioSuccessRate <= 0.70) {
    stopReason = 'снижение доли успешно выполненных пользовательских сценариев';
  } else if (abortAtVus !== 'не определено') {
    stopReason = 'получение нескольких неуспешных пользовательских сценариев подряд';
  }

  const report = `# Отчет стресс-теста публичного пользовательского сценария

## Проверяемый сценарий

В рамках теста один виртуальный пользователь последовательно выполняет следующие действия:

- получает список ресторанов
- открывает карточку выбранного ресторана
- проверяет доступность выбранного стола на дату бронирования

## Итог

Тест был остановлен по причине: **${stopReason}**.

Первые признаки деградации производительности зафиксированы примерно при: **${firstFailedAtVus} VUs**.

Максимальное значение нагрузки, при котором фиксировались плохие ответы: **${maxFailedAtVus} VUs**.

Максимальное количество активных VUs во время теста: **${maxActiveVus} VUs**.

## Основные показатели

| Показатель | Значение |
|---|---:|
| Причина остановки | ${stopReason} |
| Первые признаки деградации | ${firstFailedAtVus} VUs |
| Максимальное VUs с плохими ответами | ${maxFailedAtVus} VUs |
| VUs при ручной остановке теста | ${abortAtVus} |
| Максимальное активное VUs | ${maxActiveVus} VUs |
| Доля успешных пользовательских сценариев | ${(scenarioSuccessRate * 100).toFixed(2)} % |
| Доля плохих ответов | ${(badResponseRate * 100).toFixed(2)} % |
| Доля серверных ошибок 5xx | ${(serverErrorRate * 100).toFixed(2)} % |
| Количество плохих ответов | ${badResponses} |
| Среднее время ответа по всем запросам | ${avgDuration.toFixed(2)} мс |
| 95-й перцентиль времени ответа по всем запросам | ${p95Duration.toFixed(2)} мс |

## Доля плохих ответов по этапам сценария

| Этап сценария | Доля плохих ответов |
|---|---:|
| Получение списка ресторанов | ${formatRate(data, 'stress_restaurants_list_bad_response_rate')} |
| Получение карточки ресторана | ${formatRate(data, 'stress_restaurant_details_bad_response_rate')} |
| Проверка доступности стола | ${formatRate(data, 'stress_availability_bad_response_rate')} |

## Условия остановки теста

Тест автоматически останавливается, если выполняется одно из условий:

- доля HTTP-ошибок превышает 20 %
- доля плохих ответов превышает 20 %
- доля серверных ошибок 5xx превышает 10 %
- доля успешно выполненных пользовательских сценариев становится ниже 70 %
- один виртуальный пользователь получил ${MAX_BAD_SCENARIOS_IN_ROW} неуспешных пользовательских сценариев подряд

## Что считается плохим ответом

Плохим ответом считается ситуация, когда:

- статус ответа не соответствует ожидаемому для этапа сценария
- сервер вернул ошибку 5xx
- время ответа превысило допустимый предел для этапа сценария

## Допустимое время ответа по этапам

| Этап сценария | Допустимое время ответа |
|---|---:|
| Получение списка ресторанов | ${MAX_RESTAURANTS_LIST_RESPONSE_TIME_MS} мс |
| Получение карточки ресторана | ${MAX_RESTAURANT_DETAILS_RESPONSE_TIME_MS} мс |
| Проверка доступности стола | ${MAX_AVAILABILITY_RESPONSE_TIME_MS} мс |

## Интерпретация результата

Данный тест показывает устойчивость публичной части пользовательского сценария без входа в систему, расчета сервисного сбора и создания бронирования. Нагрузка распределяется между API-шлюзом, сервисом ресторанов, сервисом бронирований и соответствующими базами данных.

`;

  return {
    stdout: report,
    'reports/stress-public-flow-summary.md': report,
    'reports/stress-public-flow-summary.json': JSON.stringify(data, null, 2),
  };
}