#!/bin/bash

# Sales CSV Import Test Script
# This script demonstrates how to use the CSV import API

# Configuration
BASE_URL="http://localhost:8080"
CSV_FILE="ddl/sales.csv"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "Sales CSV Import Test Script"
echo "=========================================="
echo ""

# Check if CSV file exists
if [ ! -f "$CSV_FILE" ]; then
    echo -e "${RED}Error: CSV file not found at $CSV_FILE${NC}"
    exit 1
fi

echo -e "${GREEN}✓${NC} CSV file found: $CSV_FILE"
echo ""

# Get authentication token
echo "Step 1: Authentication"
echo "----------------------"
read -p "Enter your email: " EMAIL
read -sp "Enter your password: " PASSWORD
echo ""

# Login and get token
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token' 2>/dev/null)

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo -e "${RED}✗ Authentication failed${NC}"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Authentication successful${NC}"
echo "Token: ${TOKEN:0:20}..."
echo ""

# Import CSV
echo "Step 2: Importing CSV"
echo "----------------------"
IMPORT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/sales/import" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@$CSV_FILE")

# Check response status
STATUS=$(echo $IMPORT_RESPONSE | jq -r '.status' 2>/dev/null)

if [ "$STATUS" = "200" ]; then
    echo -e "${GREEN}✓ Import successful!${NC}"
    echo ""

    # Extract statistics
    TOTAL_ROWS=$(echo $IMPORT_RESPONSE | jq -r '.data.totalRows')
    SUCCESSFUL=$(echo $IMPORT_RESPONSE | jq -r '.data.successfulImports')
    FAILED=$(echo $IMPORT_RESPONSE | jq -r '.data.failedImports')
    SALES_CREATED=$(echo $IMPORT_RESPONSE | jq -r '.data.salesCreated')
    DETAILS_CREATED=$(echo $IMPORT_RESPONSE | jq -r '.data.salesDetailsCreated')

    echo "Import Statistics:"
    echo "  Total Rows:          $TOTAL_ROWS"
    echo "  Successful Imports:  $SUCCESSFUL"
    echo "  Failed Imports:      $FAILED"
    echo "  Sales Created:       $SALES_CREATED"
    echo "  Details Created:     $DETAILS_CREATED"
    echo ""

    # Show warnings if any
    WARNINGS=$(echo $IMPORT_RESPONSE | jq -r '.data.warnings[]?' 2>/dev/null)
    if [ ! -z "$WARNINGS" ]; then
        echo -e "${YELLOW}Warnings:${NC}"
        echo "$WARNINGS" | while IFS= read -r warning; do
            echo "  - $warning"
        done
        echo ""
    fi

    # Show errors if any
    ERRORS=$(echo $IMPORT_RESPONSE | jq -r '.data.errors[]?' 2>/dev/null)
    if [ ! -z "$ERRORS" ]; then
        echo -e "${RED}Errors:${NC}"
        echo "$ERRORS" | while IFS= read -r error; do
            echo "  - $error"
        done
        echo ""
    fi

elif [ "$STATUS" = "401" ]; then
    echo -e "${RED}✗ Unauthorized: Invalid token${NC}"
    echo "Response: $IMPORT_RESPONSE"
    exit 1
elif [ "$STATUS" = "400" ]; then
    echo -e "${RED}✗ Bad Request: Invalid file or data${NC}"
    echo "Response: $IMPORT_RESPONSE"
    exit 1
else
    echo -e "${RED}✗ Import failed${NC}"
    echo "Response: $IMPORT_RESPONSE"
    exit 1
fi

# Verify import
echo "Step 3: Verification"
echo "----------------------"
SALES_RESPONSE=$(curl -s -X GET "$BASE_URL/api/sales?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")

TOTAL_SALES=$(echo $SALES_RESPONSE | jq -r '.data.totalSales' 2>/dev/null)

if [ ! -z "$TOTAL_SALES" ] && [ "$TOTAL_SALES" != "null" ]; then
    echo -e "${GREEN}✓ Verification successful${NC}"
    echo "Total sales in database: $TOTAL_SALES"
else
    echo -e "${YELLOW}⚠ Could not verify sales count${NC}"
fi

echo ""
echo "=========================================="
echo "Import process completed!"
echo "=========================================="

