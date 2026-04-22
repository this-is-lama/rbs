# Математическая модель динамического сервисного сбора

## 1. Назначение модели

Модель предназначена для расчета `serviceFee` в системе онлайн-бронирования столов.

В этой версии модели принимаются следующие упрощения:

- используется **одна общая математическая схема для всей системы**;
- используются **одни и те же веса модели для всех ресторанов**;
- для всех столов используется **одинаковая базовая значимость**, без индивидуальных коэффициентов привлекательности;
- модель опирается только на **успешно оформленные бронирования**;
- модель **не анализирует незавершенные действия пользователя** и **не строит вероятность подтверждения брони**.

То есть модель не отвечает на вопрос:

> "с какой вероятностью пользователь согласится на этот сервисный сбор?"

Вместо этого модель отвечает на другой вопрос:

> "насколько выбранный слот бронирования является востребованным и нагруженным, и какой сервисный сбор должен ему соответствовать?"

---

## 2. Экономический смысл сервисного сбора

В данной модели `serviceFee` не используется как инструмент прямой максимизации ожидаемой прибыли, как это было бы в вероятностной модели.

Здесь `serviceFee` выполняет другие функции:

1. **Компенсирует повышенную нагрузку на ресторан** в пиковые и сложные интервалы.
2. **Монетизирует востребованные слоты** бронирования.
3. **Дифференцирует стоимость бронирования** в зависимости от контекста.
4. **Стимулирует более крупный preorder**, так как при росте суммы предзаказа сервисный сбор уменьшается.
5. **Выравнивает спрос**, делая дорогими самые напряженные интервалы и более доступными менее загруженные.

Итоговая цель модели:

`serviceFee ~ demandIndex`

То есть сервисный сбор должен быть согласован с интегральной оценкой востребованности и нагрузки бронируемого слота.

---

## 3. Бизнес-правила

### 3.1. Бронирование без preorder

Если пользователь бронирует стол без предзаказа блюд:

`finalServiceFee = 0`

### 3.2. Бронирование с preorder

Если `hasPreorder = true`, система сначала рассчитывает `baseServiceFee`, а затем преобразует его в `finalServiceFee`.

### 3.3. Бесплатный сервисный сбор при большом preorder

Если сумма предзаказа достаточно велика:

`preorderAmount >= freeServiceFeeThreshold`

то:

`finalServiceFee = 0`

### 3.4. Плавное уменьшение сервисного сбора

Если preorder есть, но его сумма меньше порога, сбор уменьшается плавно:

`finalServiceFee = baseServiceFee * (1 - preorderAmount / freeServiceFeeThreshold)^preorderDiscountPower`

Это сделано для того, чтобы не возникал резкий скачок между, например, `2999` и `3000` рублями.

---

## 4. Почему модель не использует вероятность подтверждения брони

Изначально рассматривалась более сложная модель, в которой сервисный сбор подбирался через вероятность подтверждения брони пользователем. Такая модель требовала:

- фиксировать незавершенные сценарии;
- понимать, что считать отказом;
- отличать технический выход со страницы от реального отказа;
- оценивать чувствительность клиента к сервисному сбору.

На практике это означает необходимость сложной поведенческой аналитики, которой в системе нет.

Поэтому в финальной версии модели используются только **успешно оформленные бронирования**, так как это:

- надежно фиксируемые события;
- понятный источник данных;
- реалистичный путь для дипломного и практического решения.

---

## 5. Входные параметры модели

В этой версии модели используются следующие факторы:

1. текущая загрузка ресторана;
2. срочность брони;
3. популярность дня недели;
4. популярность времени суток;
5. погодный контекст;
6. выходные и праздничные дни.

### Что изменено относительно более ранней версии

- **нет ресторан-специфических весов**;
- **нет ресторан-специфических индексов**;
- **нет индивидуальных коэффициентов стола**;
- все рестораны и все столы работают по **одной общей конфигурации**.

Фактор популярности конкретного стола в этой версии убран как источник дифференциации, так как по условию все столы считаются одинаковыми по базовым коэффициентам.

