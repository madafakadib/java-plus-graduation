package ru.practicum.stat.client;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.common.dto.participationRequest.RegistrationRequest;
import ru.practicum.ewm.stats.messages.ActionTypeProto;
import ru.practicum.ewm.stats.messages.UserActionBatchProto;
import ru.practicum.ewm.stats.messages.UserActionProto;
import ru.practicum.ewm.stats.services.UserActionControllerGrpc;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    public void sendView(Long userId, Long eventId) {
        UserActionProto request = buildUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
        collectorClient.collectUserAction(request);
    }

    public void sendLike(Long userId, Long eventId) {
        UserActionProto request = buildUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
        collectorClient.collectUserAction(request);
    }

    public void sendRegistration(Long userId, Long eventId) {
        UserActionProto request = buildUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
        collectorClient.collectUserAction(request);
    }

    public void sendRegistrationsBatch(List<RegistrationRequest> registrations) {
        if (registrations == null || registrations.isEmpty()) {
            return;
        }

        List<UserActionProto> actions = registrations.stream()
                .map(r -> buildUserAction(
                        r.getUserId(),
                        r.getEventId(),
                        ActionTypeProto.ACTION_REGISTER
                ))
                .collect(Collectors.toList());

        try {
            UserActionBatchProto batchRequest = UserActionBatchProto.newBuilder()
                    .addAllActions(actions)
                    .build();
            collectorClient.collectUserActionsBatch(batchRequest);
        } catch (StatusRuntimeException e) {
            for (RegistrationRequest reg : registrations) {
                try {
                    sendRegistration(reg.getUserId(), reg.getEventId());
                } catch (Exception ex) {
                }
            }
        }
    }

    private UserActionProto buildUserAction(Long userId, Long eventId, ActionTypeProto actionType) {
        Instant now = Instant.now();
        return UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build();
    }
}