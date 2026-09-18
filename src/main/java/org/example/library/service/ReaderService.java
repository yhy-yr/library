package org.example.library.service;

import org.example.library.entity.Reader;
import org.example.library.exception.ConflictException;
import org.example.library.repository.ReaderRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class ReaderService {

    private final ReaderRepository readerRepository;

    public ReaderService(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }
    public int create(Reader reader) {
        validateReader(reader);

        try {
            return readerRepository.save(reader);
        } catch (DuplicateKeyException exception) {
            throw new ConflictException(
                    "手机号已存在",
                    exception
            );
        }
    }
    private void validateReader(Reader reader) {
        if (reader.getName() == null
                || reader.getName().isBlank()) {
            throw new IllegalArgumentException("读者姓名不能为空");
        }

        if (reader.getPhone() == null
                || reader.getPhone().isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }

        if (reader.getStatus() == null) {
            throw new IllegalArgumentException("读者状态不能为空");
        }
    }
}
