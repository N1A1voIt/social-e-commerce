CREATE TABLE amount_distance(
    id_amount_distance SERIAL,
    price_per_distance NUMERIC(15,2) NOT NULL,
    id_user INTEGER,
    id_mp INTEGER,
    PRIMARY KEY(id_amount_distance),
    FOREIGN KEY(id_user) REFERENCES users(id_user),
    FOREIGN KEY(id_mp) REFERENCES managed_pages(id_mp),
    CHECK (id_user IS NOT NULL OR id_mp IS NOT NULL)
);