Для формализации это означает:

`tableDemandIndex = 1`

и он больше не влияет на итоговый расчет.

---

## 6. Формализация отдельных факторов

### 6.1. `loadFactor`

Показывает текущую загрузку ресторана на выбранный интервал.

`loadFactor = bookedTablesCount / totalTablesCount`

где:

- `bookedTablesCount` — число уже занятых столов на выбранный слот;
- `totalTablesCount` — общее число столов в ресторане.

Диапазон:

`0 <= loadFactor <= 1`

### Почему используется именно доля

Абсолютное число занятых столов плохо сравнимо между ресторанами разного размера. Нормированная доля делает показатель сопоставимым.

---

### 6.2. `urgencyFactor`

Показывает, насколько бронирование является срочным.

`urgencyFactor = exp(-hoursBeforeVisit / urgencyScaleHours)`

где:

- `hoursBeforeVisit` — сколько часов осталось до визита;
- `urgencyScaleHours` — параметр масштаба, например `24`.

Диапазон:

`0 <= urgencyFactor <= 1`

### Интерпретация

- если бронь оформляется сильно заранее, значение близко к `0`;
- если бронь оформляется почти на текущее время, значение близко к `1`.

### Почему используется экспонента

Срочность растет неравномерно. Разница между бронью за 7 дней и 6 дней меньше, чем между бронью за 2 часа и 15 минут.

---

### 6.3. `dayDemandIndex`

Показывает историческую популярность выбранного дня недели.

Пусть:

- `successfulBookingsForSelectedDay` — число успешных броней для данного дня недели за окно наблюдения;
- `maxSuccessfulBookingsByDay` — максимальное число успешных броней среди всех дней недели.

Тогда:

`dayDemandIndex = successfulBookingsForSelectedDay / maxSuccessfulBookingsByDay`

Диапазон:

`0 <= dayDemandIndex <= 1`

### Что важно в этой версии

Этот индекс считается **по всей системе**, а не отдельно по ресторану.

---

### 6.4. `timeDemandIndex`

Показывает историческую популярность выбранного временного слота.

`timeDemandIndex = successfulBookingsForSelectedTimeSlot / maxSuccessfulBookingsByTimeSlot`

где:

- `successfulBookingsForSelectedTimeSlot` — число успешных броней в данном временном слоте;
- `maxSuccessfulBookingsByTimeSlot` — максимальное число успешных броней среди всех слотов.

Диапазон:

`0 <= timeDemandIndex <= 1`

Этот индекс также считается **по всем ресторанам системы вместе**.

---

## 7. Нечеткая логика для погоды

Погода плохо описывается простыми бинарными условиями, поэтому используется нечеткая логика.

### 7.1. Входные погодные параметры

- `temperature`
- `precipitation`
- `windSpeed`

### 7.2. Нечеткие классы

Вводятся три класса спроса, соответствующие погодным условиям:

- `weatherLowDemandClass`
- `weatherMediumDemandClass`
- `weatherHighDemandClass`

Для текущей погоды рассчитываются степени принадлежности:

- `weatherLowDemandMembership`
- `weatherMediumDemandMembership`
- `weatherHighDemandMembership`

### 7.3. Исторические индексы погодных классов

По успешным бронированиям за окно наблюдения считаются:

- `successfulBookingsWeatherLowClass`
- `successfulBookingsWeatherMediumClass`
- `successfulBookingsWeatherHighClass`

Нормировка:

`weatherLowClassIndex = successfulBookingsWeatherLowClass / maxWeatherClassSuccessfulBookings`

`weatherMediumClassIndex = successfulBookingsWeatherMediumClass / maxWeatherClassSuccessfulBookings`

`weatherHighClassIndex = successfulBookingsWeatherHighClass / maxWeatherClassSuccessfulBookings`

### 7.4. Итоговый погодный индекс

