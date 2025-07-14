-- Create tokens_v2 table
CREATE TABLE IF NOT EXISTS tokens_v2 (
    id BIGSERIAL PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    expired BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES sellers(id)
        ON DELETE CASCADE
);

-- Create index for faster lookups
CREATE INDEX idx_tokens_v2_token ON tokens_v2(token);
CREATE INDEX idx_tokens_v2_user_id ON tokens_v2(user_id);
