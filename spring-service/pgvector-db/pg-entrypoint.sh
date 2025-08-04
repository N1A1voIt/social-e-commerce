#!/bin/bash

exec docker-entrypoint.sh postgres
#psql -U "postgres" -d "postgres" < /docker-entrypoint-initdb.d/init.sql
#pg_dump -U postgres -d postgres -f /docker-entrypoint-initdb.d/init.sql

