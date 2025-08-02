--
-- PostgreSQL database dump
--

-- Dumped from database version 14.18 (Debian 14.18-1.pgdg120+1)
-- Dumped by pg_dump version 14.18 (Debian 14.18-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: platforms_pro; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.platforms_pro AS ENUM (
    'google',
    'facebook',
    'basic',
    'X',
    'github'
);


ALTER TYPE public.platforms_pro OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.category (
    id_category integer NOT NULL,
    val text NOT NULL,
    desc_ text
);


ALTER TABLE public.category OWNER TO postgres;

--
-- Name: category_id_category_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.category_id_category_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.category_id_category_seq OWNER TO postgres;

--
-- Name: category_id_category_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.category_id_category_seq OWNED BY public.category.id_category;


--
-- Name: comments_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.comments_v2 (
    id_comment text NOT NULL,
    message text NOT NULL,
    created_at timestamp without time zone NOT NULL,
    deleted boolean,
    id_child integer NOT NULL
);


ALTER TABLE public.comments_v2 OWNER TO postgres;

--
-- Name: deliveries_state; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.deliveries_state (
    id_delivery integer NOT NULL,
    id_status integer NOT NULL,
    state_at timestamp without time zone
);


ALTER TABLE public.deliveries_state OWNER TO postgres;

--
-- Name: delivery_driver_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_driver_v2 (
    id_dd integer NOT NULL,
    name character varying(250),
    phone_number character varying(50) NOT NULL,
    id_tt integer NOT NULL,
    id_seller integer NOT NULL
);


ALTER TABLE public.delivery_driver_v2 OWNER TO postgres;

--
-- Name: delivery_driver_v2_id_dd_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_driver_v2_id_dd_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.delivery_driver_v2_id_dd_seq OWNER TO postgres;

--
-- Name: delivery_driver_v2_id_dd_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.delivery_driver_v2_id_dd_seq OWNED BY public.delivery_driver_v2.id_dd;


--
-- Name: delivery_status_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_status_v2 (
    id_status integer NOT NULL,
    status character varying(50)
);


ALTER TABLE public.delivery_status_v2 OWNER TO postgres;

--
-- Name: delivery_status_v2_id_status_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_status_v2_id_status_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.delivery_status_v2_id_status_seq OWNER TO postgres;

--
-- Name: delivery_status_v2_id_status_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.delivery_status_v2_id_status_seq OWNED BY public.delivery_status_v2.id_status;


--
-- Name: delivery_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_v2 (
    id_delivery integer NOT NULL,
    shipping_address character varying(50),
    ended_at timestamp without time zone,
    phone_number character varying(50),
    started_at timestamp without time zone NOT NULL,
    d_status character varying(50) NOT NULL,
    id_order_m integer NOT NULL,
    id_dd integer NOT NULL
);


ALTER TABLE public.delivery_v2 OWNER TO postgres;

--
-- Name: delivery_v2_id_delivery_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_v2_id_delivery_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.delivery_v2_id_delivery_seq OWNER TO postgres;

--
-- Name: delivery_v2_id_delivery_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.delivery_v2_id_delivery_seq OWNED BY public.delivery_v2.id_delivery;


--
-- Name: inbox_child; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inbox_child (
    id_ic character varying(50) NOT NULL,
    message text NOT NULL,
    media text,
    id_pc text NOT NULL,
    id_seller integer NOT NULL,
    id_im integer NOT NULL
);


ALTER TABLE public.inbox_child OWNER TO postgres;

--
-- Name: inbox_mother; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inbox_mother (
    id_im integer NOT NULL,
    id_seller integer NOT NULL,
    id_pc text NOT NULL
);


ALTER TABLE public.inbox_mother OWNER TO postgres;

--
-- Name: inbox_mother_id_im_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.inbox_mother_id_im_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.inbox_mother_id_im_seq OWNER TO postgres;

--
-- Name: inbox_mother_id_im_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.inbox_mother_id_im_seq OWNED BY public.inbox_mother.id_im;


--
-- Name: likes_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.likes_history (
    id_lh integer NOT NULL,
    created_at timestamp without time zone NOT NULL,
    deleted boolean,
    id_child integer,
    id_pc text NOT NULL
);


ALTER TABLE public.likes_history OWNER TO postgres;

--
-- Name: likes_history_id_lh_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.likes_history_id_lh_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.likes_history_id_lh_seq OWNER TO postgres;

--
-- Name: likes_history_id_lh_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.likes_history_id_lh_seq OWNED BY public.likes_history.id_lh;


--
-- Name: linked_products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.linked_products (
    id_lp integer NOT NULL,
    id_product integer NOT NULL,
    id_post integer NOT NULL
);


ALTER TABLE public.linked_products OWNER TO postgres;

--
-- Name: linked_products_id_lp_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.linked_products_id_lp_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.linked_products_id_lp_seq OWNER TO postgres;

--
-- Name: linked_products_id_lp_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.linked_products_id_lp_seq OWNED BY public.linked_products.id_lp;


--
-- Name: managed_pages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.managed_pages (
    id_mp integer NOT NULL,
    d_status character varying(50),
    platform_identifier text NOT NULL,
    page_title text NOT NULL,
    associated_media text,
    link_to_platform text NOT NULL,
    id_sp integer NOT NULL,
    id_seller integer NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


ALTER TABLE public.managed_pages OWNER TO postgres;

--
-- Name: managed_pages_id_mp_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.managed_pages_id_mp_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.managed_pages_id_mp_seq OWNER TO postgres;

--
-- Name: managed_pages_id_mp_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.managed_pages_id_mp_seq OWNED BY public.managed_pages.id_mp;


--
-- Name: medias; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.medias (
    id integer NOT NULL,
    media_url text NOT NULL,
    id_child integer NOT NULL
);


ALTER TABLE public.medias OWNER TO postgres;

--
-- Name: medias_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.medias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.medias_id_seq OWNER TO postgres;

--
-- Name: medias_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.medias_id_seq OWNED BY public.medias.id;


--
-- Name: options_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.options_v2 (
    id_option integer NOT NULL,
    label text NOT NULL,
    id_product integer NOT NULL
);


ALTER TABLE public.options_v2 OWNER TO postgres;

--
-- Name: options_v2_id_option_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.options_v2_id_option_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.options_v2_id_option_seq OWNER TO postgres;

--
-- Name: options_v2_id_option_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.options_v2_id_option_seq OWNED BY public.options_v2.id_option;


--
-- Name: options_values_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.options_values_v2 (
    id_ov integer NOT NULL,
    value_ text NOT NULL,
    id_option integer NOT NULL
);


ALTER TABLE public.options_values_v2 OWNER TO postgres;

--
-- Name: options_values_v2_id_ov_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.options_values_v2_id_ov_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.options_values_v2_id_ov_seq OWNER TO postgres;

--
-- Name: options_values_v2_id_ov_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.options_values_v2_id_ov_seq OWNED BY public.options_values_v2.id_ov;


--
-- Name: order_details_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.order_details_v2 (
    id_order_details integer NOT NULL,
    price numeric(18,2) NOT NULL,
    quantity numeric(15,2),
    id_product integer NOT NULL,
    id_variant integer NOT NULL,
    id_order_m integer NOT NULL
);


ALTER TABLE public.order_details_v2 OWNER TO postgres;

--
-- Name: order_details_v2_id_order_details_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.order_details_v2_id_order_details_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.order_details_v2_id_order_details_seq OWNER TO postgres;

--
-- Name: order_details_v2_id_order_details_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.order_details_v2_id_order_details_seq OWNED BY public.order_details_v2.id_order_details;


--
-- Name: order_mother; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.order_mother (
    id_order_m integer NOT NULL,
    description text,
    created_at timestamp without time zone NOT NULL,
    d_total numeric(18,2),
    d_customer_name text,
    d_status character varying(50),
    shipping_address text,
    customer_number character varying(50),
    id_pc text NOT NULL
);


ALTER TABLE public.order_mother OWNER TO postgres;

--
-- Name: order_mother_id_order_m_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.order_mother_id_order_m_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.order_mother_id_order_m_seq OWNER TO postgres;

--
-- Name: order_mother_id_order_m_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.order_mother_id_order_m_seq OWNED BY public.order_mother.id_order_m;


--
-- Name: order_status_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.order_status_v2 (
    id_status integer NOT NULL,
    label text
);


ALTER TABLE public.order_status_v2 OWNER TO postgres;

--
-- Name: order_status_v2_id_status_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.order_status_v2_id_status_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.order_status_v2_id_status_seq OWNER TO postgres;

--
-- Name: order_status_v2_id_status_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.order_status_v2_id_status_seq OWNED BY public.order_status_v2.id_status;


--
-- Name: orders_state; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.orders_state (
    id_order_m integer NOT NULL,
    id_status integer NOT NULL,
    state_at timestamp without time zone NOT NULL
);


ALTER TABLE public.orders_state OWNER TO postgres;

--
-- Name: pages_state; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pages_state (
    id_mp integer NOT NULL,
    id_status integer NOT NULL,
    state_at timestamp without time zone NOT NULL
);


ALTER TABLE public.pages_state OWNER TO postgres;

--
-- Name: pat_access_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pat_access_tokens (
    id_pat integer NOT NULL,
    access_token text NOT NULL,
    expired_at timestamp without time zone NOT NULL,
    created_at timestamp without time zone NOT NULL,
    id_prt integer NOT NULL
);


ALTER TABLE public.pat_access_tokens OWNER TO postgres;

--
-- Name: pat_access_tokens_id_pat_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.pat_access_tokens_id_pat_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pat_access_tokens_id_pat_seq OWNER TO postgres;

--
-- Name: pat_access_tokens_id_pat_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.pat_access_tokens_id_pat_seq OWNED BY public.pat_access_tokens.id_pat;


--
-- Name: pat_refresh_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pat_refresh_tokens (
    id_prt integer NOT NULL,
    token text NOT NULL,
    expired_at timestamp without time zone NOT NULL,
    created_at timestamp without time zone NOT NULL,
    revoked boolean,
    id_mp integer NOT NULL
);


ALTER TABLE public.pat_refresh_tokens OWNER TO postgres;

--
-- Name: pat_refresh_tokens_id_prt_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.pat_refresh_tokens_id_prt_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pat_refresh_tokens_id_prt_seq OWNER TO postgres;

--
-- Name: pat_refresh_tokens_id_prt_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.pat_refresh_tokens_id_prt_seq OWNED BY public.pat_refresh_tokens.id_prt;


--
-- Name: payment_link; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.payment_link (
    id_pl integer NOT NULL,
    p_key text NOT NULL,
    expired_at timestamp without time zone NOT NULL,
    d_expired boolean,
    amount_of_transaction numeric(18,2) NOT NULL,
    id_pc text NOT NULL,
    id_order_m integer NOT NULL
);


ALTER TABLE public.payment_link OWNER TO postgres;

--
-- Name: payment_link_id_pl_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.payment_link_id_pl_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_link_id_pl_seq OWNER TO postgres;

--
-- Name: payment_link_id_pl_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.payment_link_id_pl_seq OWNED BY public.payment_link.id_pl;


--
-- Name: payment_method_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.payment_method_v2 (
    id_pm integer NOT NULL,
    payment_name text
);


ALTER TABLE public.payment_method_v2 OWNER TO postgres;

--
-- Name: payment_method_v2_id_pm_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.payment_method_v2_id_pm_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_method_v2_id_pm_seq OWNER TO postgres;

--
-- Name: payment_method_v2_id_pm_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.payment_method_v2_id_pm_seq OWNED BY public.payment_method_v2.id_pm;


--
-- Name: platform_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.platform_status (
    id_status integer NOT NULL,
    status_labem character varying(50) NOT NULL
);


ALTER TABLE public.platform_status OWNER TO postgres;

--
-- Name: platform_status_id_status_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.platform_status_id_status_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.platform_status_id_status_seq OWNER TO postgres;

--
-- Name: platform_status_id_status_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.platform_status_id_status_seq OWNED BY public.platform_status.id_status;


--
-- Name: post_childs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.post_childs (
    id_child integer NOT NULL,
    post_url text NOT NULL,
    media_url text,
    description text,
    platform_identifier text NOT NULL,
    type text,
    id_sp integer NOT NULL,
    id_child_1 integer,
    id_post integer NOT NULL
);


ALTER TABLE public.post_childs OWNER TO postgres;

--
-- Name: post_childs_id_child_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.post_childs_id_child_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.post_childs_id_child_seq OWNER TO postgres;

--
-- Name: post_childs_id_child_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.post_childs_id_child_seq OWNED BY public.post_childs.id_child;


--
-- Name: posts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.posts (
    id_post integer NOT NULL,
    type character varying(50),
    create_at timestamp without time zone NOT NULL,
    id_seller integer NOT NULL
);


ALTER TABLE public.posts OWNER TO postgres;

--
-- Name: posts_id_post_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.posts_id_post_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.posts_id_post_seq OWNER TO postgres;

--
-- Name: posts_id_post_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.posts_id_post_seq OWNED BY public.posts.id_post;


--
-- Name: potential_customers_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.potential_customers_v2 (
    id_pc text NOT NULL,
    name text NOT NULL,
    link_to_profile text,
    d_platform character varying(50),
    id_sp integer NOT NULL
);


ALTER TABLE public.potential_customers_v2 OWNER TO postgres;

--
-- Name: products_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.products_v2 (
    id_product integer NOT NULL,
    description text,
    name text NOT NULL,
    price numeric(18,2) NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    media text,
    id_seller integer NOT NULL,
    id_category integer NOT NULL
);


ALTER TABLE public.products_v2 OWNER TO postgres;

--
-- Name: products_v2_id_product_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.products_v2_id_product_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.products_v2_id_product_seq OWNER TO postgres;

--
-- Name: products_v2_id_product_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.products_v2_id_product_seq OWNED BY public.products_v2.id_product;


--
-- Name: sales; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sales (
    id_sale timestamp without time zone NOT NULL,
    amount numeric(18,2) NOT NULL,
    effectued_at timestamp without time zone NOT NULL,
    from_number text NOT NULL,
    from_name character varying(50) NOT NULL,
    description text,
    id_spn integer NOT NULL,
    id_order_m integer NOT NULL,
    id_pc text NOT NULL
);


ALTER TABLE public.sales OWNER TO postgres;

--
-- Name: seller_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.seller_v2 (
    id_seller integer NOT NULL,
    email text NOT NULL,
    username text NOT NULL,
    id_provider public.platforms_pro,
    phone_number text,
    firebase_uid text NOT NULL
);


ALTER TABLE public.seller_v2 OWNER TO postgres;

--
-- Name: seller_v2_id_seller_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seller_v2_id_seller_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.seller_v2_id_seller_seq OWNER TO postgres;

--
-- Name: seller_v2_id_seller_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.seller_v2_id_seller_seq OWNED BY public.seller_v2.id_seller;


--
-- Name: sellers_phone_number_e; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sellers_phone_number_e (
    id_spn integer NOT NULL,
    phone_number character varying(50) NOT NULL,
    associated_name text NOT NULL,
    id_pm integer NOT NULL,
    id_seller integer NOT NULL
);


ALTER TABLE public.sellers_phone_number_e OWNER TO postgres;

--
-- Name: sellers_phone_number_e_id_spn_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sellers_phone_number_e_id_spn_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sellers_phone_number_e_id_spn_seq OWNER TO postgres;

--
-- Name: sellers_phone_number_e_id_spn_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sellers_phone_number_e_id_spn_seq OWNED BY public.sellers_phone_number_e.id_spn;


--
-- Name: stocks_child; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.stocks_child (
    id_st_ch integer NOT NULL,
    price numeric(15,2) NOT NULL,
    action_at timestamp without time zone NOT NULL,
    input numeric(15,2),
    output numeric(15,2),
    d_product_number numeric(15,2),
    d_variant_number numeric(15,2),
    product_name text,
    variant_name text,
    id_product integer NOT NULL,
    id_variant integer NOT NULL,
    id_mv integer NOT NULL
);


ALTER TABLE public.stocks_child OWNER TO postgres;

--
-- Name: stocks_child_id_st_ch_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.stocks_child_id_st_ch_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.stocks_child_id_st_ch_seq OWNER TO postgres;

--
-- Name: stocks_child_id_st_ch_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.stocks_child_id_st_ch_seq OWNED BY public.stocks_child.id_st_ch;


--
-- Name: stocks_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.stocks_v2 (
    id_mv integer NOT NULL,
    description text,
    created_at timestamp without time zone NOT NULL,
    id_order_m integer
);


ALTER TABLE public.stocks_v2 OWNER TO postgres;

--
-- Name: stocks_v2_id_mv_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.stocks_v2_id_mv_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.stocks_v2_id_mv_seq OWNER TO postgres;

--
-- Name: stocks_v2_id_mv_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.stocks_v2_id_mv_seq OWNED BY public.stocks_v2.id_mv;


--
-- Name: supported_platforms_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.supported_platforms_v2 (
    id_sp integer NOT NULL,
    label character varying(250)
);


ALTER TABLE public.supported_platforms_v2 OWNER TO postgres;

--
-- Name: supported_platforms_v2_id_sp_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.supported_platforms_v2_id_sp_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.supported_platforms_v2_id_sp_seq OWNER TO postgres;

--
-- Name: supported_platforms_v2_id_sp_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.supported_platforms_v2_id_sp_seq OWNED BY public.supported_platforms_v2.id_sp;


--
-- Name: temporary_product; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.temporary_product (
    id_temp_product integer NOT NULL,
    description text,
    name text NOT NULL,
    price numeric(18,2) NOT NULL,
    media text,
    id_category integer NOT NULL,
    id_seller integer NOT NULL,
    state boolean
);


ALTER TABLE public.temporary_product OWNER TO postgres;

--
-- Name: temporary_product_id_temp_product_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.temporary_product_id_temp_product_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.temporary_product_id_temp_product_seq OWNER TO postgres;

--
-- Name: temporary_product_id_temp_product_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.temporary_product_id_temp_product_seq OWNED BY public.temporary_product.id_temp_product;


--
-- Name: tokens_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tokens_v2 (
    id_token integer NOT NULL,
    token text NOT NULL,
    expired_at timestamp without time zone,
    id_seller integer NOT NULL
);


ALTER TABLE public.tokens_v2 OWNER TO postgres;

--
-- Name: tokens_v2_id_token_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tokens_v2_id_token_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tokens_v2_id_token_seq OWNER TO postgres;

--
-- Name: tokens_v2_id_token_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tokens_v2_id_token_seq OWNED BY public.tokens_v2.id_token;


--
-- Name: transport_type_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transport_type_v2 (
    id_tt integer NOT NULL,
    label character varying(50),
    price_per_ten_km numeric(15,2),
    id_seller integer NOT NULL
);


ALTER TABLE public.transport_type_v2 OWNER TO postgres;

--
-- Name: transport_type_v2_id_tt_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.transport_type_v2_id_tt_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.transport_type_v2_id_tt_seq OWNER TO postgres;

--
-- Name: transport_type_v2_id_tt_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.transport_type_v2_id_tt_seq OWNED BY public.transport_type_v2.id_tt;


--
-- Name: v_managed_accounts; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_managed_accounts AS
SELECT
    NULL::integer AS id_mp,
    NULL::character varying(50) AS d_status,
    NULL::text AS platform_identifier,
    NULL::text AS page_title,
    NULL::text AS associated_media,
    NULL::text AS link_to_platform,
    NULL::character varying(250) AS platform,
    NULL::text AS email,
    NULL::integer AS id_seller,
    NULL::text AS username,
    NULL::text AS access_token,
    NULL::timestamp without time zone AS acctoken_expiration,
    NULL::timestamp without time zone AS reftoken_expiration,
    NULL::text AS token,
    NULL::boolean AS revoked;


ALTER TABLE public.v_managed_accounts OWNER TO postgres;

--
-- Name: v_post_child_media; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_post_child_media AS
 SELECT row_number() OVER (PARTITION BY 1::integer) AS id,
    post_childs.id_child,
    post_childs.media_url AS main_media_url,
    post_childs.description,
    mp.platform_identifier,
    post_childs.type,
    s.id_sp,
    s.label AS supported_platform,
    post_childs.id_child_1,
    p.id_post,
    p.id_seller,
    m.media_url AS additional_media,
    mp.page_title,
    mp.associated_media
   FROM ((((public.post_childs
     LEFT JOIN public.medias m ON ((post_childs.id_child = m.id_child)))
     JOIN public.posts p ON ((post_childs.id_post = p.id_post)))
     JOIN public.supported_platforms_v2 s ON ((post_childs.id_sp = s.id_sp)))
     JOIN public.managed_pages mp ON ((s.id_sp = mp.id_sp)));


ALTER TABLE public.v_post_child_media OWNER TO postgres;

--
-- Name: v_refresh_token_holder; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_refresh_token_holder AS
 SELECT row_number() OVER (ORDER BY 1::integer) AS id,
    managed_pages.id_mp,
    managed_pages.d_status,
    managed_pages.platform_identifier,
    managed_pages.page_title,
    managed_pages.associated_media,
    managed_pages.link_to_platform,
    managed_pages.id_sp,
    managed_pages.id_seller,
    managed_pages.created_at,
    managed_pages.updated_at,
    s.email,
    pat_refresh_tokens.token
   FROM (public.seller_v2 s
     JOIN (public.pat_refresh_tokens
     JOIN public.managed_pages ON ((pat_refresh_tokens.id_mp = managed_pages.id_mp))) ON ((s.id_seller = managed_pages.id_seller)))
  WHERE (pat_refresh_tokens.revoked = false);


ALTER TABLE public.v_refresh_token_holder OWNER TO postgres;

--
-- Name: variant_option_values_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.variant_option_values_v2 (
    id integer NOT NULL,
    id_ov integer NOT NULL,
    id_variant integer NOT NULL
);


ALTER TABLE public.variant_option_values_v2 OWNER TO postgres;

--
-- Name: variant_option_values_v2_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.variant_option_values_v2_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.variant_option_values_v2_id_seq OWNER TO postgres;

--
-- Name: variant_option_values_v2_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.variant_option_values_v2_id_seq OWNED BY public.variant_option_values_v2.id;


--
-- Name: variants_v2; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.variants_v2 (
    id_variant integer NOT NULL,
    title text NOT NULL,
    price numeric(18,2) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone,
    id_product integer NOT NULL
);


ALTER TABLE public.variants_v2 OWNER TO postgres;

--
-- Name: variants_v2_id_variant_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.variants_v2_id_variant_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.variants_v2_id_variant_seq OWNER TO postgres;

--
-- Name: variants_v2_id_variant_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.variants_v2_id_variant_seq OWNED BY public.variants_v2.id_variant;


--
-- Name: category id_category; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category ALTER COLUMN id_category SET DEFAULT nextval('public.category_id_category_seq'::regclass);


--
-- Name: delivery_driver_v2 id_dd; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_driver_v2 ALTER COLUMN id_dd SET DEFAULT nextval('public.delivery_driver_v2_id_dd_seq'::regclass);


--
-- Name: delivery_status_v2 id_status; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_status_v2 ALTER COLUMN id_status SET DEFAULT nextval('public.delivery_status_v2_id_status_seq'::regclass);


--
-- Name: delivery_v2 id_delivery; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_v2 ALTER COLUMN id_delivery SET DEFAULT nextval('public.delivery_v2_id_delivery_seq'::regclass);


--
-- Name: inbox_mother id_im; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inbox_mother ALTER COLUMN id_im SET DEFAULT nextval('public.inbox_mother_id_im_seq'::regclass);


--
-- Name: likes_history id_lh; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.likes_history ALTER COLUMN id_lh SET DEFAULT nextval('public.likes_history_id_lh_seq'::regclass);


--
-- Name: linked_products id_lp; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.linked_products ALTER COLUMN id_lp SET DEFAULT nextval('public.linked_products_id_lp_seq'::regclass);


--
-- Name: managed_pages id_mp; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.managed_pages ALTER COLUMN id_mp SET DEFAULT nextval('public.managed_pages_id_mp_seq'::regclass);


--
-- Name: medias id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medias ALTER COLUMN id SET DEFAULT nextval('public.medias_id_seq'::regclass);


--
-- Name: options_v2 id_option; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.options_v2 ALTER COLUMN id_option SET DEFAULT nextval('public.options_v2_id_option_seq'::regclass);


--
-- Name: options_values_v2 id_ov; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.options_values_v2 ALTER COLUMN id_ov SET DEFAULT nextval('public.options_values_v2_id_ov_seq'::regclass);


--
-- Name: order_details_v2 id_order_details; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_details_v2 ALTER COLUMN id_order_details SET DEFAULT nextval('public.order_details_v2_id_order_details_seq'::regclass);


--
-- Name: order_mother id_order_m; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_mother ALTER COLUMN id_order_m SET DEFAULT nextval('public.order_mother_id_order_m_seq'::regclass);


--
-- Name: order_status_v2 id_status; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_status_v2 ALTER COLUMN id_status SET DEFAULT nextval('public.order_status_v2_id_status_seq'::regclass);


--
-- Name: pat_access_tokens id_pat; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pat_access_tokens ALTER COLUMN id_pat SET DEFAULT nextval('public.pat_access_tokens_id_pat_seq'::regclass);


--
-- Name: pat_refresh_tokens id_prt; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pat_refresh_tokens ALTER COLUMN id_prt SET DEFAULT nextval('public.pat_refresh_tokens_id_prt_seq'::regclass);


--
-- Name: payment_link id_pl; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payment_link ALTER COLUMN id_pl SET DEFAULT nextval('public.payment_link_id_pl_seq'::regclass);


--
-- Name: payment_method_v2 id_pm; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payment_method_v2 ALTER COLUMN id_pm SET DEFAULT nextval('public.payment_method_v2_id_pm_seq'::regclass);


--
-- Name: platform_status id_status; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_status ALTER COLUMN id_status SET DEFAULT nextval('public.platform_status_id_status_seq'::regclass);


--
-- Name: post_childs id_child; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_childs ALTER COLUMN id_child SET DEFAULT nextval('public.post_childs_id_child_seq'::regclass);


--
-- Name: posts id_post; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posts ALTER COLUMN id_post SET DEFAULT nextval('public.posts_id_post_seq'::regclass);


--
-- Name: products_v2 id_product; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_v2 ALTER COLUMN id_product SET DEFAULT nextval('public.products_v2_id_product_seq'::regclass);


--
-- Name: seller_v2 id_seller; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seller_v2 ALTER COLUMN id_seller SET DEFAULT nextval('public.seller_v2_id_seller_seq'::regclass);


--
-- Name: sellers_phone_number_e id_spn; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sellers_phone_number_e ALTER COLUMN id_spn SET DEFAULT nextval('public.sellers_phone_number_e_id_spn_seq'::regclass);


--
-- Name: stocks_child id_st_ch; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stocks_child ALTER COLUMN id_st_ch SET DEFAULT nextval('public.stocks_child_id_st_ch_seq'::regclass);


--
-- Name: stocks_v2 id_mv; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stocks_v2 ALTER COLUMN id_mv SET DEFAULT nextval('public.stocks_v2_id_mv_seq'::regclass);


--
-- Name: supported_platforms_v2 id_sp; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supported_platforms_v2 ALTER COLUMN id_sp SET DEFAULT nextval('public.supported_platforms_v2_id_sp_seq'::regclass);


--
-- Name: temporary_product id_temp_product; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.temporary_product ALTER COLUMN id_temp_product SET DEFAULT nextval('public.temporary_product_id_temp_product_seq'::regclass);


--
-- Name: tokens_v2 id_token; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tokens_v2 ALTER COLUMN id_token SET DEFAULT nextval('public.tokens_v2_id_token_seq'::regclass);


--
-- Name: transport_type_v2 id_tt; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transport_type_v2 ALTER COLUMN id_tt SET DEFAULT nextval('public.transport_type_v2_id_tt_seq'::regclass);


--
-- Name: variant_option_values_v2 id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_option_values_v2 ALTER COLUMN id SET DEFAULT nextval('public.variant_option_values_v2_id_seq'::regclass);


--
-- Name: variants_v2 id_variant; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variants_v2 ALTER COLUMN id_variant SET DEFAULT nextval('public.variants_v2_id_variant_seq'::regclass);


--
-- Data for Name: category; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.category (id_category, val, desc_) FROM stdin;
1	Electronics	Consumer electronics, gadgets, and electronic devices
2	Computers & Laptops	Desktop computers, laptops, tablets, and computer accessories
3	Mobile Phones	Smartphones, basic phones, and mobile accessories
4	Audio & Video	Headphones, speakers, cameras, TVs, and audio/video equipment
5	Gaming	Video games, gaming consoles, and gaming accessories
6	Smart Home	Home automation devices, smart speakers, and IoT products
7	Men's Clothing	Clothing and apparel for men
8	Women's Clothing	Clothing and apparel for women
9	Kids & Baby Clothing	Clothing for children and babies
10	Shoes & Footwear	All types of footwear for men, women, and children
11	Accessories & Jewelry	Fashion accessories, jewelry, watches, and bags
12	Activewear & Sportswear	Sports clothing, gym wear, and athletic apparel
13	Home Decor	Decorative items, artwork, and home styling products
14	Furniture	Indoor and outdoor furniture for all rooms
15	Kitchen & Dining	Cookware, dinnerware, kitchen appliances, and utensils
16	Bedding & Bath	Bed linens, towels, bathroom accessories, and sleep products
17	Garden & Outdoor	Gardening supplies, outdoor furniture, and lawn care equipment
18	Home Improvement	Tools, hardware, paint, and home renovation supplies
19	Beauty & Personal Care	Cosmetics, skincare, haircare, and personal hygiene products
20	Health & Wellness	Vitamins, supplements, medical supplies, and health products
21	Fitness Equipment	Exercise machines, weights, and fitness accessories
22	Pharmacy	Over-the-counter medications and pharmaceutical products
23	Car Parts & Accessories	Vehicle parts, accessories, and automotive supplies
24	Motorcycles & ATVs	Motorcycles, ATVs, and related parts and accessories
25	Car Electronics	GPS systems, dash cams, car audio, and automotive electronics
26	Sports Equipment	Equipment for various sports and recreational activities
27	Outdoor Recreation	Camping, hiking, fishing, and outdoor adventure gear
28	Bicycles	Bikes, bike parts, and cycling accessories
29	Water Sports	Swimming, surfing, boating, and water activity equipment
30	Books	Physical and digital books across all genres
31	Movies & TV	DVDs, Blu-rays, digital movies, and TV show collections
32	Music	CDs, vinyl records, digital music, and musical instruments
33	Musical Instruments	Guitars, pianos, drums, and all musical instruments
34	Groceries	Food items, snacks, and everyday grocery products
35	Beverages	Drinks, juices, coffee, tea, and alcoholic beverages
36	Gourmet Food	Specialty foods, organic products, and premium food items
37	Pet Food	Food and treats for dogs, cats, and other pets
38	Toys & Games	Toys for all ages, board games, and educational toys
39	Baby Products	Baby care items, strollers, car seats, and infant supplies
40	Kids' Furniture	Furniture designed specifically for children
41	Office Supplies	Stationery, office equipment, and workplace essentials
42	Business Equipment	Printers, scanners, office furniture, and business machines
43	Industrial & Scientific	Professional tools, laboratory equipment, and industrial supplies
44	Pet Supplies	General pet care products and accessories
45	Dog Supplies	Products specifically for dogs
46	Cat Supplies	Products specifically for cats
47	Fish & Aquatic Pets	Aquarium supplies and fish care products
48	Arts & Crafts	Art supplies, craft materials, and creative hobby items
49	Sewing & Knitting	Fabric, yarn, sewing machines, and textile crafts
50	Collectibles	Antiques, collectible items, and rare finds
51	Digital Services	Software, apps, digital subscriptions, and online services
52	Gift Cards	Gift certificates and prepaid cards
53	Professional Services	Consulting, repairs, installations, and professional assistance
54	Travel & Luggage	Suitcases, travel accessories, and travel-related products
55	Religious & Spiritual	Religious books, spiritual items, and ceremonial products
56	Party Supplies	Decorations, party favors, and event planning items
57	Seasonal Items	Holiday decorations, seasonal products, and special occasion items
58	Other	Miscellaneous items that don't fit other categories
\.


--
-- Data for Name: comments_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.comments_v2 (id_comment, message, created_at, deleted, id_child) FROM stdin;
\.


--
-- Data for Name: deliveries_state; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.deliveries_state (id_delivery, id_status, state_at) FROM stdin;
\.


--
-- Data for Name: delivery_driver_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.delivery_driver_v2 (id_dd, name, phone_number, id_tt, id_seller) FROM stdin;
\.


--
-- Data for Name: delivery_status_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.delivery_status_v2 (id_status, status) FROM stdin;
\.


--
-- Data for Name: delivery_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.delivery_v2 (id_delivery, shipping_address, ended_at, phone_number, started_at, d_status, id_order_m, id_dd) FROM stdin;
\.


--
-- Data for Name: inbox_child; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inbox_child (id_ic, message, media, id_pc, id_seller, id_im) FROM stdin;
\.


--
-- Data for Name: inbox_mother; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inbox_mother (id_im, id_seller, id_pc) FROM stdin;
\.


--
-- Data for Name: likes_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.likes_history (id_lh, created_at, deleted, id_child, id_pc) FROM stdin;
\.


--
-- Data for Name: linked_products; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.linked_products (id_lp, id_product, id_post) FROM stdin;
\.


--
-- Data for Name: managed_pages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.managed_pages (id_mp, d_status, platform_identifier, page_title, associated_media, link_to_platform, id_sp, id_seller, created_at, updated_at) FROM stdin;
20	active	757064984155341	Pejy	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-1/523951476_122095802708962486_4305956395115047822_n.jpg?stp=c0.24.736.736a_dst-jpg_s200x200_tt6&_nc_cat=102&ccb=1-7&_nc_sid=f907e8&_nc_eui2=AeE50e9fbDucH-I337NIJtTKDSyAOHI73DQNLIA4cjvcNFRn39qDQPk0QMgl7cVc-nCmiSG_Dc3ofxhBZ9oxXsE6&_nc_ohc=bSy4W9DBKZ0Q7kNvwGEGuTk&_nc_oc=AdkY8bEo1PISIe9feojM37SIbmt1ls1ETNizAbTRXuu-09tJl4dpvABp6EIbaMr2dhU&_nc_zt=24&_nc_ht=scontent.ftnr2-2.fna&edm=AGaHXAAEAAAA&_nc_gid=Us5yDAkdwWva7VK1-w1FYg&oh=00_AfTvF88EC2Tl3Lkd_ANhFwJbFHsJXn2IvCWCEgW95e1FgA&oe=688FAE0C	https://www.facebook.com/757064984155341	1	1	2025-07-30 12:13:13.036185	2025-07-30 12:13:13.036205
21	active	17841476233815414	busin_ess_123			2	1	2025-07-30 12:13:13.062281	2025-07-30 12:13:13.062309
22	active	683633991503262	Page de Test	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-1/513913904_122097414722932141_2992619681762329114_n.jpg?stp=c0.24.720.720a_dst-jpg_s200x200_tt6&_nc_cat=101&ccb=1-7&_nc_sid=f907e8&_nc_eui2=AeEsHsitPvn2v-pOPQHLqD_r5OfkYAtzl5Tk5-RgC3OXlCoHmonK9snKwNos0TSm4ZbhUMwPj6mpUs-OitohR_6_&_nc_ohc=qMRJqlVNlsAQ7kNvwHzUv0l&_nc_oc=AdmvUyNGly_NWK8TbZmB-ISqBPFd_5qGtg5FypBOVX7HRMcDsfRJIm1yqyG3-lpzxVY&_nc_zt=24&_nc_ht=scontent.ftnr2-2.fna&edm=AGaHXAAEAAAA&_nc_gid=Us5yDAkdwWva7VK1-w1FYg&oh=00_AfQqnYJB3YAd3SffQIc_qdj_4Tu44Baf4njh4CHJerrLLg&oe=688F9CAC	https://www.facebook.com/683633991503262	1	1	2025-07-30 12:13:13.064971	2025-07-30 12:13:13.064989
\.


--
-- Data for Name: medias; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.medias (id, media_url, id_child) FROM stdin;
1	https://scontent.cdninstagram.com/v/t51.82787-15/523473184_17844131958541128_7861740099854992222_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=103&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeFWZAvfK5vAZULtZk3_a3F1qi8Mh8uuZliqLwyHy65mWI0YSYlvC_cXiNZHfD-SOahnQwGXa4fR-iFnWwRI2_Ba&_nc_ohc=27e7rfTe6fMQ7kNvwG5wapx&_nc_oc=Adnwxe0g6Nb__XJ0UlCMK_8h1690u_x2ydAEGSC3tC3aIjiFZmZkUbzO7V9eyKl-Tpk&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfQt_rtvfiOX7gVSTNO0uWqr9KuVlk3GhLVGE1uk_heeow&oe=688D4DE5	102
2	https://scontent.cdninstagram.com/v/t51.82787-15/522702985_17844131955541128_7435726395178942533_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=100&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeG1zWFse043u2a-mpbK0bS2m0OSeU9-vC2bQ5J5T368LUYngj2mwePyTAgLiMxs4BH1lAuHR-XpQ9ZdYorwItwc&_nc_ohc=oSdcyGMLQEYQ7kNvwFNCrZR&_nc_oc=AdnwAsRpliU00stcJ8SDZkiaezSqQuRjkRdviad5VRLRm9Goqep9lhJIrmCaFsgZgOk&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfRGHHP6c0AqNOgm3J4unVAjfgPxPBWNf-dUTlzFKMeaYA&oe=688D4F96	102
3	https://scontent.cdninstagram.com/v/t51.82787-15/523217590_17844131973541128_1551903219632748630_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=107&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeEo9yEdFxBZ7NQiU5YesZnMxrbTr-OBB6zGttOv44EHrLzw1xnAXleAsymQC3kmRJQ_p3bmxINxVqaCayS4-nb-&_nc_ohc=7vgco9O64MkQ7kNvwFstyCX&_nc_oc=AdmHkjANo-L7aXPaJaRs00K6EMwq6c4k8LA7WRTK8wVikvPhmzvwyuVrPUVPtyyNgdQ&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfT_6TIwSWO8cYE0_eo0xciymJlrEqolK_01ZGI7VnJ31A&oe=688D39B9	102
4	https://scontent.cdninstagram.com/v/t51.82787-15/524198791_17844131964541128_6144843531135810076_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=103&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeGcxZIz53aFbRzKQ5Sk4V1z3CcPoTkT-7HcJw-hORP7sfyDxPpEUVv_pfHdKuQv4mK0ufrMSwi5rQQHnV04BBoi&_nc_ohc=bSIrFnuIaA8Q7kNvwF1D4co&_nc_oc=Adlw00z1R_Sy0uGr7ArMdKfBwnnf8urdmR1W2KtPrs5ET-dn5PJQfQsmWY44gwFjcco&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfTdvtkgrbM0H2_Pmpbe9J2cFLmYmg2yrf2F1Tr_Fgl7Kw&oe=688D4256	102
5	https://scontent.cdninstagram.com/v/t51.82787-15/525234042_17844131883541128_4960162907688548777_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=104&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeHZEthH5ME01SkqDIbEu3efbc9g9bCAwXNtz2D1sIDBc1yrwahXELjocw5GnX2VSQcKV9W14x-1zfNDnIVzsQOz&_nc_ohc=l7kTL2JavGQQ7kNvwH-BbiN&_nc_oc=AdmCXa9juhuurpXlEOSjAMIFL7z_5GebTtvQsq1fhqFFe1m7gxRTrTjXO_hxhJiJC80&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfSVQDW_Midp9LZ9wWiSw4LKOzKTF3Kh5RmOMFptZguf6w&oe=688D1FF4	103
6	https://scontent.cdninstagram.com/v/t51.82787-15/523881965_17844118293541128_6138618720886930231_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=105&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeEj2J15JoCBwmnkrhsiewGrxEKXiVleKTPEQpeJWV4pM6Kh6gzxVK2Ua8gHvWV5hOdTV2rAuqAt7y20yUJXGfXa&_nc_ohc=2c-_rqekZlIQ7kNvwGRM-5K&_nc_oc=Adl3NXHtXRCzv4DKm-lbW9RMQvz4swVHbLsN-iGc1uGUKgVuuay6p7tu3TuMnwSTntc&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfSWzTjMVeUVGpCRGa-qggaz0IngIH15XlLLcYC2XV80sQ&oe=688D2705	104
\.


--
-- Data for Name: options_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.options_v2 (id_option, label, id_product) FROM stdin;
6	Size	5
7	fefezf	6
8	Color	7
9	Input voltage	7
\.


--
-- Data for Name: options_values_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.options_values_v2 (id_ov, value_, id_option) FROM stdin;
1	12	6
2	12	7
3	Black	8
4	110 W	9
\.


--
-- Data for Name: order_details_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.order_details_v2 (id_order_details, price, quantity, id_product, id_variant, id_order_m) FROM stdin;
\.


--
-- Data for Name: order_mother; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.order_mother (id_order_m, description, created_at, d_total, d_customer_name, d_status, shipping_address, customer_number, id_pc) FROM stdin;
\.


--
-- Data for Name: order_status_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.order_status_v2 (id_status, label) FROM stdin;
\.


--
-- Data for Name: orders_state; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.orders_state (id_order_m, id_status, state_at) FROM stdin;
\.


--
-- Data for Name: pages_state; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.pages_state (id_mp, id_status, state_at) FROM stdin;
\.


--
-- Data for Name: pat_access_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.pat_access_tokens (id_pat, access_token, expired_at, created_at, id_prt) FROM stdin;
\.


--
-- Data for Name: pat_refresh_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.pat_refresh_tokens (id_prt, token, expired_at, created_at, revoked, id_mp) FROM stdin;
20	EAAuhgnWv5t0BPONVHhZC8Vcv0OvdocopEHzKGrfRh9ENKrgWc0e7JAykLqBQtxCB46x8gWblBsI5gd6cexaNgpGQ2AmShn1ZAcY2d0t6XxuszaSZCTRn3TLEymfwyqktx05ZClws4r9BZATfCpiIr1Kc9TMdJd0SPhra08QdtqFXVAR3pZCHj4n5BPXQqlkm1qjQZAG	2025-08-29 12:13:13.054346	2025-07-30 12:13:13.054816	f	20
21	EAAuhgnWv5t0BPONVHhZC8Vcv0OvdocopEHzKGrfRh9ENKrgWc0e7JAykLqBQtxCB46x8gWblBsI5gd6cexaNgpGQ2AmShn1ZAcY2d0t6XxuszaSZCTRn3TLEymfwyqktx05ZClws4r9BZATfCpiIr1Kc9TMdJd0SPhra08QdtqFXVAR3pZCHj4n5BPXQqlkm1qjQZAG	2025-08-29 12:13:13.06372	2025-07-30 12:13:13.063867	f	21
22	EAAuhgnWv5t0BPAskauANZCk0kd6zuMJV6s8w2jKsmaNZA6RgHQPdiFEovGfrivkL7uWBSNC1BEKjCXP2AFqsFlfK4egqS71FG6dsf70OIWAW8bl0lbmUUNZBrpZBxs10OJjkdp7GUdAnMmgRvi3yoW0xILwVzIPTsAiZCiVZCMc34zsaB7oppQtJ8hvixjpTKXeJK4	2025-08-29 12:13:13.066055	2025-07-30 12:13:13.066176	f	22
\.


--
-- Data for Name: payment_link; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.payment_link (id_pl, p_key, expired_at, d_expired, amount_of_transaction, id_pc, id_order_m) FROM stdin;
\.


--
-- Data for Name: payment_method_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.payment_method_v2 (id_pm, payment_name) FROM stdin;
\.


--
-- Data for Name: platform_status; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.platform_status (id_status, status_labem) FROM stdin;
1	active
2	inactive
\.


--
-- Data for Name: post_childs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.post_childs (id_child, post_url, media_url, description, platform_identifier, type, id_sp, id_child_1, id_post) FROM stdin;
84	https://www.facebook.com/122097004844962486/posts/122095803446962486	\N	Publication	757064984155341_122095803446962486	main_post	1	\N	31
85	https://www.facebook.com/photo.php?fbid=122095803392962486&set=a.122095803440962486&type=3	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523703713_122095803398962486_3236942814440566761_n.jpg?_nc_cat=102&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeFGJxE6M6ytfnOCXY_DzukxoPkjLnwzTFqg-SMufDNMWpUrECyvCrAYXFSnGJLFWQPi8y4R3KZcEbLx6lwKNaHv&_nc_ohc=LTlclTqvL8gQ7kNvwE2vogw&_nc_oc=Adlog49CJPdEJMUC5yzcd4GTA8q4Iq5hOGZXQOftOaMzMXvR_F7x7zYgAyGSPfOUYzc&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=eRd9xeKGd3QoN7U7bKDjYQ&oh=00_AfTDhfrvqXMXV2HLg-fdxhEeP9082fDhIQueFf6Gtgp9Xg&oe=688D3906	\N	122095803392962486	photo	1	84	31
86	https://www.facebook.com/photo.php?fbid=122095803362962486&set=a.122095803440962486&type=3	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523978053_122095803368962486_5439436496908595273_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=106&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeE-h_UYA00ZRf6UY61FMfhBoQRWsY6syuShBFaxjqzK5EoSv_pAnzs7iprKPKbPGwnntRElH6FZRtxiYKdHHo7B&_nc_ohc=sF0hpkRDTQAQ7kNvwENO0pT&_nc_oc=AdlhHmV_n8ajyMvBdYcv3Hfei4cR87yQmOdbu6XsfeFKVQSFFctxgk_5axrjfCLm2CU&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=eRd9xeKGd3QoN7U7bKDjYQ&oh=00_AfTv-2Kq0GbJydlTvfWpH60MbYip1wqfbpuHpfkRldP1kw&oe=688D3559	\N	122095803362962486	photo	1	84	31
87	https://www.facebook.com/122097004844962486/posts/122095802732962486?substory_index=722784407199880	\N		757064984155341_122095802732962486	main_post	1	\N	32
88	https://www.facebook.com/profile.php?id=122097004844962486	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523951476_122095802708962486_4305956395115047822_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=102&ccb=1-7&_nc_sid=6ee11a&_nc_eui2=AeE50e9fbDucH-I337NIJtTKDSyAOHI73DQNLIA4cjvcNFRn39qDQPk0QMgl7cVc-nCmiSG_Dc3ofxhBZ9oxXsE6&_nc_ohc=jyH-KxKrHSEQ7kNvwEDYoQs&_nc_oc=AdkL0l7e6YzuE-iF8_q2XNGqjR8ilDi6191bX2-nJsfaDtElN3Bh8UUw0ygP12ZZCY4&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=eRd9xeKGd3QoN7U7bKDjYQ&oh=00_AfSnR0YjbrOn5bcfsTiMFAuH38dP3SsR59LuxwwWtJp2hw&oe=688D336F	\N	757064984155341_122095802732962486	photo	1	87	32
89	https://www.facebook.com/122115757556932141/posts/122114957294932141	\N	Check schema	683633991503262_122114957294932141	main_post	1	\N	33
90	https://www.facebook.com/photo.php?fbid=122114957090932141&set=a.122097415442932141&type=3	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/524593674_122114957096932141_4503032692274847380_n.jpg?_nc_cat=106&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeHgOtJVcRVf9vNbL-I_9UV1IzWG_9nCjLwjNYb_2cKMvEhhUcPgXEWfJM7jh7C05Whz_--75EkcoxNu14uR-Mha&_nc_ohc=EvyRNaJOA0oQ7kNvwH5zJfo&_nc_oc=AdlXec5HRvuQlvgNq25gfXJ-jaTuFPmhhIQu0_xKb-vYl0lPZqCdB2FKwzDFoU9rCsc&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfTfVQkv13dBf3QImn4X-jK71YtRwy0KTK8DygO4oNujjw&oe=688D4FF9	\N	122114957090932141	photo	1	89	33
91	https://www.facebook.com/photo.php?fbid=122114957198932141&set=a.122097415442932141&type=3	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523094143_122114957210932141_4088153394920142544_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=103&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeEqfJlz6w8CNNkaiQkeKo5W4GMLpxuf32XgYwunG5_fZYKvUX64CdNknXiipxUzvBTcSBgPkNh1Yza9rKB24aZx&_nc_ohc=XfJT-JDHsokQ7kNvwEHMChD&_nc_oc=Adl7YA2jic_0A_KuP5tVfkmopYLHXIcpYksPCez_-lpclTl0BFtEST8vIN1GeDQDLTA&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfQNXAZRYDpRYo42qQN7n6UUcjSbjYuf0c9IsYufmIs6Eg&oe=688D32BE	\N	122114957198932141	photo	1	89	33
92	https://www.facebook.com/photo.php?fbid=122114957186932141&set=a.122097415442932141&type=3	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523146264_122114957204932141_6003498343021502635_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=102&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeHX2ThUV-ClD8tjPqomKA0Yj6XWZXI-TaiPpdZlcj5NqAUJSfU_GgE-hkCrcLQAkFrD-6WGQ0j4KDt6vHpUP4dR&_nc_ohc=QZd-JftkpXAQ7kNvwHUxMOT&_nc_oc=Adnh3cWWPSF0GoutRy4ZY7xokTG9REuq3xTnXElgEBnCzRD8hk1kHtgD8RNTghOqaUg&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfR27CfiuHKNpuRz4RZ_lz5o3DVJHJjqEoJO3eZVpxyvXA&oe=688D2BDD	\N	122114957186932141	photo	1	89	33
93	https://www.facebook.com/122115757556932141/posts/122097453332932141	\N	Test 2	683633991503262_122097453332932141	main_post	1	\N	34
94	https://www.facebook.com/photo.php?fbid=122097453002932141&set=a.122097415442932141&type=3	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514283989_122097453008932141_507726957567822948_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=108&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeHl1Kmh1etlQLeXjB5iwpp7klmvHx3u-0WSWa8fHe77RdTTI-lX_jVVGqfpnP__yIUS9k50V7cyyh1GdxAhoAJF&_nc_ohc=jGYntNg8iKcQ7kNvwGbh2uW&_nc_oc=AdkvhWcYQKYqkh_4wpezMLfyfBrFWdPrBac3RTJe0PFHJb2KhcW3ty5fZCDIS7ckAzU&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfSYsLs-2OWknTz6CymmSl9mxhFudVColhZYaimI2a-KPg&oe=688D22C4	\N	122097453002932141	photo	1	93	34
95	https://www.facebook.com/photo.php?fbid=122097453230932141&set=a.122097415442932141&type=3	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/515104056_122097453248932141_9146316789992363867_n.jpg?_nc_cat=105&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeGEI7T_imj4FEGKbf6b1l7uWYI0FrzAUzdZgjQWvMBTN_qgAiloDa4QwCzCf6mEtejnHfZtTiLFassKAOh9JhaV&_nc_ohc=2OXps6FlOA0Q7kNvwHcMfhx&_nc_oc=Adnx3ikKU27YG1xRywpvKSlAuiM3j1cUrV-Jk2yP6DpT-gaO9MNAUJPhHnyNmRdaFZg&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfRuzhsiDwul119NczWVTSRd8G2Zf2a1IG3kkeM4GwRyHQ&oe=688D20C4	\N	122097453230932141	photo	1	93	34
96	https://www.facebook.com/photo.php?fbid=122097453290932141&set=a.122097415442932141&type=3	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514191692_122097453296932141_7770825472985379872_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=106&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeFLEXdWOk6RqXORRuiViBreMrVl600dcq0ytWXrTR1yrWJYjFbcrbcI7IpqvmxQ3tByFb9eERMZVu-t5jUSrChO&_nc_ohc=oNWzSGnVMT4Q7kNvwEa5hcA&_nc_oc=AdnhsNqpCZsln8e259EWQwGtodStvR9xm7qPhxnW4FYlC0_Zr6yHSgoR1kC3m92vuXE&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfSrYVIiKuok38vlOTWNPpGpzbTSA42iaf_ZJsJVGyoUFw&oe=688D2466	\N	122097453290932141	photo	1	93	34
97	https://www.facebook.com/photo.php?fbid=122097453218932141&set=a.122097415442932141&type=3	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514368772_122097453242932141_3505335620424002402_n.jpg?_nc_cat=111&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeE-IR1y132gVoLWiF4TQPRUwUC5ap3lq7TBQLlqneWrtO3HyIQ-nipMZrAQithFebd-zlBsJGXnN-vh_k8mU7Z1&_nc_ohc=rVPCitFdkQYQ7kNvwGwcLIF&_nc_oc=Adme8hb0voBcFhM0OEIQSmB48Hh6bGSfEhmLOnS5vp21r-DeIJ9vOX-BMI3eSzYxSkY&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfRYP6v5pGroxD3Vzd_KShrjZgSG-SanvNePDC0OxGRbuA&oe=688D4ED1	\N	122097453218932141	photo	1	93	34
98	https://www.facebook.com/122115757556932141/posts/122097415454932141	\N	Test Graph API	683633991503262_122097415454932141	main_post	1	\N	35
99	https://www.facebook.com/photo.php?fbid=122097415400932141&set=a.122097415442932141&type=3	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514338184_122097415406932141_5467583372442251496_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=107&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeFXF9zwY6TQt21TaowVu3tE5MDn5rfPxhHkwOfmt8_GEX9nLm0_fdT-upAfuGFaX_FDIr0npUxDXNW4rUed5Gjk&_nc_ohc=G7xOHxfq764Q7kNvwFolTQX&_nc_oc=AdnLmuJZ1mqd-k-q0gu3MKRa2vVFZqSDWxApukLyYJLxXDF5KJz67mnNPtRlmLoBa3A&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfQKpJvl3boiu-vsu4DLqFdLSFDHz5r6-wVoknaV9OE1TA&oe=688D3263	\N	683633991503262_122097415454932141	photo	1	98	35
100	https://www.facebook.com/122115757556932141/posts/122097414752932141?substory_index=1463942994774004	\N		683633991503262_122097414752932141	main_post	1	\N	36
101	https://www.facebook.com/profile.php?id=122115757556932141	https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/513913904_122097414722932141_2992619681762329114_n.jpg?_nc_cat=101&ccb=1-7&_nc_sid=6ee11a&_nc_eui2=AeEsHsitPvn2v-pOPQHLqD_r5OfkYAtzl5Tk5-RgC3OXlCoHmonK9snKwNos0TSm4ZbhUMwPj6mpUs-OitohR_6_&_nc_ohc=1sSiP1FT1UwQ7kNvwFIYhIZ&_nc_oc=AdnsqWmqdzg__BTp8ycmbmS2JPwjVczTRj_mkdvsBv1LURJStgJ9EMmLFqjlXa12LzI&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfQrG7Xe-W3e3DLtNbAz_mJRuS8F-AnZyQnnoH5UyQjt2w&oe=688D23CF	\N	683633991503262_122097414752932141	photo	1	100	36
102	https://www.instagram.com/p/DMpV2lssRdm/	\N	Ity dia multiple	18511345999050634	CAROUSEL_ALBUM	2	\N	37
103	https://www.instagram.com/p/DMpVwKPMc6g/	\N	Caption	18062426839998462	IMAGE	2	\N	38
104	https://www.instagram.com/p/DMpHeT3MYnv/	\N		17915494014119984	IMAGE	2	\N	39
115	https://www.facebook.com/757064984155341_122101791062962486	\N	Sarzera sy kojakoja	facebook	main_post	1	\N	44
116	https://www.facebook.com/757064984155341_122101791062962486	https://www.facebook.com/122101790786962486	Sarzera	facebook	photo	1	115	44
117	https://www.facebook.com/757064984155341_122101791062962486	https://www.facebook.com/122101790954962486	Kojakoja	facebook	photo	1	115	44
118	https://www.facebook.com/683633991503262_122117369366932141	\N	Sarzera sy kojakoja	facebook	main_post	1	\N	44
119	https://www.facebook.com/683633991503262_122117369366932141	https://www.facebook.com/122117369240932141	Sarzera	facebook	photo	1	118	44
120	https://www.facebook.com/683633991503262_122117369366932141	https://www.facebook.com/122117369306932141	Kojakoja	facebook	photo	1	118	44
121	https://www.facebook.com/757064984155341_122101793162962486	\N	V2 V2 V2	facebook	main_post	1	\N	45
122	https://www.facebook.com/757064984155341_122101793162962486	https://www.facebook.com/122101793102962486	Sarzera beeee	122101793102962486	photo	1	121	45
123	https://www.facebook.com/683633991503262_122117369918932141	\N	V2 V2 V2	facebook	main_post	1	\N	45
124	https://www.facebook.com/683633991503262_122117369918932141	https://www.facebook.com/122117369864932141	Sarzera beeee	122117369864932141	photo	1	123	45
\.


--
-- Data for Name: posts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.posts (id_post, type, create_at, id_seller) FROM stdin;
31	facebook_post	2025-07-28 15:10:03.115884	1
32	facebook_post	2025-07-28 15:10:03.116036	1
33	facebook_post	2025-07-28 15:10:03.1161	1
34	facebook_post	2025-07-28 15:10:03.116153	1
35	facebook_post	2025-07-28 15:10:03.116203	1
36	facebook_post	2025-07-28 15:10:03.116252	1
37	instagram_post	2025-07-28 09:11:14	1
38	instagram_post	2025-07-28 09:10:22	1
39	instagram_post	2025-07-28 07:05:35	1
44	post	2025-08-02 16:27:21.027305	1
45	post	2025-08-02 16:30:34.358655	1
\.


--
-- Data for Name: potential_customers_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.potential_customers_v2 (id_pc, name, link_to_profile, d_platform, id_sp) FROM stdin;
\.


--
-- Data for Name: products_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.products_v2 (id_product, description, name, price, created_at, updated_at, media, id_seller, id_category) FROM stdin;
5	faef	reg	10.00	2025-07-25 15:35:38.375704	2025-07-25 15:35:38.375733	https://tqilotxiysbnucjdtxtw.supabase.co/storage/v1/object/public/products/uploads/1753446852417-9tuz6e7itsf.jpg	1	1
6	efezfz	efezf	1.00	2025-07-25 15:38:07.55119	2025-07-25 15:38:07.551205	https://tqilotxiysbnucjdtxtw.supabase.co/storage/v1/object/public/products/uploads/1753446852417-9tuz6e7itsf.jpg	1	1
7	Power up your devices at lightning speed with the SmartCharge Pro 65W GaN USB-C Charger — the ultimate compact charging solution for all your gadgets. Built with next-gen GaN (Gallium Nitride) technology, this charger delivers more power in a smaller size, ensuring efficient, cool, and safe charging for everything from your MacBook or iPad Pro to Android smartphones and wireless earbuds.  Equipped with two USB-C Power Delivery ports and one USB-A Quick Charge 3.0 port, it can charge up to three devices simultaneously without sacrificing speed. Whether you're at home, in the office, or traveling, the SmartCharge Pro automatically adjusts voltage and current to maximize charging speed and protect your devices.  With full support for universal voltage (100–240V) and built-in protection against overheating, overvoltage, and short-circuits, it's not just powerful — it's also safe and reliable. Say goodbye to bulky chargers and hello to streamlined power with this sleek, travel-ready powerhouse.	SmartCharge Pro 65W GaN USB-C Charger	39.99	2025-08-02 15:49:32.420073	2025-08-02 15:49:32.420076	https://tqilotxiysbnucjdtxtw.supabase.co/storage/v1/object/public/products/uploads/1754138933439-rqc92nl5g5c.jpg	1	1
\.


--
-- Data for Name: sales; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sales (id_sale, amount, effectued_at, from_number, from_name, description, id_spn, id_order_m, id_pc) FROM stdin;
\.


--
-- Data for Name: seller_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.seller_v2 (id_seller, email, username, id_provider, phone_number, firebase_uid) FROM stdin;
1	nyavorandrianarisoa@gmail.com	Ny Avohasina Mampandry Randrianarisoa (ITU)	google	\N	3VOHm2AhKXOzYx8Q8rhCuIMVz4H2
2	baptiste@yopmail.com	Jean	basic	\N	BoXjaAkK2ZaZHoISYthrQM5ylGM2
\.


--
-- Data for Name: sellers_phone_number_e; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sellers_phone_number_e (id_spn, phone_number, associated_name, id_pm, id_seller) FROM stdin;
\.


--
-- Data for Name: stocks_child; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.stocks_child (id_st_ch, price, action_at, input, output, d_product_number, d_variant_number, product_name, variant_name, id_product, id_variant, id_mv) FROM stdin;
\.


--
-- Data for Name: stocks_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.stocks_v2 (id_mv, description, created_at, id_order_m) FROM stdin;
\.


--
-- Data for Name: supported_platforms_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.supported_platforms_v2 (id_sp, label) FROM stdin;
1	facebook
2	instagram
3	x
4	thread
\.


--
-- Data for Name: temporary_product; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.temporary_product (id_temp_product, description, name, price, media, id_category, id_seller, state) FROM stdin;
4	faef	reg	10.00	https://tqilotxiysbnucjdtxtw.supabase.co/storage/v1/object/public/products/uploads/1753446852417-9tuz6e7itsf.jpg	1	1	t
5	faef	reg	10.00	https://tqilotxiysbnucjdtxtw.supabase.co/storage/v1/object/public/products/uploads/1753446852417-9tuz6e7itsf.jpg	1	1	t
6	efezfz	efezf	1.00	https://tqilotxiysbnucjdtxtw.supabase.co/storage/v1/object/public/products/uploads/1753447078976-jwsi286etb.jpg	1	1	t
2	Koto	Jeane	0.00	Baptiste	1	1	t
7	Power up your devices at lightning speed with the SmartCharge Pro 65W GaN USB-C Charger — the ultimate compact charging solution for all your gadgets. Built with next-gen GaN (Gallium Nitride) technology, this charger delivers more power in a smaller size, ensuring efficient, cool, and safe charging for everything from your MacBook or iPad Pro to Android smartphones and wireless earbuds.  Equipped with two USB-C Power Delivery ports and one USB-A Quick Charge 3.0 port, it can charge up to three devices simultaneously without sacrificing speed. Whether you're at home, in the office, or traveling, the SmartCharge Pro automatically adjusts voltage and current to maximize charging speed and protect your devices.  With full support for universal voltage (100–240V) and built-in protection against overheating, overvoltage, and short-circuits, it's not just powerful — it's also safe and reliable. Say goodbye to bulky chargers and hello to streamlined power with this sleek, travel-ready powerhouse.	SmartCharge Pro 65W GaN USB-C Charger	39.99	https://tqilotxiysbnucjdtxtw.supabase.co/storage/v1/object/public/products/uploads/1754138933439-rqc92nl5g5c.jpg	1	1	f
\.


--
-- Data for Name: tokens_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tokens_v2 (id_token, token, expired_at, id_seller) FROM stdin;
1	eyJhbGciOiJSUzI1NiIsImtpZCI6ImE4ZGY2MmQzYTBhNDRlM2RmY2RjYWZjNmRhMTM4Mzc3NDU5ZjliMDEiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiTnkgQXZvaGFzaW5hIE1hbXBhbmRyeSBSYW5kcmlhbmFyaXNvYSAoSVRVKSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NJXzRhaER6dThyTWJyYW5yNFJSMnFBbWViTWZ2ZlQ4Z0lLcXBJdkhEQWgzcmhLRVZRPXM5Ni1jIiwiaXNzIjoiaHR0cHM6Ly9zZWN1cmV0b2tlbi5nb29nbGUuY29tL3NvY2lhbC1tZWRpYS1jb21tZXJjZSIsImF1ZCI6InNvY2lhbC1tZWRpYS1jb21tZXJjZSIsImF1dGhfdGltZSI6MTc1MjkzMTcwNywidXNlcl9pZCI6IjNWT0htMkFoS1hPell4OFE4cmhDdUlNVno0SDIiLCJzdWIiOiIzVk9IbTJBaEtYT3pZeDhROHJoQ3VJTVZ6NEgyIiwiaWF0IjoxNzUyOTMxNzA3LCJleHAiOjE3NTI5MzUzMDcsImVtYWlsIjoibnlhdm9yYW5kcmlhbmFyaXNvYUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJnb29nbGUuY29tIjpbIjExMTA0Nzg0NDMwMjIyNDIwMTEwOCJdLCJlbWFpbCI6WyJueWF2b3JhbmRyaWFuYXJpc29hQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6Imdvb2dsZS5jb20ifX0.jIGbjxHmeLQedPU84tyempe_LtcpXm1KHmzYpT4Q6yC-cmj0WRYxA27JpllKKh3v7NphjmBrf87oF2HROiZt6GdChZORdOymvW7cK58koaJHplLyonX5YQsR0cJ-0FqUpsKqnYq-VRibbA6ABMYJ3V2gRQuQMXKlCvsz9sXmnLMYNCoKlXWmCV7USzXHZWZFEOs4hvE0angE0gL8h-JJr7yzaETxYPxZJ5tn7yu_dcNqLLMiUhR4tUfwLMyumUmgnH4fT3ViIWOgsm8gWzgWVzQ6FIh0om5-JdlVCGfhgApfy0jC_TwH4XA7QCU1GyIXN3TAg-sJlpNPTJLiauNj_A	2025-07-19 16:58:30.448552	1
2	eyJhbGciOiJSUzI1NiIsImtpZCI6IjZkZTQwZjA0ODgxYzZhMDE2MTFlYjI4NGE0Yzk1YTI1MWU5MTEyNTAiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiTnkgQXZvaGFzaW5hIE1hbXBhbmRyeSBSYW5kcmlhbmFyaXNvYSAoSVRVKSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NJXzRhaER6dThyTWJyYW5yNFJSMnFBbWViTWZ2ZlQ4Z0lLcXBJdkhEQWgzcmhLRVZRPXM5Ni1jIiwiaXNzIjoiaHR0cHM6Ly9zZWN1cmV0b2tlbi5nb29nbGUuY29tL3NvY2lhbC1tZWRpYS1jb21tZXJjZSIsImF1ZCI6InNvY2lhbC1tZWRpYS1jb21tZXJjZSIsImF1dGhfdGltZSI6MTc1MzA4MzE3MywidXNlcl9pZCI6IjNWT0htMkFoS1hPell4OFE4cmhDdUlNVno0SDIiLCJzdWIiOiIzVk9IbTJBaEtYT3pZeDhROHJoQ3VJTVZ6NEgyIiwiaWF0IjoxNzUzMDgzMTczLCJleHAiOjE3NTMwODY3NzMsImVtYWlsIjoibnlhdm9yYW5kcmlhbmFyaXNvYUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJnb29nbGUuY29tIjpbIjExMTA0Nzg0NDMwMjIyNDIwMTEwOCJdLCJlbWFpbCI6WyJueWF2b3JhbmRyaWFuYXJpc29hQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6Imdvb2dsZS5jb20ifX0.CEuC1CxGS0DwhFhWQWTlerGEHAQRCRFnOwY0SsrwQae50rX8s0ukau_T9_0neCnvFCLZV-KRlZ3v3PKuUkjWXNCQ5D4ycyz6W2gI9juwhxqEsEGVNiMEe3319FgkhPJXrIy4CYxLKkvxqjtVGRhh0cW9gLcB2LnJeoIyTL1Mt1gNXvhrDonstErmBojwBgnywg68SiCtwmskOJsAvglvEix1cZDXhAlYF5TZv4wS-ywaijb2eh6n68s2Im2wHuWH7MLhjBbiAAgNEoYs4YK06GdL2MR-lvS-8muxb-UlcPqZUfS4LVuf7Iz3NRB7fyFUNOTYx9SdsWAfTMPzTk1Cgw	2025-07-21 11:02:53.688907	1
4	eyJhbGciOiJSUzI1NiIsImtpZCI6IjZkZTQwZjA0ODgxYzZhMDE2MTFlYjI4NGE0Yzk1YTI1MWU5MTEyNTAiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vc29jaWFsLW1lZGlhLWNvbW1lcmNlIiwiYXVkIjoic29jaWFsLW1lZGlhLWNvbW1lcmNlIiwiYXV0aF90aW1lIjoxNzUzNTMxNTY2LCJ1c2VyX2lkIjoiQm9YamFBa0syWmFaSG9JU1l0aHJRTTV5bEdNMiIsInN1YiI6IkJvWGphQWtLMlphWkhvSVNZdGhyUU01eWxHTTIiLCJpYXQiOjE3NTM1MzE1NjYsImV4cCI6MTc1MzUzNTE2NiwiZW1haWwiOiJiYXB0aXN0ZUB5b3BtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJiYXB0aXN0ZUB5b3BtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6InBhc3N3b3JkIn19.M6Wx2mylXYQAE8Eu86uDkAo2UUU2YNz6HrKlZ3FqOyToGnqmmCoYLTZehTMbWGptTLfWwRKSBS1sNAGbPw9v-SKlI_VJVKnSwtqXGPg75T-RhsmxFHbn9KCdqqMxUH1cYGWBwGXxPB70q8ZoRDEsl5fodnAoiqfudnzIc475oS-4rcm_QkH-9LXtTzYcc9dyHW9nxHtfd4YvIlh96mDEKRlbyZFIUZ4A7E1Z2i8i-DhFW--3k4Z9j4h70G4QkO2F4D2i4XYfLvldUc3LRQ-ddlHJYfrCtMb7hPaT7O84NFd0TZWQTlvqrq-2lKK909OgM1n1m3YUGYrK8CVjf4-yOw	2025-07-26 15:36:10.167093	2
3	eyJhbGciOiJSUzI1NiIsImtpZCI6IjZkZTQwZjA0ODgxYzZhMDE2MTFlYjI4NGE0Yzk1YTI1MWU5MTEyNTAiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiTnkgQXZvaGFzaW5hIE1hbXBhbmRyeSBSYW5kcmlhbmFyaXNvYSAoSVRVKSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NJXzRhaER6dThyTWJyYW5yNFJSMnFBbWViTWZ2ZlQ4Z0lLcXBJdkhEQWgzcmhLRVZRPXM5Ni1jIiwiaXNzIjoiaHR0cHM6Ly9zZWN1cmV0b2tlbi5nb29nbGUuY29tL3NvY2lhbC1tZWRpYS1jb21tZXJjZSIsImF1ZCI6InNvY2lhbC1tZWRpYS1jb21tZXJjZSIsImF1dGhfdGltZSI6MTc1MzA4OTcxOSwidXNlcl9pZCI6IjNWT0htMkFoS1hPell4OFE4cmhDdUlNVno0SDIiLCJzdWIiOiIzVk9IbTJBaEtYT3pZeDhROHJoQ3VJTVZ6NEgyIiwiaWF0IjoxNzUzMDg5NzE5LCJleHAiOjE3NTMwOTMzMTksImVtYWlsIjoibnlhdm9yYW5kcmlhbmFyaXNvYUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJnb29nbGUuY29tIjpbIjExMTA0Nzg0NDMwMjIyNDIwMTEwOCJdLCJlbWFpbCI6WyJueWF2b3JhbmRyaWFuYXJpc29hQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6Imdvb2dsZS5jb20ifX0.gSWfnwjn2nqCHDGmyr31VsuD2r1-B_yV-PP1l3f9jpJiHt7M_1YwPmOldCuwmXs9mJfOXH_ge_cU-75wrn7oc3Lo9Q7m56KHz77QIex4jfxqF4qYIbG2a4CU4qRnzcQt23NlMqFcC5T_rqaFnxYBnhVnFZIQCduTp8fSwGWQcuXBoOLT-GtrdrVklhmk9lW5X7KGYtCl6qi0FEkJJ02nUU8mVpiONt1SWxT6pKedtk2A957x2q12qE1T_8LQlAzZhF2d3RF9vHfd8fdXuND-irZ--3-8HQh_b_DMWpC-auA-iY8yQp4eybkMHs6uV1xIIeoEtbklwKz3Yj6Po4cdTg	2025-12-31 12:52:00.943	1
\.


--
-- Data for Name: transport_type_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.transport_type_v2 (id_tt, label, price_per_ten_km, id_seller) FROM stdin;
\.


--
-- Data for Name: variant_option_values_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.variant_option_values_v2 (id, id_ov, id_variant) FROM stdin;
\.


--
-- Data for Name: variants_v2; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.variants_v2 (id_variant, title, price, created_at, updated_at, id_product) FROM stdin;
\.


--
-- Name: category_id_category_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.category_id_category_seq', 58, true);


--
-- Name: delivery_driver_v2_id_dd_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.delivery_driver_v2_id_dd_seq', 1, false);


--
-- Name: delivery_status_v2_id_status_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.delivery_status_v2_id_status_seq', 1, false);


--
-- Name: delivery_v2_id_delivery_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.delivery_v2_id_delivery_seq', 1, false);


--
-- Name: inbox_mother_id_im_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.inbox_mother_id_im_seq', 1, false);


--
-- Name: likes_history_id_lh_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.likes_history_id_lh_seq', 1, false);


--
-- Name: linked_products_id_lp_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.linked_products_id_lp_seq', 1, false);


--
-- Name: managed_pages_id_mp_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.managed_pages_id_mp_seq', 22, true);


--
-- Name: medias_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.medias_id_seq', 6, true);


--
-- Name: options_v2_id_option_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.options_v2_id_option_seq', 9, true);


--
-- Name: options_values_v2_id_ov_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.options_values_v2_id_ov_seq', 4, true);


--
-- Name: order_details_v2_id_order_details_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.order_details_v2_id_order_details_seq', 1, false);


--
-- Name: order_mother_id_order_m_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.order_mother_id_order_m_seq', 1, false);


--
-- Name: order_status_v2_id_status_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.order_status_v2_id_status_seq', 1, false);


--
-- Name: pat_access_tokens_id_pat_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.pat_access_tokens_id_pat_seq', 2, true);


--
-- Name: pat_refresh_tokens_id_prt_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.pat_refresh_tokens_id_prt_seq', 22, true);


--
-- Name: payment_link_id_pl_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.payment_link_id_pl_seq', 1, false);


--
-- Name: payment_method_v2_id_pm_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.payment_method_v2_id_pm_seq', 1, false);


--
-- Name: platform_status_id_status_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.platform_status_id_status_seq', 2, true);


--
-- Name: post_childs_id_child_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.post_childs_id_child_seq', 124, true);


--
-- Name: posts_id_post_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.posts_id_post_seq', 45, true);


--
-- Name: products_v2_id_product_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.products_v2_id_product_seq', 7, true);


--
-- Name: seller_v2_id_seller_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seller_v2_id_seller_seq', 2, true);


--
-- Name: sellers_phone_number_e_id_spn_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sellers_phone_number_e_id_spn_seq', 1, false);


--
-- Name: stocks_child_id_st_ch_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.stocks_child_id_st_ch_seq', 1, false);


--
-- Name: stocks_v2_id_mv_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.stocks_v2_id_mv_seq', 1, false);


--
-- Name: supported_platforms_v2_id_sp_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.supported_platforms_v2_id_sp_seq', 4, true);


--
-- Name: temporary_product_id_temp_product_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.temporary_product_id_temp_product_seq', 7, true);


--
-- Name: tokens_v2_id_token_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.tokens_v2_id_token_seq', 4, true);


--
-- Name: transport_type_v2_id_tt_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.transport_type_v2_id_tt_seq', 1, false);


--
-- Name: variant_option_values_v2_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.variant_option_values_v2_id_seq', 1, false);


--
-- Name: variants_v2_id_variant_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.variants_v2_id_variant_seq', 1, false);


--
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id_category);


--
-- Name: comments_v2 comments_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comments_v2
    ADD CONSTRAINT comments_v2_pkey PRIMARY KEY (id_comment);


--
-- Name: deliveries_state deliveries_state_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deliveries_state
    ADD CONSTRAINT deliveries_state_pkey PRIMARY KEY (id_delivery, id_status);


--
-- Name: delivery_driver_v2 delivery_driver_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_driver_v2
    ADD CONSTRAINT delivery_driver_v2_pkey PRIMARY KEY (id_dd);


--
-- Name: delivery_status_v2 delivery_status_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_status_v2
    ADD CONSTRAINT delivery_status_v2_pkey PRIMARY KEY (id_status);


--
-- Name: delivery_v2 delivery_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_v2
    ADD CONSTRAINT delivery_v2_pkey PRIMARY KEY (id_delivery);


--
-- Name: inbox_child inbox_child_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inbox_child
    ADD CONSTRAINT inbox_child_pkey PRIMARY KEY (id_ic);


--
-- Name: inbox_mother inbox_mother_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inbox_mother
    ADD CONSTRAINT inbox_mother_pkey PRIMARY KEY (id_im);


--
-- Name: likes_history likes_history_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.likes_history
    ADD CONSTRAINT likes_history_pkey PRIMARY KEY (id_lh);


--
-- Name: linked_products linked_products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.linked_products
    ADD CONSTRAINT linked_products_pkey PRIMARY KEY (id_lp);


--
-- Name: managed_pages managed_pages_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.managed_pages
    ADD CONSTRAINT managed_pages_pk UNIQUE (id_sp, platform_identifier);


--
-- Name: managed_pages managed_pages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.managed_pages
    ADD CONSTRAINT managed_pages_pkey PRIMARY KEY (id_mp);


--
-- Name: medias medias_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medias
    ADD CONSTRAINT medias_pkey PRIMARY KEY (id);


--
-- Name: options_v2 options_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.options_v2
    ADD CONSTRAINT options_v2_pkey PRIMARY KEY (id_option);


--
-- Name: options_values_v2 options_values_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.options_values_v2
    ADD CONSTRAINT options_values_v2_pkey PRIMARY KEY (id_ov);


--
-- Name: order_details_v2 order_details_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_details_v2
    ADD CONSTRAINT order_details_v2_pkey PRIMARY KEY (id_order_details);


--
-- Name: order_mother order_mother_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_mother
    ADD CONSTRAINT order_mother_pkey PRIMARY KEY (id_order_m);


--
-- Name: order_status_v2 order_status_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_status_v2
    ADD CONSTRAINT order_status_v2_pkey PRIMARY KEY (id_status);


--
-- Name: orders_state orders_state_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders_state
    ADD CONSTRAINT orders_state_pkey PRIMARY KEY (id_order_m, id_status);


--
-- Name: pages_state pages_state_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pages_state
    ADD CONSTRAINT pages_state_pkey PRIMARY KEY (id_mp, id_status);


--
-- Name: pat_access_tokens pat_access_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pat_access_tokens
    ADD CONSTRAINT pat_access_tokens_pkey PRIMARY KEY (id_pat);


--
-- Name: pat_refresh_tokens pat_refresh_tokens_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pat_refresh_tokens
    ADD CONSTRAINT pat_refresh_tokens_pk UNIQUE (token, id_mp);


--
-- Name: pat_refresh_tokens pat_refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pat_refresh_tokens
    ADD CONSTRAINT pat_refresh_tokens_pkey PRIMARY KEY (id_prt);


--
-- Name: payment_link payment_link_p_key_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payment_link
    ADD CONSTRAINT payment_link_p_key_key UNIQUE (p_key);


--
-- Name: payment_link payment_link_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payment_link
    ADD CONSTRAINT payment_link_pkey PRIMARY KEY (id_pl);


--
-- Name: payment_method_v2 payment_method_v2_payment_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payment_method_v2
    ADD CONSTRAINT payment_method_v2_payment_name_key UNIQUE (payment_name);


--
-- Name: payment_method_v2 payment_method_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payment_method_v2
    ADD CONSTRAINT payment_method_v2_pkey PRIMARY KEY (id_pm);


--
-- Name: platform_status platform_status_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_status
    ADD CONSTRAINT platform_status_pkey PRIMARY KEY (id_status);


--
-- Name: post_childs post_childs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_childs
    ADD CONSTRAINT post_childs_pkey PRIMARY KEY (id_child);


--
-- Name: posts posts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT posts_pkey PRIMARY KEY (id_post);


--
-- Name: potential_customers_v2 potential_customers_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.potential_customers_v2
    ADD CONSTRAINT potential_customers_v2_pkey PRIMARY KEY (id_pc);


--
-- Name: products_v2 products_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_v2
    ADD CONSTRAINT products_v2_pkey PRIMARY KEY (id_product);


--
-- Name: sales sales_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales
    ADD CONSTRAINT sales_pkey PRIMARY KEY (id_sale);


--
-- Name: seller_v2 seller_v2_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seller_v2
    ADD CONSTRAINT seller_v2_email_key UNIQUE (email);


--
-- Name: seller_v2 seller_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seller_v2
    ADD CONSTRAINT seller_v2_pkey PRIMARY KEY (id_seller);


--
-- Name: sellers_phone_number_e sellers_phone_number_e_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sellers_phone_number_e
    ADD CONSTRAINT sellers_phone_number_e_pkey PRIMARY KEY (id_spn);


--
-- Name: stocks_child stocks_child_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stocks_child
    ADD CONSTRAINT stocks_child_pkey PRIMARY KEY (id_st_ch);


--
-- Name: stocks_v2 stocks_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stocks_v2
    ADD CONSTRAINT stocks_v2_pkey PRIMARY KEY (id_mv);


--
-- Name: supported_platforms_v2 supported_platforms_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supported_platforms_v2
    ADD CONSTRAINT supported_platforms_v2_pkey PRIMARY KEY (id_sp);


--
-- Name: temporary_product temporary_product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.temporary_product
    ADD CONSTRAINT temporary_product_pkey PRIMARY KEY (id_temp_product);


--
-- Name: tokens_v2 tokens_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tokens_v2
    ADD CONSTRAINT tokens_v2_pkey PRIMARY KEY (id_token);


--
-- Name: transport_type_v2 transport_type_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transport_type_v2
    ADD CONSTRAINT transport_type_v2_pkey PRIMARY KEY (id_tt);


--
-- Name: variant_option_values_v2 variant_option_values_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_option_values_v2
    ADD CONSTRAINT variant_option_values_v2_pkey PRIMARY KEY (id);


--
-- Name: variants_v2 variants_v2_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variants_v2
    ADD CONSTRAINT variants_v2_pkey PRIMARY KEY (id_variant);


--
-- Name: v_managed_accounts _RETURN; Type: RULE; Schema: public; Owner: postgres
--

CREATE OR REPLACE VIEW public.v_managed_accounts AS
 WITH max_accesstoken AS (
         SELECT pat_access_tokens.id_pat,
            pat_access_tokens.access_token,
            pat_access_tokens.expired_at,
            max(pat_access_tokens.created_at) AS max,
            pat_access_tokens.id_prt
           FROM public.pat_access_tokens
          GROUP BY pat_access_tokens.id_pat
        ), max_refreshtoken AS (
         SELECT pat_refresh_tokens.id_prt,
            pat_refresh_tokens.token,
            pat_refresh_tokens.expired_at,
            max(pat_refresh_tokens.created_at) AS max,
            pat_refresh_tokens.revoked,
            pat_refresh_tokens.id_mp
           FROM public.pat_refresh_tokens
          GROUP BY pat_refresh_tokens.id_prt
        ), max_tokens AS (
         SELECT max_accesstoken.id_pat,
            max_accesstoken.access_token,
            max_accesstoken.expired_at AS acctoken_expiration,
            max_refreshtoken.expired_at AS reftoken_expiration,
            max_refreshtoken.id_prt,
            max_refreshtoken.token,
            max_refreshtoken.revoked,
            max_refreshtoken.id_mp
           FROM (max_accesstoken
             RIGHT JOIN max_refreshtoken ON ((max_accesstoken.id_prt = max_refreshtoken.id_prt)))
        )
 SELECT max_tokens.id_mp,
    managed_pages.d_status,
    managed_pages.platform_identifier,
    managed_pages.page_title,
    managed_pages.associated_media,
    managed_pages.link_to_platform,
    s.label AS platform,
    v.email,
    managed_pages.id_seller,
    v.username,
    max_tokens.access_token,
    max_tokens.acctoken_expiration,
    max_tokens.reftoken_expiration,
    max_tokens.token,
    max_tokens.revoked
   FROM (((public.managed_pages
     JOIN public.supported_platforms_v2 s ON ((managed_pages.id_sp = s.id_sp)))
     JOIN public.seller_v2 v ON ((managed_pages.id_seller = v.id_seller)))
     JOIN max_tokens ON ((managed_pages.id_mp = max_tokens.id_mp)));


--
-- Name: comments_v2 comments_v2_id_child_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comments_v2
    ADD CONSTRAINT comments_v2_id_child_fkey FOREIGN KEY (id_child) REFERENCES public.post_childs(id_child);


--
-- Name: deliveries_state deliveries_state_id_delivery_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deliveries_state
    ADD CONSTRAINT deliveries_state_id_delivery_fkey FOREIGN KEY (id_delivery) REFERENCES public.delivery_v2(id_delivery);


--
-- Name: deliveries_state deliveries_state_id_status_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deliveries_state
    ADD CONSTRAINT deliveries_state_id_status_fkey FOREIGN KEY (id_status) REFERENCES public.delivery_status_v2(id_status);


--
-- Name: delivery_driver_v2 delivery_driver_v2_id_seller_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_driver_v2
    ADD CONSTRAINT delivery_driver_v2_id_seller_fkey FOREIGN KEY (id_seller) REFERENCES public.seller_v2(id_seller);


--
-- Name: delivery_driver_v2 delivery_driver_v2_id_tt_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_driver_v2
    ADD CONSTRAINT delivery_driver_v2_id_tt_fkey FOREIGN KEY (id_tt) REFERENCES public.transport_type_v2(id_tt);


--
-- Name: delivery_v2 delivery_v2_id_dd_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_v2
    ADD CONSTRAINT delivery_v2_id_dd_fkey FOREIGN KEY (id_dd) REFERENCES public.delivery_driver_v2(id_dd);


--
-- Name: delivery_v2 delivery_v2_id_order_m_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_v2
    ADD CONSTRAINT delivery_v2_id_order_m_fkey FOREIGN KEY (id_order_m) REFERENCES public.order_mother(id_order_m);


--
-- Name: inbox_child inbox_child_id_im_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inbox_child
    ADD CONSTRAINT inbox_child_id_im_fkey FOREIGN KEY (id_im) REFERENCES public.inbox_mother(id_im);


--
-- Name: inbox_child inbox_child_id_pc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inbox_child
    ADD CONSTRAINT inbox_child_id_pc_fkey FOREIGN KEY (id_pc) REFERENCES public.potential_customers_v2(id_pc);


--
-- Name: inbox_child inbox_child_id_seller_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inbox_child
    ADD CONSTRAINT inbox_child_id_seller_fkey FOREIGN KEY (id_seller) REFERENCES public.seller_v2(id_seller);


--
-- Name: inbox_mother inbox_mother_id_pc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inbox_mother
    ADD CONSTRAINT inbox_mother_id_pc_fkey FOREIGN KEY (id_pc) REFERENCES public.potential_customers_v2(id_pc);


--
-- Name: inbox_mother inbox_mother_id_seller_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inbox_mother
    ADD CONSTRAINT inbox_mother_id_seller_fkey FOREIGN KEY (id_seller) REFERENCES public.seller_v2(id_seller);


--
-- Name: likes_history likes_history_id_child_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.likes_history
    ADD CONSTRAINT likes_history_id_child_fkey FOREIGN KEY (id_child) REFERENCES public.post_childs(id_child);


--
-- Name: likes_history likes_history_id_pc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.likes_history
    ADD CONSTRAINT likes_history_id_pc_fkey FOREIGN KEY (id_pc) REFERENCES public.potential_customers_v2(id_pc);


--
-- Name: linked_products linked_products_id_post_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.linked_products
    ADD CONSTRAINT linked_products_id_post_fkey FOREIGN KEY (id_post) REFERENCES public.posts(id_post);


--
-- Name: linked_products linked_products_id_product_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.linked_products
    ADD CONSTRAINT linked_products_id_product_fkey FOREIGN KEY (id_product) REFERENCES public.products_v2(id_product);


--
-- Name: managed_pages managed_pages_id_seller_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.managed_pages
    ADD CONSTRAINT managed_pages_id_seller_fkey FOREIGN KEY (id_seller) REFERENCES public.seller_v2(id_seller);


--
-- Name: managed_pages managed_pages_id_sp_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.managed_pages
    ADD CONSTRAINT managed_pages_id_sp_fkey FOREIGN KEY (id_sp) REFERENCES public.supported_platforms_v2(id_sp);


--
-- Name: medias medias_id_child_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medias
    ADD CONSTRAINT medias_id_child_fkey FOREIGN KEY (id_child) REFERENCES public.post_childs(id_child);


--
-- Name: options_v2 options_v2___fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.options_v2
    ADD CONSTRAINT options_v2___fk FOREIGN KEY (id_product) REFERENCES public.products_v2(id_product);


--
-- Name: options_values_v2 options_values_v2_id_option_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.options_values_v2
    ADD CONSTRAINT options_values_v2_id_option_fkey FOREIGN KEY (id_option) REFERENCES public.options_v2(id_option);


--
-- Name: order_details_v2 order_details_v2_id_order_m_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_details_v2
    ADD CONSTRAINT order_details_v2_id_order_m_fkey FOREIGN KEY (id_order_m) REFERENCES public.order_mother(id_order_m);


--
-- Name: order_details_v2 order_details_v2_id_product_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_details_v2
    ADD CONSTRAINT order_details_v2_id_product_fkey FOREIGN KEY (id_product) REFERENCES public.products_v2(id_product);


--
-- Name: order_details_v2 order_details_v2_id_variant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_details_v2
    ADD CONSTRAINT order_details_v2_id_variant_fkey FOREIGN KEY (id_variant) REFERENCES public.variants_v2(id_variant);


--
-- Name: order_mother order_mother_id_pc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_mother
    ADD CONSTRAINT order_mother_id_pc_fkey FOREIGN KEY (id_pc) REFERENCES public.potential_customers_v2(id_pc);


--
-- Name: orders_state orders_state_id_order_m_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders_state
    ADD CONSTRAINT orders_state_id_order_m_fkey FOREIGN KEY (id_order_m) REFERENCES public.order_mother(id_order_m);


--
-- Name: orders_state orders_state_id_status_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders_state
    ADD CONSTRAINT orders_state_id_status_fkey FOREIGN KEY (id_status) REFERENCES public.order_status_v2(id_status);


--
-- Name: pages_state pages_state_id_mp_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pages_state
    ADD CONSTRAINT pages_state_id_mp_fkey FOREIGN KEY (id_mp) REFERENCES public.managed_pages(id_mp);


--
-- Name: pages_state pages_state_id_status_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pages_state
    ADD CONSTRAINT pages_state_id_status_fkey FOREIGN KEY (id_status) REFERENCES public.platform_status(id_status);


--
-- Name: pat_access_tokens pat_access_tokens_id_prt_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pat_access_tokens
    ADD CONSTRAINT pat_access_tokens_id_prt_fkey FOREIGN KEY (id_prt) REFERENCES public.pat_refresh_tokens(id_prt);


--
-- Name: pat_refresh_tokens pat_refresh_tokens_id_mp_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pat_refresh_tokens
    ADD CONSTRAINT pat_refresh_tokens_id_mp_fkey FOREIGN KEY (id_mp) REFERENCES public.managed_pages(id_mp);


--
-- Name: payment_link payment_link_id_order_m_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payment_link
    ADD CONSTRAINT payment_link_id_order_m_fkey FOREIGN KEY (id_order_m) REFERENCES public.order_mother(id_order_m);


--
-- Name: payment_link payment_link_id_pc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payment_link
    ADD CONSTRAINT payment_link_id_pc_fkey FOREIGN KEY (id_pc) REFERENCES public.potential_customers_v2(id_pc);


--
-- Name: post_childs post_childs_id_child_1_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_childs
    ADD CONSTRAINT post_childs_id_child_1_fkey FOREIGN KEY (id_child_1) REFERENCES public.post_childs(id_child) ON DELETE CASCADE;


--
-- Name: post_childs post_childs_id_post_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_childs
    ADD CONSTRAINT post_childs_id_post_fkey FOREIGN KEY (id_post) REFERENCES public.posts(id_post);


--
-- Name: post_childs post_childs_id_sp_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_childs
    ADD CONSTRAINT post_childs_id_sp_fkey FOREIGN KEY (id_sp) REFERENCES public.supported_platforms_v2(id_sp);


--
-- Name: posts posts_id_seller_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT posts_id_seller_fkey FOREIGN KEY (id_seller) REFERENCES public.seller_v2(id_seller);


--
-- Name: potential_customers_v2 potential_customers_v2_id_sp_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.potential_customers_v2
    ADD CONSTRAINT potential_customers_v2_id_sp_fkey FOREIGN KEY (id_sp) REFERENCES public.supported_platforms_v2(id_sp);


--
-- Name: products_v2 products_v2_id_seller_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_v2
    ADD CONSTRAINT products_v2_id_seller_fkey FOREIGN KEY (id_seller) REFERENCES public.seller_v2(id_seller);


--
-- Name: products_v2 products_v2_products_v2__fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_v2
    ADD CONSTRAINT products_v2_products_v2__fk FOREIGN KEY (id_category) REFERENCES public.category(id_category);


--
-- Name: sales sales_id_order_m_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales
    ADD CONSTRAINT sales_id_order_m_fkey FOREIGN KEY (id_order_m) REFERENCES public.order_mother(id_order_m);


--
-- Name: sales sales_id_pc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales
    ADD CONSTRAINT sales_id_pc_fkey FOREIGN KEY (id_pc) REFERENCES public.potential_customers_v2(id_pc);


--
-- Name: sales sales_id_spn_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales
    ADD CONSTRAINT sales_id_spn_fkey FOREIGN KEY (id_spn) REFERENCES public.sellers_phone_number_e(id_spn);


--
-- Name: sellers_phone_number_e sellers_phone_number_e_id_pm_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sellers_phone_number_e
    ADD CONSTRAINT sellers_phone_number_e_id_pm_fkey FOREIGN KEY (id_pm) REFERENCES public.payment_method_v2(id_pm);


--
-- Name: sellers_phone_number_e sellers_phone_number_e_id_seller_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sellers_phone_number_e
    ADD CONSTRAINT sellers_phone_number_e_id_seller_fkey FOREIGN KEY (id_seller) REFERENCES public.seller_v2(id_seller);


--
-- Name: stocks_child stocks_child_id_mv_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stocks_child
    ADD CONSTRAINT stocks_child_id_mv_fkey FOREIGN KEY (id_mv) REFERENCES public.stocks_v2(id_mv);


--
-- Name: stocks_child stocks_child_id_product_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stocks_child
    ADD CONSTRAINT stocks_child_id_product_fkey FOREIGN KEY (id_product) REFERENCES public.products_v2(id_product);


--
-- Name: stocks_child stocks_child_id_variant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stocks_child
    ADD CONSTRAINT stocks_child_id_variant_fkey FOREIGN KEY (id_variant) REFERENCES public.variants_v2(id_variant);


--
-- Name: stocks_v2 stocks_v2_id_order_m_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stocks_v2
    ADD CONSTRAINT stocks_v2_id_order_m_fkey FOREIGN KEY (id_order_m) REFERENCES public.order_mother(id_order_m);


--
-- Name: temporary_product temporary_product_id_category_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.temporary_product
    ADD CONSTRAINT temporary_product_id_category_fkey FOREIGN KEY (id_category) REFERENCES public.category(id_category);


--
-- Name: temporary_product temporary_product_id_seller_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.temporary_product
    ADD CONSTRAINT temporary_product_id_seller_fkey FOREIGN KEY (id_seller) REFERENCES public.seller_v2(id_seller);


--
-- Name: tokens_v2 tokens_v2_id_seller_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tokens_v2
    ADD CONSTRAINT tokens_v2_id_seller_fkey FOREIGN KEY (id_seller) REFERENCES public.seller_v2(id_seller);


--
-- Name: transport_type_v2 transport_type_v2_id_seller_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transport_type_v2
    ADD CONSTRAINT transport_type_v2_id_seller_fkey FOREIGN KEY (id_seller) REFERENCES public.seller_v2(id_seller);


--
-- Name: variant_option_values_v2 variant_option_values_v2_id_ov_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_option_values_v2
    ADD CONSTRAINT variant_option_values_v2_id_ov_fkey FOREIGN KEY (id_ov) REFERENCES public.options_values_v2(id_ov);


--
-- Name: variant_option_values_v2 variant_option_values_v2_id_variant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_option_values_v2
    ADD CONSTRAINT variant_option_values_v2_id_variant_fkey FOREIGN KEY (id_variant) REFERENCES public.variants_v2(id_variant);


--
-- Name: variants_v2 variants_v2_id_product_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variants_v2
    ADD CONSTRAINT variants_v2_id_product_fkey FOREIGN KEY (id_product) REFERENCES public.products_v2(id_product);


--
-- PostgreSQL database dump complete
--