`weatherDemandIndex = (weatherLowDemandMembership * weatherLowClassIndex + weatherMediumDemandMembership * weatherMediumClassIndex + weatherHighDemandMembership * weatherHighClassIndex) / (weatherLowDemandMembership + weatherMediumDemandMembership + weatherHighDemandMembership)`

Диапазон:

`0 <= weatherDemandIndex <= 1`

### Почему это лучше жесткого if/else

Погода может быть одновременно частично "плохой" и частично "нейтральной". Нечеткая логика позволяет не делать искусственных резких границ.

---

## 8. Нечеткая логика для выходных и праздников

### 8.1. Классы дня

Используются четыре класса:

- `workdayClass`
- `weekendClass`
- `holidayClass`
- `peakHolidayClass`

Для них вычисляются степени принадлежности:

- `workdayMembership`
- `weekendMembership`
- `holidayMembership`
- `peakHolidayMembership`

### 8.2. Исторические индексы классов дня

По успешным бронированиям считаются:

- `successfulBookingsWorkdayClass`
- `successfulBookingsWeekendClass`
- `successfulBookingsHolidayClass`
- `successfulBookingsPeakHolidayClass`

Нормировка:

`workdayClassIndex = successfulBookingsWorkdayClass / maxHolidayClassSuccessfulBookings`

`weekendClassIndex = successfulBookingsWeekendClass / maxHolidayClassSuccessfulBookings`

`holidayClassIndex = successfulBookingsHolidayClass / maxHolidayClassSuccessfulBookings`

`peakHolidayClassIndex = successfulBookingsPeakHolidayClass / maxHolidayClassSuccessfulBookings`

### 8.3. Итоговый индекс календарного контекста

`holidayDemandIndex = (workdayMembership * workdayClassIndex + weekendMembership * weekendClassIndex + holidayMembership * holidayClassIndex + peakHolidayMembership * peakHolidayClassIndex) / (workdayMembership + weekendMembership + holidayMembership + peakHolidayMembership)`

Диапазон:

`0 <= holidayDemandIndex <= 1`

---

## 9. Блоки модели

Чтобы модель была устойчивой и интерпретируемой, факторы объединяются в блоки.

### 9.1. `loadBlock`

Блок текущей нагрузки:

`loadBlock = occupancyWeight * loadFactor^2 + urgencyWeight * urgencyFactor + interactionWeight * loadFactor * urgencyFactor`

где:

`occupancyWeight + urgencyWeight + interactionWeight = 1`

### Почему именно так

- `loadFactor^2` усиливает вклад высокой загрузки;
- `urgencyFactor` учитывает срочность;
- `loadFactor * urgencyFactor` учитывает ситуацию, когда бронь срочная и ресторан уже загружен.

---

### 9.2. `historyBlock`

Блок исторической востребованности:

`historyBlock = dayWeight * dayDemandIndex + timeWeight * timeDemandIndex`

где:

`dayWeight + timeWeight = 1`

### Почему таблицы здесь нет

В этой версии модели все столы считаются равнозначными по базовым коэффициентам, поэтому отдельный множитель `tableWeight * tableDemandIndex` убран.

---

### 9.3. `contextBlock`

Блок внешнего контекста:

`contextBlock = weatherWeight * weatherDemandIndex + holidayWeight * holidayDemandIndex`

где:

`weatherWeight + holidayWeight = 1`

---

## 10. Итоговый индекс спроса `demandIndex`

После расчета блоков строится общий индекс:

`demandIndex = loadBlockWeight * loadBlock + historyBlockWeight * historyBlock + contextBlockWeight * contextBlock`

где:

`loadBlockWeight + historyBlockWeight + contextBlockWeight = 1`

Диапазон:

`0 <= demandIndex <= 1`

### Смысл

`demandIndex` — это интегральная оценка того, насколько выбранный слот:

- загружен,
- востребован,
- сложен для обслуживания,
- ценен для ресторана.

---

## 11. Преобразование `demandIndex` в `baseServiceFee`

Используется сигмоидальная функция:

`baseServiceFee = minServiceFee + (maxServiceFee - minServiceFee) * (1 / (1 + exp(-sigmoidSlope * (demandIndex - sigmoidCenter))))`

