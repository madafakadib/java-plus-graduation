package ru.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.messages.ActionTypeProto;
import ru.practicum.ewm.stats.messages.UserActionProto;

import java.time.Instant;

@Component
public class UserActionMapper {
    public UserActionAvro mapToAvro(UserActionProto action) {
        UserActionAvro result = UserActionAvro.newBuilder()
                .setUserId(action.getUserId())
                .setEventId(action.getEventId())
                .setActionType(getActionType(action.getActionType()))
                .setTimestamp(Instant.ofEpochSecond(
                        action.getTimestamp().getSeconds(),
                        action.getTimestamp().getNanos()))
                .build();
        return result;
    }

    private ActionTypeAvro getActionType(ActionTypeProto actionTypeProto) {
        return switch (actionTypeProto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case UNRECOGNIZED -> null;
        };
    }
}