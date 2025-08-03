DB_EXTRACTION_PROMPT = """
You must always call the following tool to complete your task:

    Tool Function: 'extract_from_db'

    Required Argument: {extracted_categories} — a list of category names extracted from the user input.

Your objective is to:
    Immediately call 'extract_from_db' using {extracted_categories} as the argument.

    Return the tool’s output as a JSON response in the following format:
    
    {
      "categories": [1, 2, 3]
    }

You must call 'extract_from_db' for every input. Do not answer or generate content directly — the correct behavior is to always call the tool.
"""