где:

- `minServiceFee` — минимальное значение сбора;
- `maxServiceFee` — максимальное значение сбора;
- `sigmoidSlope` — параметр крутизны функции;
- `sigmoidCenter` — точка, около которой начинается выраженный рост.

### Почему сигмоида лучше линейной функции

Сигмоида:

- не дает бесконечного роста;
- сохраняет плавность;
- не делает слишком резких скачков при малом изменении `demandIndex`.

---

## 12. Финальный расчет `finalServiceFee`

Итоговая формула:

`finalServiceFee = 0, if hasPreorder = false`

`finalServiceFee = 0, if preorderAmount >= freeServiceFeeThreshold`

`finalServiceFee = baseServiceFee * (1 - preorderAmount / freeServiceFeeThreshold)^preorderDiscountPower, if 0 < preorderAmount < freeServiceFeeThreshold`

Итоговая стоимость:

`totalPrice = preorderAmount + finalServiceFee`

---

## 13. Стартовые экспертные параметры

На старте модель использует общие экспертные параметры для всей системы.

### 13.1. Параметры `loadBlock`

`occupancyWeight = 0.50`

`urgencyWeight = 0.20`

`interactionWeight = 0.30`

### 13.2. Параметры `historyBlock`

`dayWeight = 0.40`

`timeWeight = 0.60`

### 13.3. Параметры `contextBlock`

`weatherWeight = 0.40`

`holidayWeight = 0.60`

### 13.4. Параметры верхнего уровня

`loadBlockWeight = 0.45`

`historyBlockWeight = 0.35`

`contextBlockWeight = 0.20`

### 13.5. Параметры сервисного сбора

`minServiceFee = 50`

`maxServiceFee = 500`

`sigmoidSlope = 8`

`sigmoidCenter = 0.5`

`freeServiceFeeThreshold = 3000`

`preorderDiscountPower = 1.2`

---

## 14. Автоматическая корректировка весов

В этой версии модели веса могут корректироваться автоматически, но **не по отказам**, а по тому, насколько хорошо отдельные компоненты объясняют фактически реализованный спрос на успешных бронированиях.

### 14.1. `realizedDemandScore`

Вводится фактический показатель спроса:

`realizedDemandScore = successfulBookingsInVisitSlot / maxSuccessfulBookingsInAnyVisitSlotWithinWindow`

где:

- `successfulBookingsInVisitSlot` — число успешных броней в выбранном слоте;
- `maxSuccessfulBookingsInAnyVisitSlotWithinWindow` — максимальное число успешных броней среди всех слотов за окно наблюдения.

Диапазон:

`0 <= realizedDemandScore <= 1`

---

### 14.2. Оценка важности компонентов

Для каждого компонента вычисляется связь с `realizedDemandScore`.

Например, через абсолютную корреляцию:

`occupancyImportance = abs(corr(loadFactor^2, realizedDemandScore))`

`urgencyImportance = abs(corr(urgencyFactor, realizedDemandScore))`

`interactionImportance = abs(corr(loadFactor * urgencyFactor, realizedDemandScore))`

`dayImportance = abs(corr(dayDemandIndex, realizedDemandScore))`

`timeImportance = abs(corr(timeDemandIndex, realizedDemandScore))`

`weatherImportance = abs(corr(weatherDemandIndex, realizedDemandScore))`

`holidayImportance = abs(corr(holidayDemandIndex, realizedDemandScore))`

Для блоков:

`loadBlockImportance = abs(corr(loadBlock, realizedDemandScore))`

`historyBlockImportance = abs(corr(historyBlock, realizedDemandScore))`

`contextBlockImportance = abs(corr(contextBlock, realizedDemandScore))`

---

### 14.3. Рекомендуемые новые веса

#### Для `loadBlock`

`suggestedOccupancyWeight = occupancyImportance / (occupancyImportance + urgencyImportance + interactionImportance)`

`suggestedUrgencyWeight = urgencyImportance / (occupancyImportance + urgencyImportance + interactionImportance)`

