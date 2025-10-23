CREATE DATABASE postgres;
CREATE TYPE platforms_pro as ENUM ('google', 'facebook','basic','X','github');

CREATE TABLE seller_v2(
                          id_seller SERIAL,
                          email TEXT NOT NULL,
                          username TEXT NOT NULL,
                          id_provider platforms_pro,
                          phone_number TEXT,
                          firebase_uid TEXT NOT NULL,
                          PRIMARY KEY(id_seller),
                          UNIQUE(email)
);

CREATE TABLE tokens_v2(
                          id_token SERIAL,
                          token TEXT NOT NULL,
                          expired_at TIMESTAMP,
                          id_seller INTEGER NOT NULL,
                          PRIMARY KEY(id_token),
                          FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller)
);

CREATE TABLE supported_platforms_v2(
   id_sp SERIAL,
   label VARCHAR(250) ,
   PRIMARY KEY(id_sp)
);
INSERT INTO supported_platforms_v2 (label) VALUES ( 'facebook');
INSERT INTO supported_platforms_v2 (label) VALUES ( 'instagram');
INSERT INTO supported_platforms_v2 (label) VALUES ( 'x');
INSERT INTO supported_platforms_v2 (label) VALUES ( 'thread');

CREATE TABLE managed_pages(
      id_mp SERIAL,
      d_status VARCHAR(50) ,
      platform_identifier TEXT NOT NULL,
      page_title TEXT NOT NULL,
      associated_media TEXT,
      link_to_platform TEXT NOT NULL,
      id_sp INTEGER NOT NULL,
      id_seller INTEGER NOT NULL,
      PRIMARY KEY(id_mp),
      FOREIGN KEY(id_sp) REFERENCES supported_platforms_v2(id_sp),
      FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller)
);

CREATE TABLE platform_status(
                                id_status SERIAL,
                                status_labem VARCHAR(50)  NOT NULL,
                                PRIMARY KEY(id_status)
);
INSERT INTO platform_status (status_labem) VALUES ('active');
INSERT INTO platform_status (status_labem) VALUES ('inactive');

CREATE TABLE posts(
                      id_post SERIAL,
                      type VARCHAR(50) ,
                      create_at TIMESTAMP NOT NULL,
                      id_seller INTEGER NOT NULL,
                      PRIMARY KEY(id_post),
                      FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller)
);

CREATE TABLE post_childs(
                            id_child SERIAL,
                            post_url TEXT NOT NULL,
                            media_url TEXT,
                            description TEXT,
                            platform_identifier TEXT NOT NULL,
                            type TEXT,
                            id_sp INTEGER NOT NULL,
                            id_child_1 INTEGER,
                            id_post INTEGER NOT NULL,
                            PRIMARY KEY(id_child),
                            UNIQUE(platform_identifier),
                            FOREIGN KEY(id_sp) REFERENCES supported_platforms_v2(id_sp),
                            FOREIGN KEY(id_child_1) REFERENCES post_childs(id_child),
                            FOREIGN KEY(id_post) REFERENCES posts(id_post)
);

CREATE TABLE potential_customers_v2(
                                       id_pc TEXT,
                                       name TEXT NOT NULL,
                                       link_to_profile TEXT,
                                       d_platform VARCHAR(50) ,
                                       identifier_on_platform VARCHAR(50)  NOT NULL,
                                       media_url TEXT,
                                       id_sp INTEGER NOT NULL,
                                       PRIMARY KEY(id_pc),
                                       FOREIGN KEY(id_sp) REFERENCES supported_platforms_v2(id_sp)
);

CREATE TABLE likes_history(
                              id_lh SERIAL,
                              created_at TIMESTAMP NOT NULL,
                              reactions INTEGER,
                              id_child INTEGER,
                              id_pc TEXT NOT NULL,
                              PRIMARY KEY(id_lh),
                              FOREIGN KEY(id_child) REFERENCES post_childs(id_child),
                              FOREIGN KEY(id_pc) REFERENCES potential_customers_v2(id_pc)
);

CREATE TABLE comments_v2(
                            id_comment TEXT,
                            message TEXT NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            deleted BOOLEAN,
                            id_child INTEGER NOT NULL,
                            PRIMARY KEY(id_comment),
                            FOREIGN KEY(id_child) REFERENCES post_childs(id_child)
);

CREATE TABLE inbox(
                      id_im SERIAL,
                      id_mp INTEGER NOT NULL,
                      PRIMARY KEY(id_im),
                      FOREIGN KEY(id_mp) REFERENCES managed_pages(id_mp)
);

CREATE TABLE message_mother(
                               id_mm SERIAL,
                               id_pc TEXT NOT NULL,
                               id_mp INTEGER NOT NULL,
                               id_im INTEGER NOT NULL,
                               PRIMARY KEY(id_mm),
                               FOREIGN KEY(id_pc) REFERENCES potential_customers_v2(id_pc),
                               FOREIGN KEY(id_mp) REFERENCES managed_pages(id_mp),
                               FOREIGN KEY(id_im) REFERENCES inbox(id_im)
);

CREATE TABLE message_child(
                              id_mc SERIAL,
                              message TEXT NOT NULL,
                              from_platform BOOLEAN NOT NULL,
                              id_mm INTEGER NOT NULL,
                              PRIMARY KEY(id_mc),
                              FOREIGN KEY(id_mm) REFERENCES message_mother(id_mm)
);

CREATE TABLE category (
    id_category SERIAL,
    val TEXT NOT NULL,
    desc_ TEXT,
    embedding VECTOR(384),
    PRIMARY KEY(id_category)
);

CREATE TABLE temporary_product(
    id_temp_product SERIAL,
    description TEXT,
    name TEXT NOT NULL,
    price NUMERIC(18,2)   NOT NULL,
    media TEXT,
    id_category INTEGER NOT NULL,
    id_seller INTEGER NOT NULL,
    state BOOLEAN,
    PRIMARY KEY(id_temp_product),
    FOREIGN KEY(id_category) REFERENCES category(id_category),
    FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller)
);
/*
CREATE TABLE temp_product_state(
    id_temp_product INTEGER NOT NULL,
    state VARCHAR(50) ,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY(id_temp_product, state),
    FOREIGN KEY(id_temp_product) REFERENCES temporary_product(id_temp_product)
);*/

