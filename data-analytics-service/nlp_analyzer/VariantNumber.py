from typing import List

from pydantic import BaseModel, Field


class VariantNumber(BaseModel):
    """Represents a single product SKU and its quantity."""
    sku: str = Field(description="The product SKU, typically an uppercase word or code.")
    qty: int = Field(description="The integer quantity for the corresponding SKU.")

class MessageNLPOutput(BaseModel):
    """A container for the list of extracted variants."""
    variants: List[VariantNumber] = Field(description="A list of all extracted SKU and quantity pairs.")