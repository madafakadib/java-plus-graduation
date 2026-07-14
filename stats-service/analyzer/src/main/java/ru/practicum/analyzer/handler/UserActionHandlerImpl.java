package ru.practicum.analyzer.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.mapper.UserActionMapper;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
public class UserActionHandlerImpl implements UserActionHandler {

    private final UserActionRepository actionRepository;
    private final UserActionMapper actionMapper;

    @Override
    public void handleUserAction(UserActionAvro avro) {
        UserAction action = actionMapper.mapToUserAction(avro);

        UserAction oldAction = actionRepository
                .findByEventIdAndUserId(action.getEventId(), action.getUserId())
                .orElse(null);

        if (oldAction == null) {
            actionRepository.save(action);
        } else {
            if (action.getWeight() > oldAction.getWeight()) {
                oldAction.setWeight(action.getWeight());
                oldAction.setTimestamp(action.getTimestamp());
                actionRepository.save(oldAction);
            }
        }
    }
}