CREATE TABLE products_v2(
                            id_product SERIAL,
                            description TEXT,
                            name TEXT NOT NULL,
                            price NUMERIC(18,2)   NOT NULL,
                            created_at TIMESTAMP,
                            updated_at TIMESTAMP,
                            media TEXT,
                            id_category INTEGER NOT NULL,
                            id_seller INTEGER NOT NULL,
                            PRIMARY KEY(id_product),
                            FOREIGN KEY(id_category) REFERENCES category(id_category),
                            FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller)
);

CREATE TABLE variants_v2(
                            id_variant SERIAL,
                            title TEXT NOT NULL,
                            price NUMERIC(18,2)   NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP,
                            id_product INTEGER NOT NULL,
                            PRIMARY KEY(id_variant),
                            FOREIGN KEY(id_product) REFERENCES products_v2(id_product)
);

CREATE TABLE options_v2(
                           id_option SERIAL,
                           label TEXT NOT NULL,
                           id_product INTEGER NOT NULL,
                           PRIMARY KEY(id_option),
                           FOREIGN KEY(id_product) REFERENCES products_v2(id_product)
);

CREATE TABLE options_values_v2(
                                  id_ov SERIAL ,
                                  value_ TEXT NOT NULL,
                                  id_option INTEGER NOT NULL,
                                  PRIMARY KEY(id_ov),
                                  FOREIGN KEY(id_option) REFERENCES options_v2(id_option)
);

CREATE TABLE variant_option_values_v2(
                                         id SERIAL,
                                         id_ov INTEGER  NOT NULL,
                                         id_variant INTEGER NOT NULL,
                                         PRIMARY KEY(id),
                                         FOREIGN KEY(id_ov) REFERENCES options_values_v2(id_ov),
                                         FOREIGN KEY(id_variant) REFERENCES variants_v2(id_variant)
);

CREATE TABLE order_mother(
                             id_order_m SERIAL,
                             description TEXT,
                             created_at TIMESTAMP NOT NULL,
                             d_total NUMERIC(18,2)  ,
                             d_customer_name TEXT,
                             d_status VARCHAR(50) ,
                             shipping_address TEXT,
                             customer_number VARCHAR(50) ,
                             id_pc TEXT NOT NULL,
                             id_cart INTEGER,
                             id_customer INTEGER,
                             id_managed_pages INTEGER NOT NULL,
                             PRIMARY KEY(id_order_m),
                             FOREIGN KEY(id_managed_pages) REFERENCES managed_pages(id_mp),
                             FOREIGN KEY(id_pc) REFERENCES potential_customers_v2(id_pc),
                             FOREIGN KEY(id_cart) REFERENCES cart(id_cart),
                             FOREIGN KEY(id_customer) REFERENCES customer(id_customer)
);

CREATE TABLE order_status_v2(
                                id_status SERIAL,
                                label TEXT,
                                PRIMARY KEY(id_status)
);

/*
CREATE TABLE product_creation_sessions (
   id SERIAL,
   user_id INTEGER NOT NULL,
   session_id VARCHAR(100) NOT NULL UNIQUE,
   redis_key VARCHAR(200) NOT NULL,
   current_step INTEGER DEFAULT 1,
   completed_steps JSON, -- [1, 2, 3]
   form_data JSON, -- Complete form data as backup
   created_at TIMESTAMP DEFAULT NOW(),
   updated_at TIMESTAMP DEFAULT NOW() ON UPDATE NOW(),
   status ENUM('active', 'completed', 'abandoned') DEFAULT 'active',
   PRIMARY KEY(id),
   INDEX idx_user_status (user_id, status),
   INDEX idx_updated (updated_at)
);*/


CREATE TABLE order_details_v2(
                                 id_order_details SERIAL,
                                 price NUMERIC(18,2)   NOT NULL,
                                 quantity NUMERIC(15,2)  ,
                                 id_product INTEGER NOT NULL,
                                 id_variant INTEGER NOT NULL,
                                 id_order_m INTEGER NOT NULL,
                                 PRIMARY KEY(id_order_details),
                                 FOREIGN KEY(id_product) REFERENCES products_v2(id_product),
                                 FOREIGN KEY(id_variant) REFERENCES variants_v2(id_variant),
                                 FOREIGN KEY(id_order_m) REFERENCES order_mother(id_order_m)
);

CREATE TABLE stocks_v2(
                          id_mv SERIAL,
                          description TEXT,
                          created_at TIMESTAMP NOT NULL,
                          id_order_m INTEGER,
                          PRIMARY KEY(id_mv),
                          FOREIGN KEY(id_order_m) REFERENCES order_mother(id_order_m)
);

CREATE TABLE stocks_child(
                             id_st_ch SERIAL,
                             price NUMERIC(15,2)   NOT NULL,
                             action_at TIMESTAMP NOT NULL,
                             input NUMERIC(15,2)  ,
                             output NUMERIC(15,2)  ,
                             d_product_number NUMERIC(15,2)  ,
                             d_variant_number NUMERIC(15,2)  ,
                             product_name TEXT,
                             variant_name TEXT,
                             id_product INTEGER NOT NULL,
                             id_variant INTEGER NOT NULL,
                             id_mv INTEGER NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
                             PRIMARY KEY(id_st_ch),
                             FOREIGN KEY(id_product) REFERENCES products_v2(id_product),
                             FOREIGN KEY(id_variant) REFERENCES variants_v2(id_variant),
                             FOREIGN KEY(id_mv) REFERENCES stocks_v2(id_mv)
);

CREATE TABLE payment_method_v2(
                                  id_pm SERIAL,
                                  payment_name TEXT,
                                  PRIMARY KEY(id_pm),
                                  UNIQUE(payment_name)
);

CREATE TABLE sellers_phone_number_e(
                                       id_spn SERIAL,
                                       phone_number VARCHAR(50)  NOT NULL,
                                       associated_name TEXT NOT NULL,
                                       id_pm INTEGER NOT NULL,
                                       id_seller INTEGER NOT NULL,
                                       PRIMARY KEY(id_spn),
                                       FOREIGN KEY(id_pm) REFERENCES payment_method_v2(id_pm),
                                       FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller)
);

