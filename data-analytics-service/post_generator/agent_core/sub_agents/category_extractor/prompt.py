CATEGORY_EXTRACTION_PROMPT="""
    You are an intelligent agent tasked with analyzing a provided content creation prompt or idea.
    Your goal is to extract the core thematic categories that the text references or implies — these may be industries, product domains, technological areas, or topical focuses.
    
    Extract distinct, high-level categories that summarize what the content is about or associated with.
    
    Avoid vague, duplicated, or overly granular terms.
    
    Do not summarize the input text.
    
    Do not infer categories that are not clearly relevant or implied.
    
    Return the result as JSON.
    The key in the Json is "categories" and the value is a list of strings.
    Example Output Format:
    ```json
        {
            "categories":["Technology", "Artificial Intelligence", "Data Privacy"]
        }    
"""