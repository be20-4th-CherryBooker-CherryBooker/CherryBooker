package com.cherry.cherrybookerbe.mylib.query.service;

import com.cherry.cherrybookerbe.common.enums.Status;
import com.cherry.cherrybookerbe.common.exception.BadRequestException;
import com.cherry.cherrybookerbe.common.exception.ResourceNotFoundException;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;
import com.cherry.cherrybookerbe.mylib.query.dto.request.MyLibrarySearchRequest;
import com.cherry.cherrybookerbe.mylib.query.dto.response.MyBookCardResponse;
import com.cherry.cherrybookerbe.mylib.query.dto.response.MyBookDetailResponse;
import com.cherry.cherrybookerbe.mylib.query.dto.response.MyLibrarySliceResponse;
import com.cherry.cherrybookerbe.mylib.query.dto.response.QuoteSnippetResponse;
import com.cherry.cherrybookerbe.quote.query.repository.QuoteQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyLibSearchService {

    private static final String EMPTY_NOTICE = "더 이상 불러올 도서가 없습니다.";

    @PersistenceContext
    private EntityManager entityManager;

    private final QuoteQueryRepository quoteQueryRepository;

    // 나의 서재 검색
    // ToDo: Authenticated user만 받도록 리팩토링이 필요 
    public MyLibrarySliceResponse getMyLibrary(MyLibrarySearchRequest request) {
        if (request.getUserId() == null) {
            throw new BadRequestException("userId는 필수입니다.");
        }
        LibraryQueryHelper helper = new LibraryQueryHelper(request);
        long totalCount = helper.fetchCount();
        List<MyLib> page = helper.fetchPage();

        List<MyBookCardResponse> books = page.stream()
                .map(MyBookCardResponse::from)
                .collect(Collectors.toList());

        boolean hasMore = (long) (request.getPage() + 1) * request.getSize() < totalCount;
        int dummySlots = totalCount == 0
                ? request.getSize()
                : (int) ((request.getSize() - (totalCount % request.getSize())) % request.getSize());

        return new MyLibrarySliceResponse(
                totalCount,
                request.getPage(),
                request.getSize(),
                hasMore,
                books,
                dummySlots,
                books.isEmpty() ? EMPTY_NOTICE : null
        );
    }

    // 나의 서재에 등록되어 있는 책의 세부 정보 조회
    public MyBookDetailResponse getBookDetail(Long myLibId) {
        MyLib myLib = fetchMyLib(myLibId);
        List<QuoteSnippetResponse> quotes = quoteQueryRepository
                .findByUserBookIdAndStatusOrderByCreatedAtDesc(myLibId, Status.Y)
                .stream()
                .map(QuoteSnippetResponse::from)
                .toList();

        return new MyBookDetailResponse(
                myLib.getMyLibId(),
                myLib.getBook().getBookId(),
                myLib.getBookStatus(),
                myLib.getBook().getTitle(),
                myLib.getBook().getAuthor(),
                myLib.getBook().getCoverImageUrl(),
                quotes,
                myLib.getBookStatus() == BookStatus.READ
        );
    }

    private MyLib fetchMyLib(Long myLibId) {
        TypedQuery<MyLib> query = entityManager.createQuery(
                "select ml from MyLib ml join fetch ml.book b where ml.myLibId = :myLibId",
                MyLib.class
        );
        query.setParameter("myLibId", myLibId);
        return query.getResultStream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("해당 서재 도서를 찾을 수 없습니다."));
    }

    private class LibraryQueryHelper {
        private final MyLibrarySearchRequest request;

        private LibraryQueryHelper(MyLibrarySearchRequest request) {
            this.request = request;
        }

        private String baseCondition() {
            StringBuilder builder = new StringBuilder(" where ml.user.userId = :userId");
            if (request.getStatus() != null) {
                builder.append(" and ml.bookStatus = :status");
            }
            if (StringUtils.hasText(request.getKeyword())) {
                builder.append(" and (lower(b.title) like :keyword or lower(b.author) like :keyword)");
            }
            return builder.toString();
        }

        private void assignParameters(TypedQuery<?> query) {
            query.setParameter("userId", Math.toIntExact(request.getUserId()));
            if (request.getStatus() != null) {
                query.setParameter("status", request.getStatus());
            }
            if (StringUtils.hasText(request.getKeyword())) {
                query.setParameter("keyword", "%" + request.getKeyword().trim().toLowerCase() + "%");
            }
        }

        private long fetchCount() {
            String jpql = "select count(ml) from MyLib ml join ml.book b" + baseCondition();
            TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
            assignParameters(query);
            return query.getSingleResult();
        }

        private List<MyLib> fetchPage() {
            String jpql = "select ml from MyLib ml join fetch ml.book b" + baseCondition() + " order by ml.createdAt desc";
            TypedQuery<MyLib> query = entityManager.createQuery(jpql, MyLib.class);
            assignParameters(query);
            query.setFirstResult(request.getPage() * request.getSize());
            query.setMaxResults(request.getSize());
            return query.getResultList();
        }
    }
}