CREATE TABLE payment_link(
                             id_pl SERIAL,
                             p_key TEXT NOT NULL,
                             expired_at TIMESTAMP NOT NULL,
                             d_expired BOOLEAN,
                             amount_of_transaction NUMERIC(18,2)   NOT NULL,
                             id_pc TEXT NOT NULL,
                             id_order_m INTEGER NOT NULL,
                             PRIMARY KEY(id_pl),
                             UNIQUE(p_key),
                             FOREIGN KEY(id_pc) REFERENCES potential_customers_v2(id_pc),
                             FOREIGN KEY(id_order_m) REFERENCES order_mother(id_order_m)
);

CREATE TABLE delivery_status_v2(
                                   id_status SERIAL,
                                   status VARCHAR(50) ,
                                   PRIMARY KEY(id_status)
);

CREATE TABLE transport_type_v2(
                                  id_tt SERIAL,
                                  label VARCHAR(50) ,
                                  price_per_ten_km NUMERIC(15,2)  ,
                                  id_seller INTEGER NOT NULL,
                                  PRIMARY KEY(id_tt),
                                  FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller)
);

CREATE TABLE pat_refresh_tokens(
       id_prt SERIAL,
       token TEXT NOT NULL,
       expired_at TIMESTAMP NOT NULL,
       created_at TIMESTAMP NOT NULL,
       revoked BOOLEAN,
       id_mp INTEGER NOT NULL,
       PRIMARY KEY(id_prt),
       UNIQUE(token),
       FOREIGN KEY(id_mp) REFERENCES managed_pages(id_mp)
);


CREATE TABLE pat_access_tokens(
   id_pat SERIAL,
   access_token TEXT NOT NULL,
   expired_at TIMESTAMP NOT NULL,
   created_at TIMESTAMP NOT NULL,
   id_prt INTEGER NOT NULL,
   PRIMARY KEY(id_pat),
   FOREIGN KEY(id_prt) REFERENCES pat_refresh_tokens(id_prt)
);

CREATE TABLE medias(
                       id SERIAL,
                       media_url TEXT NOT NULL,
                       id_child INTEGER NOT NULL,
                       PRIMARY KEY(id),
                       FOREIGN KEY(id_child) REFERENCES post_childs(id_child)
);

CREATE TABLE likes_history(
      id_lh SERIAL,
      created_at TIMESTAMP NOT NULL,
      id_child INTEGER NOT NULL,
      reactions INTEGER,
      PRIMARY KEY(id_lh),
      FOREIGN KEY (id_child) REFERENCES post_childs(id_child)
--       FOREIGN KEY(id_post) REFERENCES posts(id_post),
--       FOREIGN KEY(id_pc) REFERENCES potential_customers_v2(id_pc)
);

CREATE TABLE sales(
                      id_sale TIMESTAMP,
                      amount NUMERIC(18,2)   NOT NULL,
                      effectued_at TIMESTAMP NOT NULL,
                      from_number TEXT NOT NULL,
                      from_name VARCHAR(50)  NOT NULL,
                      description TEXT,
                      id_spn INTEGER NOT NULL,
                      id_order_m INTEGER NOT NULL,
                      id_pc TEXT NOT NULL,
                      PRIMARY KEY(id_sale),
                      FOREIGN KEY(id_spn) REFERENCES sellers_phone_number_e(id_spn),
                      FOREIGN KEY(id_order_m) REFERENCES order_mother(id_order_m),
                      FOREIGN KEY(id_pc) REFERENCES potential_customers_v2(id_pc)
);

CREATE TABLE delivery_v2(
    id_delivery SERIAL,
    shipping_address VARCHAR(50) ,
    ended_at TIMESTAMP,
    phone_number VARCHAR(50) ,
    started_at TIMESTAMP NOT NULL,
    d_status VARCHAR(50)  NOT NULL,
    amount NUMERIC(15,2)  ,
    id_shp INTEGER NOT NULL,
    id_order_m INTEGER NOT NULL,
    PRIMARY KEY(id_delivery),
    FOREIGN KEY(id_shp) REFERENCES shipping_points(id_shp),
    FOREIGN KEY(id_order_m) REFERENCES order_mother(id_order_m)
);

CREATE TABLE shipping_points(
    id_shp SERIAL,
    place_name TEXT NOT NULL,
    latitude NUMERIC(8,6)  ,
    longitude NUMERIC(8,6)  ,
    distance NUMERIC(15,2)   NOT NULL,
    origin VARCHAR(250) ,
    id_mp INTEGER NOT NULL,
    PRIMARY KEY(id_shp),
    FOREIGN KEY(id_mp) REFERENCES managed_pages(id_mp)
);
CREATE TABLE amount_distance(
    id_amount_distance SERIAL,
    price_per_distance NUMERIC(15,2) NOT NULL,
    id_user INTEGER,
    id_mp INTEGER,
    PRIMARY KEY(id_amount_distance),
    FOREIGN KEY(id_user) REFERENCES seller_v2(id_seller),
    FOREIGN KEY(id_mp) REFERENCES managed_pages(id_mp),
    CHECK (id_user IS NOT NULL OR id_mp IS NOT NULL)
);

CREATE TABLE pages_state(
    id_mp INTEGER,
    id_status INTEGER,
    state_at TIMESTAMP NOT NULL,
    PRIMARY KEY(id_mp, id_status),
    FOREIGN KEY(id_mp) REFERENCES managed_pages(id_mp),
    FOREIGN KEY(id_status) REFERENCES platform_status(id_status)
);

CREATE TABLE orders_state(
     id_order_m INTEGER,
     id_status INTEGER,
     state_at TIMESTAMP NOT NULL,
     PRIMARY KEY(id_order_m, id_status),
     FOREIGN KEY(id_order_m) REFERENCES order_mother(id_order_m),
     FOREIGN KEY(id_status) REFERENCES order_status_v2(id_status)
);

CREATE TABLE deliveries_state(
     id_delivery INTEGER,
     id_status INTEGER,
     state_at TIMESTAMP,
     PRIMARY KEY(id_delivery, id_status),
     FOREIGN KEY(id_delivery) REFERENCES delivery_v2(id_delivery),
     FOREIGN KEY(id_status) REFERENCES delivery_status_v2(id_status)
);

