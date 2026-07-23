package ru.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AggregatorServiceImpl implements AggregatorService {
    @Value("${application.action-weight.view}")
    private Double view;
    @Value("${application.action-weight.register}")
    private Double register;
    @Value("${application.action-weight.like}")
    private Double like;

    private final Map<Long, Map<Long, Double>> eventUserWeight = new HashMap<>();
    private final Map<Long, Double> eventWeightSum = new HashMap<>();
    private final Map<Long, Map<Long, Double>> twoEventsMinSum = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> aggregationUserAction(SpecificRecordBase userActionAvro) {
        UserActionAvro action = (UserActionAvro) userActionAvro;
        List<EventSimilarityAvro> result = new ArrayList<>();
        Double weightDiff = getDiffEventUserWeight(action);
        if (weightDiff.equals(0.0)) {
            return new ArrayList<>();
        }
        updateEventUserWeight(action);
        updateEventWeightSum(action, weightDiff);

        List<Long> eventIdsForCalculate = new ArrayList<>();
        for (Long id : eventUserWeight.keySet()) {
            if (!id.equals(action.getEventId())) {
                Double otherEventUserWeight = eventUserWeight.get(id).get(action.getUserId());
                if (otherEventUserWeight != null && otherEventUserWeight != 0) {
                    eventIdsForCalculate.add(id);
                }
            }
        }
        if (eventIdsForCalculate.isEmpty()) {
            return new ArrayList<>();
        }

        updateTwoEventsMinSum(action, weightDiff, eventIdsForCalculate);

        for (Long id : eventIdsForCalculate) {
            Long first = Math.min(action.getEventId(), id);
            Long second = Math.max(action.getEventId(), id);

            double similarity = twoEventsMinSum.get(first).get(second) /
                    (Math.sqrt(eventWeightSum.get(first)) * Math.sqrt(eventWeightSum.get(second)));
            EventSimilarityAvro eventSimilarity = EventSimilarityAvro.newBuilder()
                    .setEventA(first)
                    .setEventB(second)
                    .setScore(similarity)
                    .setTimestamp(action.getTimestamp())
                    .build();
            result.add(eventSimilarity);
        }

        return result;
    }

    private void updateEventWeightSum(UserActionAvro action, Double weightDiff) {
        Long userId = action.getUserId();
        Long eventId = action.getEventId();
        Double eventWeight = eventUserWeight.get(eventId).getOrDefault(userId, 0.0);
        if (weightDiff.equals(0.0)) {
            return;
        }
        if (!eventWeightSum.containsKey(eventId)) {
            eventWeightSum.put(eventId, eventWeight);
            return;
        }
        eventWeightSum.merge(eventId, weightDiff, Double::sum);
    }

    private void updateTwoEventsMinSum(UserActionAvro action, Double diffWeight, List<Long> eventIdsForCalculate) {
        Long userId = action.getUserId();
        long eventId = action.getEventId();
        Double eventWeight = eventUserWeight.get(eventId).getOrDefault(userId, 0.0);

        if (eventWeight.equals(0.0) || diffWeight.equals(0.0)) {
            return;
        }
        Double oldEventWeight = eventWeight - diffWeight;

        for (Long otherEventId : eventIdsForCalculate) {
            Double otherEventWeight = eventUserWeight.get(otherEventId).getOrDefault(userId, 0.0);
            if (otherEventWeight.equals(0.0)) {
                continue;
            }

            Long first = Math.min(eventId, otherEventId);
            Long second = Math.max(eventId, otherEventId);
            Map<Long, Double> map = twoEventsMinSum.get(first);
            if (map == null || map.isEmpty()) {
                twoEventsMinSum.computeIfAbsent(first, k -> new HashMap<>())
                        .put(second, Math.min(eventWeight, otherEventWeight));
               continue;
            }
            Double oldSum = map.get(second);
            if (oldSum == null) {
                twoEventsMinSum.computeIfAbsent(first, k -> new HashMap<>())
                        .put(second, Math.min(eventWeight, otherEventWeight));
                continue;
            }

            if (eventWeight >= otherEventWeight) {
                if (oldEventWeight >= otherEventWeight) {
                    continue;
                } else {
                    oldSum += otherEventWeight - oldEventWeight;
                }
            } else {
                oldSum += eventWeight - oldEventWeight;
            }
            twoEventsMinSum.computeIfAbsent(first, k -> new HashMap<>())
                    .put(second, oldSum);
        }
    }

    private Double getDiffEventUserWeight(UserActionAvro action) {
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        Double weight = getWeight(action.getActionType());

        Map<Long, Double> oldUserWeight = eventUserWeight.get(eventId);
        if (oldUserWeight == null || oldUserWeight.isEmpty()) {
            return weight;
        }
        Double oldWeight = oldUserWeight.get(userId);
        if (oldWeight == null || oldWeight == 0) {
            return weight;
        }
        if (oldWeight >= weight) {
            return 0.0;
        }
        return weight - oldWeight;
    }

    private void updateEventUserWeight(UserActionAvro action) {
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        Double weight = getWeight(action.getActionType());

        Map<Long, Double> oldUserWeight = eventUserWeight.get(eventId);
        if (oldUserWeight == null || oldUserWeight.isEmpty()) {
            eventUserWeight.computeIfAbsent(eventId, k -> new HashMap<>()).put(userId, weight);
            return;
        }
        Double oldWeight = oldUserWeight.get(userId);
        if (oldWeight == null || oldWeight == 0) {
            oldUserWeight.put(userId, weight);
            return;
        }
        if (oldWeight >= weight) {
            return;
        }
        oldUserWeight.put(userId, weight);
    }

    private Double getWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> view;
            case REGISTER -> register;
            case LIKE -> like;
        };
    }
}