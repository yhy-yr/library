package org.example.library.service;

import org.example.library.dto.BorrowDetailResponse;
import org.example.library.dto.BorrowRequest;
import org.example.library.entity.*;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.BookRepository;
import org.example.library.repository.BorrowRecordRepository;
import org.example.library.repository.ReaderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BorrowService {

    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    public BorrowService(ReaderRepository readerRepository, BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository) {
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }
    @Transactional
    public void borrow(BorrowRequest request) {

        if (request.bookId() == null
                || request.readerId() == null) {
            throw new IllegalArgumentException(
                    "读者 ID 和图书 ID 不能为空"
            );
        }

        Reader reader = readerRepository
                .findById(request.readerId())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "读者不存在"
                        )
                );

        if (reader.getStatus() != ReaderStatus. ACTIVE) {
            throw new IllegalArgumentException("读者已停用");
        }
        long borrowedCount =
                borrowRecordRepository.countBorrowedByReaderId(request.readerId());

        if (borrowedCount >= 5) {
            throw new IllegalArgumentException(
                    "每位读者最多同时借阅 5 本图书"
            );
        }
        Book book = bookRepository
                .findById(request.bookId())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "图书不存在"
                        )
                );

        if (book.getStatus() != BookStatus.ON_SALE) {
            throw new IllegalArgumentException("图书已经下架");
        }
        boolean alreadyBorrowed =
                borrowRecordRepository.existsBorrowedRecord(
                        request.readerId(),
                        request.bookId()
                );

        if (alreadyBorrowed) {
            throw new IllegalArgumentException(
                    "不能重复借阅同一本图书"
            );
        }
        int affectedRows = bookRepository.changeStock(
                request.bookId(),
                -1
        );

        if (affectedRows == 0) {
            throw new IllegalArgumentException("图书库存不足");
        }


        borrowRecordRepository.save(
                request.readerId(),
                request.bookId()
        );
    }
    @Transactional
    public void returnBook(Long borrowId) {

        BorrowRecord record = borrowRecordRepository
                .findById(borrowId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "借阅记录不存在"
                        )
                );

        if (record.status() != BorrowStatus. BORROWED) {
            throw new IllegalArgumentException(
                    "该借阅记录已经归还"
            );
        }
        int affectedRows = borrowRecordRepository.markReturned(
                borrowId
        );

        if (affectedRows == 0) {
            throw new IllegalArgumentException(
                    "借阅记录已被归还"
            );
        }

        int stockAffectedRows = bookRepository.changeStock(
                record.bookId(),+1

        );

        if (stockAffectedRows == 0) {
            throw new IllegalStateException(
                    "归还失败：对应图书不存在"
            );
        }

    }
    public List<BorrowRecord> getByReaderId(Long readerId) {
        ensureReaderExists(readerId);
        return borrowRecordRepository.findByReaderId(readerId);
    }
    public List<BorrowRecord> getOverdueRecords() {
        return borrowRecordRepository.findOverdueRecords();
    }
    public List<BorrowDetailResponse> getDetailsByReaderId(
            Long readerId
    ) {
        ensureReaderExists(readerId);

        return borrowRecordRepository.findDetailsByReaderId(readerId);

    }
    private void ensureReaderExists(Long readerId) {
        readerRepository.findById(readerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "读者不存在"
                        )
                );
    }





}