CREATE TABLE linked_products(
    id_lp SERIAL,
    id_product INTEGER NOT NULL,
    id_post INTEGER NOT NULL,
    PRIMARY KEY(id_lp),
    FOREIGN KEY(id_product) REFERENCES products_v2(id_product),
    FOREIGN KEY(id_post) REFERENCES posts(id_post)
);

CREATE TABLE down_payment_parameter(
   id SERIAL,
   start_at TIMESTAMP NOT NULL,
   end_at TIMESTAMP NOT NULL,
   payment_in_percent NUMERIC(4,1)  ,
   id_seller INTEGER NOT NULL,
   PRIMARY KEY(id),
   FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller)
);

CREATE TABLE temp_payment_link(
  id TEXT,
  temp_link TEXT NOT NULL,
  expired_at TIMESTAMP NOT NULL,
  phone_number TEXT NOT NULL,
  id_order_m INTEGER NOT NULL,
  id_seller INTEGER NOT NULL,
  PRIMARY KEY(id),
  FOREIGN KEY(id_order_m) REFERENCES order_mother(id_order_m),
  FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller)
);



CREATE TABLE delivery_dp (
    id_delivery_dp SERIAL,
    id_order_m INTEGER NOT NULL,
    id_dd INTEGER NOT NULL,
    id_delivery INTEGER NOT NULL,
    PRIMARY KEY(id_delivery_dp),
    FOREIGN KEY(id_order_m) REFERENCES order_mother(id_order_m),
    FOREIGN KEY(id_dd) REFERENCES delivery_driver_v2(id_dd),
    FOREIGN KEY(id_delivery) REFERENCES delivery_v2(id_delivery)
);

CREATE TABLE amount_distance_log (
     id SERIAL PRIMARY KEY,
     id_amount_distance INTEGER NOT NULL,
     price_per_distance NUMERIC(15,2) NOT NULL,
     id_user INTEGER,
     id_mp INTEGER,
     happened_at TIMESTAMP NOT NULL DEFAULT now(),
     action TEXT NOT NULL, -- e.g. INSERT, UPDATE, DELETE
     FOREIGN KEY (id_user) REFERENCES seller_v2(id_seller),
     FOREIGN KEY (id_mp) REFERENCES managed_pages(id_mp)
);

CREATE TABLE delivery_log(
   id_di SERIAL,
   message TEXT NOT NULL,
   contact VARCHAR(50) ,
   id_mp INTEGER NOT NULL,
   id_seller INTEGER NOT NULL,
   id_dd INTEGER NOT NULL,
   id_delivery INTEGER NOT NULL,
   PRIMARY KEY(id_di),
   FOREIGN KEY(id_mp) REFERENCES managed_pages(id_mp),
   FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller),
   FOREIGN KEY(id_dd) REFERENCES delivery_driver_v2(id_dd),
   FOREIGN KEY(id_delivery) REFERENCES delivery_v2(id_delivery)
);


CREATE TABLE mvola_tokens(
                             id_token SERIAL,
                             token TEXT NOT NULL,
                             start_date TIMESTAMP NOT NULL,
                             expiration_date TIMESTAMP NOT NULL,
                             PRIMARY KEY(id_token)
);


CREATE TABLE mp_payment_number(
                                  id SERIAL,
                                  id_spn INTEGER NOT NULL,
                                  id_mp INTEGER NOT NULL,
                                  PRIMARY KEY(id),
                                  UNIQUE (id_spn, id_mp),
                                  FOREIGN KEY(id_spn) REFERENCES sellers_phone_number_e(id_spn),
                                  FOREIGN KEY(id_mp) REFERENCES managed_pages(id_mp)
);

CREATE TABLE deliverer_token(
                                id SERIAL,
                                token TEXT NOT NULL,
                                expiry_date TIMESTAMP NOT NULL,
                                id_dd INTEGER NOT NULL,
                                PRIMARY KEY(id),
                                FOREIGN KEY(id_dd) REFERENCES delivery_driver_v2(id_dd)
);


CREATE TABLE likes_state_log (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    username varchar(250) NOT NULL,
    id_user_platform varchar(250) NOT NULL,
    id_sp INTEGER NOT NULL,
    reaction varchar(250),
    id_child INTEGER NOT NULL,
    id_mp INTEGER NOT NULL,
    happened_at TIMESTAMP NOT NULL DEFAULT now(),
    FOREIGN KEY (id_sp) REFERENCES supported_platforms_v2(id_sp),
    FOREIGN KEY (id_child) REFERENCES post_childs(id_child),
    FOREIGN KEY (id_mp) REFERENCES managed_pages(id_mp)
);


CREATE TABLE customer (
    id_customer SERIAL,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE ,
    phone_number TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    firebase_uid TEXT NOT NULL UNIQUE ,
    PRIMARY KEY(id_customer)
);

CREATE TABLE cart (
    id_cart SERIAL,
    id_customer INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    state BOOLEAN DEFAULT TRUE NOT NULL,
    PRIMARY KEY(id_cart),
    FOREIGN KEY(id_customer) REFERENCES customer(id_customer)
);

CREATE TABLE cart_details (
    id_cd SERIAL,
    id_cart INTEGER NOT NULL,
    id_product INTEGER NOT NULL,
    id_variant INTEGER NOT NULL,
    quantity NUMERIC(15,2)  NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY(id_cd),
    FOREIGN KEY(id_cart) REFERENCES cart(id_cart),
    FOREIGN KEY(id_product) REFERENCES products_v2(id_product),
    FOREIGN KEY(id_variant) REFERENCES variants_v2(id_variant)
);


CREATE TABLE customer_token(
  id_token SERIAL,
  token TEXT NOT NULL,
  expired_at TIMESTAMP,
  id_customer INTEGER NOT NULL,
  PRIMARY KEY(id_token),
  FOREIGN KEY(id_customer) REFERENCES customer(id_customer)
);

