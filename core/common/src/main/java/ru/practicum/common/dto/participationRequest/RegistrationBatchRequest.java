package ru.practicum.common.dto.participationRequest;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationBatchRequest {
    private List<RegistrationRequest> registrations;
}