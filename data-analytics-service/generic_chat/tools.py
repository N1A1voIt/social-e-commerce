from datetime import datetime

from nlp_analyzer.db.order_repository import OrderRepository


def extract_sales_story(start_date: datetime, end_date: datetime, user_id: int) :
    order_repository = OrderRepository()
    sales_statistics = order_repository.get_sales_evolution(start_date, end_date, user_id)
    return sales_statistics

def extract_sales_summary(start_date: datetime, end_date: datetime, user_id: int) :
    order_repository = OrderRepository()
    sales_summary = order_repository.get_sales_summary(start_date, end_date, user_id)
    return sales_summary
