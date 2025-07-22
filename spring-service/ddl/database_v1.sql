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
                      platform_post_identifier TEXT NOT NULL,
                      associated_media TEXT,
                      message TEXT,
                      d_platform VARCHAR(50) ,
                      id_post_1 INTEGER NOT NULL,
                      id_seller INTEGER NOT NULL,
                      id_sp INTEGER NOT NULL,
                      PRIMARY KEY(id_post),
                      UNIQUE(platform_post_identifier),
                      FOREIGN KEY(id_post_1) REFERENCES posts(id_post),
                      FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller),
                      FOREIGN KEY(id_sp) REFERENCES supported_platforms_v2(id_sp)
);

CREATE TABLE potential_customers_v2(
                                       id_pc TEXT,
                                       name TEXT NOT NULL,
                                       link_to_profile TEXT,
                                       d_platform VARCHAR(50) ,
                                       id_sp INTEGER NOT NULL,
                                       PRIMARY KEY(id_pc),
                                       FOREIGN KEY(id_sp) REFERENCES supported_platforms_v2(id_sp)
);

CREATE TABLE comments_v2(
                            id_comment TEXT,
                            message TEXT NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            deleted BOOLEAN,
                            id_post INTEGER NOT NULL,
                            PRIMARY KEY(id_comment),
                            FOREIGN KEY(id_post) REFERENCES posts(id_post)
);

CREATE TABLE inbox_mother(
                             id_im SERIAL,
                             id_seller INTEGER NOT NULL,
                             id_pc TEXT NOT NULL,
                             PRIMARY KEY(id_im),
                             FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller),
                             FOREIGN KEY(id_pc) REFERENCES potential_customers_v2(id_pc)
);

CREATE TABLE inbox_child(
                            id_ic VARCHAR(50) ,
                            message TEXT NOT NULL,
                            media TEXT,
                            id_pc TEXT NOT NULL,
                            id_seller INTEGER NOT NULL,
                            id_im INTEGER NOT NULL,
                            PRIMARY KEY(id_ic),
                            FOREIGN KEY(id_pc) REFERENCES potential_customers_v2(id_pc),
                            FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller),
                            FOREIGN KEY(id_im) REFERENCES inbox_mother(id_im)
);

CREATE TABLE products_v2(
                            id_product SERIAL,
                            description TEXT,
                            name TEXT NOT NULL,
                            price NUMERIC(18,2)   NOT NULL,
                            created_at TIMESTAMP,
                            updated_at TIMESTAMP,
                            media TEXT,
                            id_seller INTEGER NOT NULL,
                            PRIMARY KEY(id_product),
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
                           PRIMARY KEY(id_option)
);

CREATE TABLE options_values_v2(
                                  id_ov VARCHAR(50) ,
                                  value_ TEXT NOT NULL,
                                  id_option INTEGER NOT NULL,
                                  PRIMARY KEY(id_ov),
                                  FOREIGN KEY(id_option) REFERENCES options_v2(id_option)
);

CREATE TABLE variant_option_values_v2(
                                         id SERIAL,
                                         id_ov VARCHAR(50)  NOT NULL,
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
                             PRIMARY KEY(id_order_m),
                             FOREIGN KEY(id_pc) REFERENCES potential_customers_v2(id_pc)
);

CREATE TABLE order_status_v2(
                                id_status SERIAL,
                                label TEXT,
                                PRIMARY KEY(id_status)
);

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


CREATE TABLE likes_history(
                              id_lh SERIAL,
                              created_at TIMESTAMP NOT NULL,
                              deleted BOOLEAN,
                              id_post INTEGER NOT NULL,
                              id_pc TEXT NOT NULL,
                              PRIMARY KEY(id_lh),
                              FOREIGN KEY(id_post) REFERENCES posts(id_post),
                              FOREIGN KEY(id_pc) REFERENCES potential_customers_v2(id_pc)
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

CREATE TABLE delivery_driver_v2(
                                   id_dd SERIAL,
                                   name VARCHAR(250) ,
                                   phone_number VARCHAR(50)  NOT NULL,
                                   id_tt INTEGER NOT NULL,
                                   id_seller INTEGER NOT NULL,
                                   PRIMARY KEY(id_dd),
                                   FOREIGN KEY(id_tt) REFERENCES transport_type_v2(id_tt),
                                   FOREIGN KEY(id_seller) REFERENCES seller_v2(id_seller)
);

CREATE TABLE delivery_v2(
                            id_delivery SERIAL,
                            shipping_address VARCHAR(50) ,
                            ended_at TIMESTAMP,
                            phone_number VARCHAR(50) ,
                            started_at TIMESTAMP NOT NULL,
                            d_status VARCHAR(50)  NOT NULL,
                            id_order_m INTEGER NOT NULL,
                            id_dd INTEGER NOT NULL,
                            PRIMARY KEY(id_delivery),
                            FOREIGN KEY(id_order_m) REFERENCES order_mother(id_order_m),
                            FOREIGN KEY(id_dd) REFERENCES delivery_driver_v2(id_dd)
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

SELECT * FROM pat_refresh_tokens;

CREATE VIEW v_managed_accounts AS
SELECT id_mp,d_status,platform_identifier,page_title,associated_media,link_to_platform,label as platform,email,managed_pages.id_seller as id_seller,username FROM managed_pages
                  JOIN supported_platforms_v2 s on managed_pages.id_sp = s.id_sp
                  JOIN seller_v2 v on managed_pages.id_seller = v.id_seller;