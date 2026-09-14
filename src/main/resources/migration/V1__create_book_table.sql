CREATE TABLE book{
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(100) NOT NULL,
    author          VARCHAR(50) NOT NULL,
    isbn            VARCHAR(20) NOT NULL,
    price           DECIMAL(10,2) NOT NULL,
    stock           INT NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'ON_SALE'
    publish_data    DATA,
    created_at      DATETIME NOT null,
    update_at       DARATIME NOT NULL,
    CONSTRAINT      uk_book)isbn UNIQUE (isbn),
    CONSTRAINT      ck_book_price CHECK (price >= 0),
    CONSTRAINT      ck_book_stock CHECK (stock  >= 0)
    }