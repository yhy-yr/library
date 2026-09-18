CREATE TABLE reader
(
        id          BIGINT AUTO_INCREMENT PRIMARY KEY,
        name        VARCHAR(50) NOT NULL,
        phone       VARCHAR(20) NOT NULL,
        status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
        created_at  DATETIME NOT NULL,
        updated_at  DATETIME   NOT NULL,
        CONSTRAINT uk_reader_phone UNIQUE (phone)
);
CREATE TABLE borrow_record
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    reader_id   BIGINT NOT NULL,
    book_id     BIGINT NOT NULL,
    borrowed_at DATETIME NOT NULL,
    due_at      DATETIME NOT NULL,
    returned_at DATETIME,
    status      VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL,

    CONSTRAINT fk_borrow_reader
        FOREIGN KEY (reader_id) REFERENCES reader(id),

    CONSTRAINT fk_borrow_book
        FOREIGN KEY (book_id) REFERENCES book(id)
);