`suggestedInteractionWeight = interactionImportance / (occupancyImportance + urgencyImportance + interactionImportance)`

#### Для `historyBlock`

`suggestedDayWeight = dayImportance / (dayImportance + timeImportance)`

`suggestedTimeWeight = timeImportance / (dayImportance + timeImportance)`

#### Для `contextBlock`

`suggestedWeatherWeight = weatherImportance / (weatherImportance + holidayImportance)`

`suggestedHolidayWeight = holidayImportance / (weatherImportance + holidayImportance)`

#### Для верхнего уровня

`suggestedLoadBlockWeight = loadBlockImportance / (loadBlockImportance + historyBlockImportance + contextBlockImportance)`

`suggestedHistoryBlockWeight = historyBlockImportance / (loadBlockImportance + historyBlockImportance + contextBlockImportance)`

`suggestedContextBlockWeight = contextBlockImportance / (loadBlockImportance + historyBlockImportance + contextBlockImportance)`

---

### 14.4. Плавное обновление весов

Чтобы веса не менялись слишком резко, используется сглаживание:

`newWeight = (1 - learningRate) * oldWeight + learningRate * suggestedWeight`

где `learningRate`, например, равен `0.1`.

---

### 14.5. Ограничение весов

Чтобы один фактор не получил слишком большой вес, вводятся границы:

`minWeight <= weight <= maxWeight`

Например:

`minWeight = 0.10`

`maxWeight = 0.70`

После сглаживания применяется ограничение:

`boundedWeight = min(maxWeight, max(minWeight, newWeight))`

---

### 14.6. Нормировка после ограничения

После применения границ веса нормируются повторно, чтобы сумма в блоке снова была равна `1`.

Например, для `loadBlock`:

`normalizedOccupancyWeight = boundedOccupancyWeight / (boundedOccupancyWeight + boundedUrgencyWeight + boundedInteractionWeight)`

Аналогично — для остальных блоков.

---

### 14.7. Частота пересчета

Рекомендуемый режим:

- `statsWindowDays = 90`
- пересчет агрегатов — каждый день ночью;
- пересчет весов — раз в неделю.

---

## 15. Какие таблицы нужны в базе данных

Ниже приведена рекомендуемая логическая структура таблиц.

### 15.1. `bookings`

Хранит факт брони.

Поля:

- `id`
- `restaurantId`
- `tableId`
- `userId`
- `bookingCreatedAt`
- `visitDateTime`
- `guestCount`
- `hasPreorder`
- `preorderAmount`
- `status`
- `createdAt`
- `updatedAt`

---

### 15.2. `bookingPricingSnapshot`

Хранит снимок расчета сервисного сбора по каждой успешной брони.

Поля:

- `id`
- `bookingId`
- `modelVersionId`
- `loadFactor`
- `urgencyFactor`
- `dayDemandIndex`
- `timeDemandIndex`
- `weatherDemandIndex`
- `holidayDemandIndex`
- `loadBlock`
- `historyBlock`
- `contextBlock`
- `demandIndex`
- `baseServiceFee`
- `finalServiceFee`
- `createdAt`

---

### 15.3. `pricingModelVersion`

Хранит версии модели.

Поля:

- `id`
- `versionName`
- `description`
- `isActive`
- `versionType` (`EXPERT`, `AUTO_ADJUSTED`, `HYBRID`)
- `validFrom`
- `validTo`
- `createdAt`

---

### 15.4. `pricingModelParams`

Хранит параметры конкретной версии модели.

Поля:

- `modelVersionId`
- `occupancyWeight`
- `urgencyWeight`
- `interactionWeight`
- `dayWeight`
- `timeWeight`
- `weatherWeight`
- `holidayWeight`
- `loadBlockWeight`
- `historyBlockWeight`
- `contextBlockWeight`
- `minServiceFee`
- `maxServiceFee`
- `sigmoidSlope`
- `sigmoidCenter`
- `freeServiceFeeThreshold`
- `preorderDiscountPower`
- `learningRate`
- `minWeight`
- `maxWeight`
- `statsWindowDays`
- `updatedAt`

