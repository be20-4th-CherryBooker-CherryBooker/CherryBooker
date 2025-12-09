package com.cherry.cherrybookerbe.mylib.command.application.service;

import com.cherry.cherrybookerbe.common.exception.BadRequestException;
import com.cherry.cherrybookerbe.common.exception.ResourceNotFoundException;
import com.cherry.cherrybookerbe.mylib.command.application.dto.request.BookStatusChangeRequest;
import com.cherry.cherrybookerbe.mylib.command.application.dto.response.BookStatusChangeResponse;
import com.cherry.cherrybookerbe.mylib.command.application.dto.StatusTrigger;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;
import com.cherry.cherrybookerbe.mylib.command.domain.repository.MyLibRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookStatusService {

    private final MyLibRepository myLibRepository;

    public BookStatusChangeResponse changeStatus(Long myLibId, BookStatusChangeRequest request) {
        MyLib myLib = myLibRepository.findWithBookByMyLibId(myLibId)
                .orElseThrow(() -> new ResourceNotFoundException("등록된 서재 도서를 찾을 수 없습니다."));

        BookStatus currentStatus = myLib.getBookStatus();
        BookStatus targetStatus = request.targetStatus();
        StatusTrigger trigger = request.triggerOrDefault();

        if (currentStatus == targetStatus) {
            return BookStatusChangeResponse.of(myLib, currentStatus, false, trigger);
        }

        validateStateTransition(currentStatus, targetStatus, trigger);
        myLib.changeBookStatus(targetStatus);

        return BookStatusChangeResponse.of(myLib, currentStatus, true, trigger);
    }

    private void validateStateTransition(BookStatus currentStatus,
                                         BookStatus targetStatus,
                                         StatusTrigger trigger) {
        boolean isValid =
                (currentStatus == BookStatus.WISH && targetStatus == BookStatus.READING) ||
                        (currentStatus == BookStatus.READING && targetStatus == BookStatus.READ) ||
                        (currentStatus == BookStatus.WISH && targetStatus == BookStatus.READ);

        if (!isValid) {
            throw new BadRequestException("허용되지 않는 상태 전환입니다.");
        }

        if (targetStatus == BookStatus.READING && trigger == StatusTrigger.MANUAL) {
            throw new BadRequestException("읽는 중 상태는 글귀 등록을 통해서만 변경됩니다.");
        }
    }
}