CREATE OR REPLACE FUNCTION log_amount_distance_changes()
    RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO amount_distance_log (id_amount_distance, price_per_distance, id_user, id_mp, happened_at, action)
        VALUES (OLD.id_amount_distance, OLD.price_per_distance, OLD.id_user, OLD.id_mp, now(), TG_OP);
        RETURN OLD;
    ELSE
        INSERT INTO amount_distance_log (id_amount_distance, price_per_distance, id_user, id_mp, happened_at, action)
        VALUES (NEW.id_amount_distance, NEW.price_per_distance, NEW.id_user, NEW.id_mp, now(), TG_OP);
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_amount_distance_log
    AFTER INSERT OR UPDATE OR DELETE
    ON amount_distance
    FOR EACH ROW
EXECUTE FUNCTION log_amount_distance_changes();



SELECT * FROM pat_refresh_tokens;

CREATE VIEW v_managed_accounts AS
WITH max_accesstoken AS (
    SELECT id_pat, access_token, expired_at, id_prt
    FROM (
             SELECT *,
                    ROW_NUMBER() OVER (PARTITION BY id_pat ORDER BY created_at DESC) AS rn
             FROM pat_access_tokens
         ) t
    WHERE rn = 1
),
     max_refreshtoken AS (
         SELECT id_prt, token, expired_at, revoked, id_mp,rn
         FROM (
                  SELECT *,
                         ROW_NUMBER() OVER (PARTITION BY id_mp ORDER BY created_at DESC) AS rn
                  FROM pat_refresh_tokens
              ) t
         WHERE rn = 1
     ),
     max_tokens AS (
         SELECT a.id_pat, a.access_token, a.expired_at as acctoken_expiration,
                r.expired_at as reftoken_expiration, r.id_prt, r.token, r.revoked, r.id_mp
         FROM max_refreshtoken r
                  LEFT JOIN max_accesstoken a ON a.id_prt = r.id_prt
     )
SELECT mp.id_mp,
       d_status,
       platform_identifier,
       page_title,
       associated_media,
       link_to_platform,
       label as platform,
       email,
       mp.id_seller as id_seller,
       username,
       access_token,
       acctoken_expiration,
       reftoken_expiration,
       token,
       revoked
FROM managed_pages mp
         JOIN supported_platforms_v2 s ON mp.id_sp = s.id_sp
         JOIN seller_v2 v ON mp.id_seller = v.id_seller
         LEFT JOIN max_tokens ON mp.id_mp = max_tokens.id_mp;



CREATE VIEW v_refresh_token_holder AS
    SELECT row_number() over (order by 1) as id, managed_pages.*,s.email,pat_refresh_tokens.token FROM seller_v2 as s
        JOIN pat_refresh_tokens
        JOIN managed_pages
            ON pat_refresh_tokens.id_mp = managed_pages.id_mp
            ON s.id_seller = managed_pages.id_seller
        WHERE pat_refresh_tokens.revoked = false;


CREATE VIEW v_post_child_media AS
    SELECT row_number() over (partition by 1) as id,post_childs.id_child,
           post_childs.media_url as main_media_url,description,mp.platform_identifier,post_childs.type,s.id_sp,s.label as supported_platform,
           id_child_1,p.id_post,p.id_seller,m.media_url as additional_media,mp.page_title,mp.associated_media
    FROM post_childs
        LEFT JOIN medias m on post_childs.id_child = m.id_child
        JOIN posts p on post_childs.id_post = p.id_post
        JOIN supported_platforms_v2 s on post_childs.id_sp = s.id_sp
        JOIN managed_pages mp on s.id_sp = mp.id_sp ORDER BY post_childs.id_child, type;


CREATE VIEW v_message_box AS
    SELECT id_mm, message_mother.id_pc, id_mp, id_im, name, link_to_profile, d_platform, identifier_on_platform, media_url, id_sp FROM message_mother JOIN potential_customers_v2 ON potential_customers_v2.id_pc = message_mother.id_pc;

CREATE VIEW v_product_stock_cpl AS
    WITH stock_details AS (
        SELECT max(action_at),d_product_number,id_product FROM stocks_child GROUP BY id_product, d_product_number
    )
    SELECT
        p.id_product,description,name,price,media,id_seller,c.id_category,
        c.val as category,COALESCE(d_product_number,0) as product_number,
        CASE WHEN COALESCE(d_product_number,0) = 0 THEN 'Out of Stock'
                WHEN COALESCE(d_product_number,0) >= 10 THEN 'In Stock'
                WHEN COALESCE(d_product_number,0) > 0 AND COALESCE(d_product_number,0) < 10 THEN 'Low Stock' END as stock_status
        FROM products_v2 p
        LEFT JOIN stock_details s ON p.id_product = s.id_product
        JOIN category c on c.id_category = p.id_category;

CREATE VIEW v_variant_cpl AS
    WITH stock_details AS (
        SELECT max(action_at),d_variant_number,id_variant FROM stocks_child GROUP BY id_variant, d_variant_number
    )
    SELECT
        v.id_variant, v.title, v.price, v.created_at, v.updated_at, v.id_product,
        COALESCE(d_variant_number,0) as variant_number,
        CASE WHEN COALESCE(d_variant_number,0) = 0 THEN 'Out of Stock'
                WHEN COALESCE(d_variant_number,0) >= 10 THEN 'In Stock'
                WHEN COALESCE(d_variant_number,0) > 0 AND COALESCE(d_variant_number,0) < 10 THEN 'Low Stock' END as stock_status
        FROM variants_v2 v LEFT JOIN stock_details ON v.id_variant = stock_details.id_variant;


WITH recent_variants_retriever AS
(
    SELECT id_variant, MAX(created_at) AS max_created_at
    FROM stocks_child
    WHERE id_variant IN (?)
    GROUP BY id_variant
)
SELECT sc.*
FROM stocks_child sc
JOIN recent_variants_retriever AS sub ON sc.id_variant = sub.id_variant AND sc.created_at = sub.max_created_at;


CREATE VIEW v_delivery_applicants AS
    SELECT
        delivery_log.id_di,d.id_delivery,d.shipping_address,d.id_shp,d.d_status,d.amount,d.distance,dd.id_dd,dd.name as driver_name,dd.phone_number as driver_phone,mp.id_mp,mp.page_title,s.id_seller,s.email,s.username
        FROM delivery_log
        JOIN delivery_v2 d on d.id_delivery = delivery_log.id_delivery
        JOIN delivery_driver_v2 dd on dd.id_dd = delivery_log.id_dd
        JOIN managed_pages mp on delivery_log.id_mp = mp.id_mp
        JOIN seller_v2 s on delivery_log.id_seller = s.id_seller;

