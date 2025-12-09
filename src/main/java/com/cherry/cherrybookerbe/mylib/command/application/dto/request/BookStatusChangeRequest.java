package com.cherry.cherrybookerbe.mylib.command.application.dto.request;

import com.cherry.cherrybookerbe.mylib.command.application.dto.StatusTrigger;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import jakarta.validation.constraints.NotNull;

public record BookStatusChangeRequest(
        @NotNull BookStatus targetStatus,
        StatusTrigger trigger
) {

    public StatusTrigger triggerOrDefault() {
        return trigger == null ? StatusTrigger.MANUAL : trigger;
    }
}
