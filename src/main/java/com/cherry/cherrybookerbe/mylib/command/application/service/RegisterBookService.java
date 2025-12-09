package com.cherry.cherrybookerbe.mylib.command.application.service;

import com.cherry.cherrybookerbe.common.exception.ResourceNotFoundException;
import com.cherry.cherrybookerbe.mylib.command.application.dto.request.RegisterBookRequest;
import com.cherry.cherrybookerbe.mylib.command.application.dto.response.RegisterBookResponse;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.Book;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;
import com.cherry.cherrybookerbe.mylib.command.domain.repository.MyLibRepository;
import com.cherry.cherrybookerbe.mylib.command.domain.service.RegisterNewBookService;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import com.cherry.cherrybookerbe.user.command.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterBookService {

    private final UserRepository userRepository;
    private final RegisterNewBookService registerNewBookService;
    private final MyLibRepository myLibRepository;

    public RegisterBookResponse register(RegisterBookRequest request) {
        User user = userRepository.findById(Math.toIntExact(request.userId()))
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        Book book = registerNewBookService.findOrCreate(request.keyword(), request.isbnHint());

        return myLibRepository.findByUserUserIdAndBookBookId(user.getUserId(), book.getBookId())
                .map(existing -> RegisterBookResponse.of(existing, false))
                .orElseGet(() -> RegisterBookResponse.of(createMyLib(user, book), true));
    }

    private MyLib createMyLib(User user, Book book) {
        MyLib myLib = MyLib.builder()
                .user(user)
                .book(book)
                .bookStatus(BookStatus.WISH)
                .build();
        return myLibRepository.save(myLib);
    }
}