CREATE VIEW v_mission_history AS
    SELECT id_di,d.*,sp.origin,sp.place_name,delivery_log.id_dd AS log_id_deliverer FROM delivery_log
        JOIN delivery_v2 d on d.id_delivery = delivery_log.id_delivery
        JOIN shipping_points sp on d.id_shp = sp.id_shp WHERE d_status='CLOSED'
;

CREATE view v_pending_request AS
    SELECT id_di,delivery_v2.*,sp.place_name,dl.id_dd as log_id_deliverer,sp.origin FROM delivery_v2
        JOIN delivery_log dl on delivery_v2.id_delivery = dl.id_delivery
        JOIN delivery_driver_v2 dd on dl.id_dd = dd.id_dd
        JOIN shipping_points sp on delivery_v2.id_shp = sp.id_shp
             WHERE d_status='CALL_FOR_TENDERED';

CREATE VIEW v_order_mother_cpl AS
    SELECT order_mother.*,managed_pages.id_sp,page_title FROM order_mother JOIN managed_pages ON managed_pages.id_mp = order_mother.id_managed_pages;
-- Revenue per platform for a specific seller (id_seller = 1)
WITH total_revenue AS (
    SELECT SUM(d_total) AS total_revenue
    FROM order_mother
    WHERE id_seller = 1
)
SELECT row_number() over () as dummy_id,sum(d_total)/total_revenue.total_revenue * 100 as total_percentage,sum(d_total) as total,id_sp
FROM v_order_mother_cpl
    CROSS JOIN total_revenue
WHERE v_order_mother_cpl.id_seller = 1 GROUP BY id_sp,total_revenue.total_revenue ;
-- Revenue per pages for a specific seller (id_seller = 1)
WITH total_revenue AS (
    SELECT SUM(d_total) AS total_revenue
    FROM order_mother
    WHERE id_seller = 1
)
SELECT  row_number() over () as dummy_id,COALESCE(sum(d_total)/total_revenue.total_revenue * 100,0) as total_percentage,mp.page_title,mp.associated_media,COALESCE(sum(d_total),0) as total,mp.id_mp as id_managed_pages,mp.id_sp
FROM v_order_mother_cpl
    RIGHT JOIN managed_pages mp on v_order_mother_cpl.id_managed_pages = mp.id_mp
    CROSS JOIN total_revenue
WHERE mp.id_seller = 1
GROUP BY mp.id_mp,mp.id_sp,mp.page_title,total_revenue.total_revenue;


--
-- CREATE OR REPLACE FUNCTION update_stocks_child_denormalized_fields_function()
--     RETURNS TRIGGER AS $$
-- BEGIN
--     -- The trigger is fired FOR EACH ROW that is inserted or updated.
--
--     -- The first part of this function updates the variant and product stock numbers
--     -- from the current row's timestamp onwards.
--
--     WITH RunningTotals AS (
--         SELECT
--             sc.id_st_ch,
--             sc.id_variant,
--             sc.id_product,
--
--             -- Calculate the cumulative sum for the variant stock
--             -- We start the sum from the last known value before this transaction's date.
--             COALESCE((
--                          SELECT d_variant_number
--                          FROM stocks_child
--                          WHERE id_variant = NEW.id_variant AND created_at < NEW.created_at
--                          ORDER BY created_at DESC
--                          LIMIT 1
--                      ), 0) + SUM(COALESCE(sc.input, 0) - COALESCE(sc.output, 0)) OVER (PARTITION BY sc.id_variant ORDER BY sc.created_at) AS new_variant_number,
--
--             -- Calculate the cumulative sum for the product stock
--             COALESCE((
--                          SELECT d_product_number
--                          FROM stocks_child
--                          WHERE id_product = NEW.id_product AND created_at < NEW.created_at
--                          ORDER BY created_at DESC
--                          LIMIT 1
--                      ), 0) + SUM(COALESCE(sc.input, 0) - COALESCE(sc.output, 0)) OVER (PARTITION BY sc.id_product ORDER BY sc.created_at) AS new_product_number
--         FROM stocks_child sc
--         WHERE
--             (sc.id_variant = NEW.id_variant OR sc.id_product = NEW.id_product)
--           AND sc.created_at >= NEW.created_at
--     )
--     UPDATE stocks_child
--     SET
--         d_variant_number = rt.new_variant_number,
--         d_product_number = rt.new_product_number
--     FROM RunningTotals rt
--     WHERE stocks_child.id_st_ch = rt.id_st_ch;
--
--     -- This ensures the current row being inserted/updated also gets the correct values.
--     -- The `UPDATE` statement above might not catch the `NEW` row itself, depending on timing.
--     -- We'll manually set the values to be safe.
--     RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql;
--
-- CREATE TRIGGER update_stocks_child_denormalized_fields_trigger
--     AFTER INSERT OR UPDATE ON stocks_child
--     FOR EACH ROW
-- EXECUTE FUNCTION update_stocks_child_denormalized_fields_function();


CREATE VIEW v_likes_history_post_child AS (
    SELECT likes_history.*,pc.id_mp,p.id_seller FROM likes_history JOIN post_childs pc on pc.id_child = likes_history.id_child JOIN posts p on pc.id_post = p.id_post
);

WITH engagement_by_datetime AS (
    SELECT
        EXTRACT(DOW FROM created_at) AS day_of_week,
        EXTRACT(HOUR FROM created_at) AS hour_of_day,
        COUNT(*) AS total_posts,
        SUM(reactions) AS total_reactions,
        AVG(reactions) AS avg_reactions,
        PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY reactions) AS median_reactions
    FROM v_likes_history_post_child
    WHERE reactions IS NOT NULL AND id_seller = 1
    GROUP BY
        EXTRACT(DOW FROM created_at),
        EXTRACT(HOUR FROM created_at)
)
SELECT
    CASE day_of_week
        WHEN 0 THEN 'Sunday'
        WHEN 1 THEN 'Monday'
        WHEN 2 THEN 'Tuesday'
        WHEN 3 THEN 'Wednesday'
        WHEN 4 THEN 'Thursday'
        WHEN 5 THEN 'Friday'
        WHEN 6 THEN 'Saturday'
        END AS best_day,
    hour_of_day AS best_hour,
    total_posts,
    total_reactions,
    ROUND(avg_reactions, 2) AS avg_reactions,
    median_reactions AS median_reactions
