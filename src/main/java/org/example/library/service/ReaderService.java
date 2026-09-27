package org.example.library.service;

import org.example.library.entity.Reader;
import org.example.library.entity.ReaderStatus;
import org.example.library.exception.ConflictException;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.ReaderRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        // 创建读者：先检查姓名、手机号。
        validateReaderDetails(reader);

        // 创建读者还必须有初始状态。
        if (reader.getStatus() == null) {
            throw new IllegalArgumentException("读者状态不能为空");
        }
    }

    private void validateReaderDetails(Reader reader) {
        if (reader.getName() == null
                || reader.getName().isBlank()) {
            throw new IllegalArgumentException("读者姓名不能为空");
        }

        if (reader.getPhone() == null
                || reader.getPhone().isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
    }

    @Transactional
    public void updateStatus(
            Long id,
            ReaderStatus status
    ) {
        int affectedRows = readerRepository.updateStatus(id, status);

        if (affectedRows == 0) {
            throw new ResourceNotFoundException(
                    "读者不存在"
            );
        }
    }
    public Optional<Reader> getById(Long id) {
        return readerRepository.findById(id);
    }
    public List<Reader> getAll() {
        return readerRepository.findAll();
    }
    public int update(Long id, Reader reader) {
        // 更新资料只校验姓名和手机号，不要求 status。
        validateReaderDetails(reader);
        // 目标读者由 URL 中的 ID 决定。
        reader.setId(id);

        try {
            return readerRepository.update(reader);
        } catch (DuplicateKeyException exception) {
            throw new ConflictException(
                    "手机号已存在",
                    exception
            );
        }
    }

}
