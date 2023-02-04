CREATE TABLE IF NOT EXISTS order_exam (
    id uuid DEFAULT uuid_generate_v4 () NOT NULL,
    collecton_id VARCHAR,
    status VARCHAR,
    PRIMARY KEY(id),
    CONSTRAINT fk_collecton_id
        FOREIGN KEY (collecton_id)
            REFERENCES claim(collecton_id)
);