FROM engagement_by_datetime
WHERE total_posts >= 5 -- Filter out combinations with too few posts
ORDER BY avg_reactions DESC
LIMIT 10;

-- Analysis 2: Best Day of Week (aggregated across all hours)
WITH daily_engagement AS (
    SELECT
        EXTRACT(DOW FROM created_at) AS day_of_week,
        COUNT(*) AS total_posts,
        SUM(reactions) AS total_reactions,
        AVG(reactions) AS avg_reactions
    FROM likes_history
    WHERE reactions IS NOT NULL
    GROUP BY EXTRACT(DOW FROM created_at)
)
SELECT
    CASE day_of_week
        WHEN 0 THEN 'Sunday'
        WHEN 1 THEN 'Monday'
        WHEN 2 THEN 'Tuesday'
        WHEN 3 THEN 'Wednesday'
        WHEN 4 THEN 'Thursday'
        WHEN 5 THEN 'Friday'
        WHEN 6 THEN 'Saturday'
        END AS day_name,
    total_posts,
    total_reactions,
    ROUND(avg_reactions, 2) AS avg_reactions
FROM daily_engagement
ORDER BY avg_reactions DESC;

-- Analysis 3: Best Hour of Day (aggregated across all days)
WITH hourly_engagement AS (
    SELECT
        EXTRACT(HOUR FROM created_at) AS hour_of_day,
        COUNT(*) AS total_posts,
        SUM(reactions) AS total_reactions,
        AVG(reactions) AS avg_reactions
    FROM likes_history
    WHERE reactions IS NOT NULL
    GROUP BY EXTRACT(HOUR FROM created_at)
)
SELECT
    hour_of_day,
    CASE
        WHEN hour_of_day = 0 THEN '12:00 AM'
        WHEN hour_of_day < 12 THEN hour_of_day || ':00 AM'
        WHEN hour_of_day = 12 THEN '12:00 PM'
        ELSE (hour_of_day - 12) || ':00 PM'
        END AS time_formatted,
    total_posts,
    total_reactions,
    ROUND(avg_reactions, 2) AS avg_reactions
FROM hourly_engagement
ORDER BY avg_reactions DESC;

-- Analysis 4: Heatmap data (for visualization)
SELECT
    CASE EXTRACT(DOW FROM created_at)
        WHEN 0 THEN 'Sunday'
        WHEN 1 THEN 'Monday'
        WHEN 2 THEN 'Tuesday'
        WHEN 3 THEN 'Wednesday'
        WHEN 4 THEN 'Thursday'
        WHEN 5 THEN 'Friday'
        WHEN 6 THEN 'Saturday'
        END AS day_name,
    EXTRACT(HOUR FROM created_at) AS hour,
    COUNT(*) AS post_count,
    ROUND(AVG(reactions), 2) AS avg_reactions
FROM likes_history
WHERE reactions IS NOT NULL
GROUP BY
    EXTRACT(DOW FROM created_at),
    EXTRACT(HOUR FROM created_at)
ORDER BY
    EXTRACT(DOW FROM created_at),
    hour;


SELECT
    CONCAT('Week ', EXTRACT(WEEK FROM created_at)) AS y_axis,
    CASE EXTRACT(DOW FROM created_at)
        WHEN 0 THEN 'Sunday'
        WHEN 1 THEN 'Monday'
        WHEN 2 THEN 'Tuesday'
        WHEN 3 THEN 'Wednesday'
        WHEN 4 THEN 'Thursday'
        WHEN 5 THEN 'Friday'
        WHEN 6 THEN 'Saturday'
        END AS x_axis
        ,
    COUNT(*) AS post_count,
    ROUND(AVG(reactions), 2) AS avg_reactions,
    MIN(EXTRACT(DOW FROM created_at)) AS dow_order,
    MIN(EXTRACT(HOUR FROM created_at)) AS hour_order,
    MIN(EXTRACT(WEEK FROM created_at)) AS week_order,
    MIN(EXTRACT(MONTH FROM created_at)) AS month_order
FROM likes_history
WHERE reactions IS NOT NULL
GROUP BY y_axis, x_axis
ORDER BY
    CASE
        WHEN y_axis ~ '^[0-9]' THEN CAST(REGEXP_REPLACE(y_axis, '[^0-9]', '', 'g') AS INTEGER)
        ELSE dow_order
        END,
    CASE
        WHEN x_axis ~ '^[0-9]' THEN CAST(REGEXP_REPLACE(x_axis, '[^0-9]', '', 'g') AS INTEGER)
        ELSE hour_order
    END;



-- Electronics & Technology
INSERT INTO category (val, desc_) VALUES ('Electronics', 'Consumer electronics, gadgets, and electronic devices');
INSERT INTO category (val, desc_) VALUES ('Computers & Laptops', 'Desktop computers, laptops, tablets, and computer accessories');
INSERT INTO category (val, desc_) VALUES ('Mobile Phones', 'Smartphones, basic phones, and mobile accessories');
INSERT INTO category (val, desc_) VALUES ('Audio & Video', 'Headphones, speakers, cameras, TVs, and audio/video equipment');
INSERT INTO category (val, desc_) VALUES ('Gaming', 'Video games, gaming consoles, and gaming accessories');
INSERT INTO category (val, desc_) VALUES ('Smart Home', 'Home automation devices, smart speakers, and IoT products');

-- Fashion & Clothing
INSERT INTO category (val, desc_) VALUES ('Men''s Clothing', 'Clothing and apparel for men');
INSERT INTO category (val, desc_) VALUES ('Women''s Clothing', 'Clothing and apparel for women');
INSERT INTO category (val, desc_) VALUES ('Kids & Baby Clothing', 'Clothing for children and babies');
INSERT INTO category (val, desc_) VALUES ('Shoes & Footwear', 'All types of footwear for men, women, and children');
INSERT INTO category (val, desc_) VALUES ('Accessories & Jewelry', 'Fashion accessories, jewelry, watches, and bags');
INSERT INTO category (val, desc_) VALUES ('Activewear & Sportswear', 'Sports clothing, gym wear, and athletic apparel');

