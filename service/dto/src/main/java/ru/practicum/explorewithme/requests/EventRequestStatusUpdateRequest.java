package ru.practicum.explorewithme.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.events.utils.EventStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EventRequestStatusUpdateRequest {
    @NotNull(message = "Field: requestIds. Error: must not be blank. Value: null")
    private List<Long> requestIds;

    @NotNull(message = "Field: status. Error: must not be blank. Value: null")
    private EventStatus status;
}