---

### 15.5. `bookingStatsDayOfWeek`

Поля:

- `dayOfWeek`
- `successfulBookingsCount`
- `windowStart`
- `windowEnd`
- `updatedAt`

---

### 15.6. `bookingStatsTimeSlot`

Поля:

- `timeSlot`
- `successfulBookingsCount`
- `windowStart`
- `windowEnd`
- `updatedAt`

---

### 15.7. `bookingStatsWeatherClass`

Поля:

- `weatherClass`
- `successfulBookingsCount`
- `windowStart`
- `windowEnd`
- `updatedAt`

---

### 15.8. `bookingStatsHolidayClass`

Поля:

- `holidayClass`
- `successfulBookingsCount`
- `windowStart`
- `windowEnd`
- `updatedAt`

---

### 15.9. `slotDemandStats`

Поля:

- `visitDate`
- `timeSlot`
- `successfulBookingsCount`
- `realizedDemandScore`
- `windowStart`
- `windowEnd`
- `updatedAt`

---

### 15.10. `modelRecalculationLog`

Хранит историю автокоррекции модели.

Поля:

- `id`
- `modelVersionId`
- `windowStart`
- `windowEnd`
- `oldParamsJson`
- `suggestedParamsJson`
- `appliedParamsJson`
- `recalculationStatus`
- `createdAt`

---

## 16. Как модель обновляется

### 16.1. При каждом успешном бронировании

1. Сохраняется запись в `bookings`.
2. Считаются текущие индексы.
3. Считается `baseServiceFee`.
4. Считается `finalServiceFee`.
5. Сохраняется запись в `bookingPricingSnapshot`.

### 16.2. Ночной job

Раз в сутки:

- пересчитывает статистику по окну `statsWindowDays`;
- обновляет:
    - `bookingStatsDayOfWeek`,
    - `bookingStatsTimeSlot`,
    - `bookingStatsWeatherClass`,
    - `bookingStatsHolidayClass`,
    - `slotDemandStats`.

### 16.3. Еженедельный job

Раз в неделю:

- берет данные из `bookingPricingSnapshot` и `slotDemandStats`;
- считает важности факторов;
- считает `suggested*Weight`;
- сглаживает их через `learningRate`;
- ограничивает по `minWeight/maxWeight`;
- нормирует;
- создает новую запись в `pricingModelVersion`;
- сохраняет новые параметры в `pricingModelParams`;
- помечает новую версию как активную.

---

## 17. Что обновляется автоматически, а что нет

### Автоматически обновляются

- `dayDemandIndex`
- `timeDemandIndex`
- `weatherDemandIndex`
- `holidayDemandIndex`
- веса блоков и подблоков

### Обычно не обновляются автоматически на первом этапе

- `minServiceFee`
- `maxServiceFee`
- `freeServiceFeeThreshold`
- `sigmoidSlope`
- `sigmoidCenter`
- `preorderDiscountPower`

Их лучше оставлять бизнес-параметрами, а не давать им свободно “плавать”.

---

## 18. Полная компактная запись модели

`loadFactor = bookedTablesCount / totalTablesCount`

`urgencyFactor = exp(-hoursBeforeVisit / urgencyScaleHours)`

`dayDemandIndex = successfulBookingsForSelectedDay / maxSuccessfulBookingsByDay`

`timeDemandIndex = successfulBookingsForSelectedTimeSlot / maxSuccessfulBookingsByTimeSlot`

`weatherDemandIndex = (weatherLowDemandMembership * weatherLowClassIndex + weatherMediumDemandMembership * weatherMediumClassIndex + weatherHighDemandMembership * weatherHighClassIndex) / (weatherLowDemandMembership + weatherMediumDemandMembership + weatherHighDemandMembership)`

`holidayDemandIndex = (workdayMembership * workdayClassIndex + weekendMembership * weekendClassIndex + holidayMembership * holidayClassIndex + peakHolidayMembership * peakHolidayClassIndex) / (workdayMembership + weekendMembership + holidayMembership + peakHolidayMembership)`