-- Home & Garden
INSERT INTO category (val, desc_) VALUES ('Home Decor', 'Decorative items, artwork, and home styling products');
INSERT INTO category (val, desc_) VALUES ('Furniture', 'Indoor and outdoor furniture for all rooms');
INSERT INTO category (val, desc_) VALUES ('Kitchen & Dining', 'Cookware, dinnerware, kitchen appliances, and utensils');
INSERT INTO category (val, desc_) VALUES ('Bedding & Bath', 'Bed linens, towels, bathroom accessories, and sleep products');
INSERT INTO category (val, desc_) VALUES ('Garden & Outdoor', 'Gardening supplies, outdoor furniture, and lawn care equipment');
INSERT INTO category (val, desc_) VALUES ('Home Improvement', 'Tools, hardware, paint, and home renovation supplies');

-- Health & Beauty
INSERT INTO category (val, desc_) VALUES ('Beauty & Personal Care', 'Cosmetics, skincare, haircare, and personal hygiene products');
INSERT INTO category (val, desc_) VALUES ('Health & Wellness', 'Vitamins, supplements, medical supplies, and health products');
INSERT INTO category (val, desc_) VALUES ('Fitness Equipment', 'Exercise machines, weights, and fitness accessories');
INSERT INTO category (val, desc_) VALUES ('Pharmacy', 'Over-the-counter medications and pharmaceutical products');

-- Automotive
INSERT INTO category (val, desc_) VALUES ('Car Parts & Accessories', 'Vehicle parts, accessories, and automotive supplies');
INSERT INTO category (val, desc_) VALUES ('Motorcycles & ATVs', 'Motorcycles, ATVs, and related parts and accessories');
INSERT INTO category (val, desc_) VALUES ('Car Electronics', 'GPS systems, dash cams, car audio, and automotive electronics');

-- Sports & Recreation
INSERT INTO category (val, desc_) VALUES ('Sports Equipment', 'Equipment for various sports and recreational activities');
INSERT INTO category (val, desc_) VALUES ('Outdoor Recreation', 'Camping, hiking, fishing, and outdoor adventure gear');
INSERT INTO category (val, desc_) VALUES ('Bicycles', 'Bikes, bike parts, and cycling accessories');
INSERT INTO category (val, desc_) VALUES ('Water Sports', 'Swimming, surfing, boating, and water activity equipment');

-- Books, Media & Entertainment
INSERT INTO category (val, desc_) VALUES ('Books', 'Physical and digital books across all genres');
INSERT INTO category (val, desc_) VALUES ('Movies & TV', 'DVDs, Blu-rays, digital movies, and TV show collections');
INSERT INTO category (val, desc_) VALUES ('Music', 'CDs, vinyl records, digital music, and musical instruments');
INSERT INTO category (val, desc_) VALUES ('Musical Instruments', 'Guitars, pianos, drums, and all musical instruments');

-- Food & Beverages
INSERT INTO category (val, desc_) VALUES ('Groceries', 'Food items, snacks, and everyday grocery products');
INSERT INTO category (val, desc_) VALUES ('Beverages', 'Drinks, juices, coffee, tea, and alcoholic beverages');
INSERT INTO category (val, desc_) VALUES ('Gourmet Food', 'Specialty foods, organic products, and premium food items');
INSERT INTO category (val, desc_) VALUES ('Pet Food', 'Food and treats for dogs, cats, and other pets');

-- Toys & Baby Products
INSERT INTO category (val, desc_) VALUES ('Toys & Games', 'Toys for all ages, board games, and educational toys');
INSERT INTO category (val, desc_) VALUES ('Baby Products', 'Baby care items, strollers, car seats, and infant supplies');
INSERT INTO category (val, desc_) VALUES ('Kids'' Furniture', 'Furniture designed specifically for children');

-- Office & Business
INSERT INTO category (val, desc_) VALUES ('Office Supplies', 'Stationery, office equipment, and workplace essentials');
INSERT INTO category (val, desc_) VALUES ('Business Equipment', 'Printers, scanners, office furniture, and business machines');
INSERT INTO category (val, desc_) VALUES ('Industrial & Scientific', 'Professional tools, laboratory equipment, and industrial supplies');

-- Pet Supplies
INSERT INTO category (val, desc_) VALUES ('Pet Supplies', 'General pet care products and accessories');
INSERT INTO category (val, desc_) VALUES ('Dog Supplies', 'Products specifically for dogs');
INSERT INTO category (val, desc_) VALUES ('Cat Supplies', 'Products specifically for cats');
INSERT INTO category (val, desc_) VALUES ('Fish & Aquatic Pets', 'Aquarium supplies and fish care products');

-- Art & Crafts
INSERT INTO category (val, desc_) VALUES ('Arts & Crafts', 'Art supplies, craft materials, and creative hobby items');
INSERT INTO category (val, desc_) VALUES ('Sewing & Knitting', 'Fabric, yarn, sewing machines, and textile crafts');
INSERT INTO category (val, desc_) VALUES ('Collectibles', 'Antiques, collectible items, and rare finds');

-- Services
INSERT INTO category (val, desc_) VALUES ('Digital Services', 'Software, apps, digital subscriptions, and online services');
INSERT INTO category (val, desc_) VALUES ('Gift Cards', 'Gift certificates and prepaid cards');
INSERT INTO category (val, desc_) VALUES ('Professional Services', 'Consulting, repairs, installations, and professional assistance');

-- Miscellaneous
INSERT INTO category (val, desc_) VALUES ('Travel & Luggage', 'Suitcases, travel accessories, and travel-related products');
INSERT INTO category (val, desc_) VALUES ('Religious & Spiritual', 'Religious books, spiritual items, and ceremonial products');
INSERT INTO category (val, desc_) VALUES ('Party Supplies', 'Decorations, party favors, and event planning items');
INSERT INTO category (val, desc_) VALUES ('Seasonal Items', 'Holiday decorations, seasonal products, and special occasion items');
INSERT INTO category (val, desc_) VALUES ('Other', 'Miscellaneous items that don''t fit other categories');