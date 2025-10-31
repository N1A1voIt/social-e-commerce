import json
import re
from datetime import datetime

from nlp_analyzer.db.order_repository import OrderRepository

def extract_sales_story(user_id: int):

    range = clean_and_parse_json("{ \"start_date\": \"1990-01-01\", \"end_date\": \"3000-01-01\" }")
    start_date = range.get("start_date", "1990-01-01")
    end_date = range.get("end_date", "3000-01-01")
    if isinstance(start_date, str):
        start_date = datetime.fromisoformat(start_date)
    if isinstance(end_date, str):
        end_date = datetime.fromisoformat(end_date)
    order_repository = OrderRepository()
    sales_statistics = order_repository.get_sales_evolution(start_date, end_date, user_id)
    return sales_statistics

def clean_and_parse_json(markdown_json_str):
    cleaned = re.sub(r"^```json\s*|\s*```$", "", markdown_json_str.strip(), flags=re.MULTILINE)
    return json.loads(cleaned)

def extract_sales_summary(user_id: int):
    range = clean_and_parse_json("{ \"start_date\": \"1990-01-01\", \"end_date\": \"3000-01-01\" }")
    start_date = range.get("start_date", "1990-01-01")
    end_date = range.get("end_date", "3000-01-01")
    if isinstance(start_date, str):
        start_date = datetime.fromisoformat(start_date)
    if isinstance(end_date, str):
        end_date = datetime.fromisoformat(end_date)

    print(f"start_date: {start_date} end_date: {end_date} and id: {user_id}")
    order_repository = OrderRepository()
    sales_summary = order_repository.get_sales_summary(start_date, end_date, user_id)
    print(sales_summary)
    return sales_summary