`loadBlock = occupancyWeight * loadFactor^2 + urgencyWeight * urgencyFactor + interactionWeight * loadFactor * urgencyFactor`

`historyBlock = dayWeight * dayDemandIndex + timeWeight * timeDemandIndex`

`contextBlock = weatherWeight * weatherDemandIndex + holidayWeight * holidayDemandIndex`

`demandIndex = loadBlockWeight * loadBlock + historyBlockWeight * historyBlock + contextBlockWeight * contextBlock`

`baseServiceFee = minServiceFee + (maxServiceFee - minServiceFee) * (1 / (1 + exp(-sigmoidSlope * (demandIndex - sigmoidCenter))))`

`finalServiceFee = 0, if hasPreorder = false`

`finalServiceFee = 0, if preorderAmount >= freeServiceFeeThreshold`

`finalServiceFee = baseServiceFee * (1 - preorderAmount / freeServiceFeeThreshold)^preorderDiscountPower, if 0 < preorderAmount < freeServiceFeeThreshold`

`totalPrice = preorderAmount + finalServiceFee`

`realizedDemandScore = successfulBookingsInVisitSlot / maxSuccessfulBookingsInAnyVisitSlotWithinWindow`

`importance = abs(corr(component, realizedDemandScore))`

`suggestedWeight = componentImportance / sumOfAllComponentImportancesInBlock`

`newWeight = (1 - learningRate) * oldWeight + learningRate * suggestedWeight`

`boundedWeight = min(maxWeight, max(minWeight, newWeight))`

`normalizedWeight = boundedWeight / sumOfBoundedWeightsInBlock`

---

## 19. Сравнение с исходной моделью на вероятности подтверждения брони

### Исходная вероятностная модель

Ее логика была такой:

1. считается индекс ситуации;
2. вводится функция вероятности подтверждения брони;
3. затем сервисный сбор выбирается так, чтобы максимизировать ожидаемую выгоду.

Примерный вид был таким:

`bookingAcceptanceProbability = 1 / (1 + exp(priceSensitivity * (serviceFee - acceptanceThreshold)))`

После этого строилась целевая функция прибыли.

### Плюсы вероятностной модели

- математически выглядит очень сильно;
- напрямую моделирует реакцию пользователя на цену;
- позволяет формально говорить о максимизации ожидаемой прибыли.

### Минусы вероятностной модели

- нужно понимать, что считать отказом;
- нужно собирать незавершенные пользовательские действия;
- нужно отличать технический выход от реального отказа;
- нужны спорные параметры вроде `priceSensitivity` и `acceptanceThreshold`;
- без качественной аналитики такая модель становится слишком условной.

---

### Финальная детерминированная модель

В текущей версии:

- используется только история успешных бронирований;
- оценивается `demandIndex` слота;
- сервисный сбор согласуется с этим индексом;
- веса модели могут автоматически уточняться по фактическому успешному спросу.

### Плюсы финальной модели

- опирается только на достоверные события;
- не требует анализа отказов;
- проще внедряется;
- легче защищается в дипломе;
- адаптируется по мере накопления данных.

### Минусы финальной модели

- не моделирует реакцию пользователя на цену напрямую;
- не дает строгой максимизации ожидаемой прибыли;
- больше подходит для адаптивной тарификации, чем для поведенческой оптимизации.

---

## 20. Итоговый вывод

Для данного проекта финальная модель является более подходящей, чем исходная вероятностная модель, потому что:

- она не требует сложной аналитики пользовательского поведения;
- она строится на реально доступных данных;
- она сохраняет математическую содержательность;
- она позволяет постепенно улучшать веса модели автоматически;
- она подходит и для дипломного обоснования, и для практической реализации.

Итоговая интерпретация такая:

> сервисный сбор в данной модели — это адаптивная функция от интегральной оценки востребованности и нагрузки бронируемого слота, а не инструмент прямого расчета вероятности подтверждения брони пользователем.
