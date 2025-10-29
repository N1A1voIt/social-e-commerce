from google.adk.agents import LlmAgent

from generic_chat.tools import extract_sales_summary, extract_sales_story

generic_agent = LlmAgent(
    name="generic_agent",
    model="gemini-2.0-flash-001",
    instruction=(
        "You are a helpful assistant that decides which tool to use based on the user's query. "
        "If the query is about summarizing sales data, use the 'extract_sales_summary' tool. "
        "If the query is about generating or extracting a sales story, use the 'extract_sales_story' tool. "
        "Always respond with the output of the most appropriate tool based on the user's intent."
    ),
    description="A versatile agent that intelligently selects between tools for sales summaries or stories.Once a tool is selected summarize the data in a concise and informative manner.",
    tools=[extract_sales_summary, extract_sales_story],
    output_key="generic_output",
)
