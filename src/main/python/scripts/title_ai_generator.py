from openai import BaseModel, OpenAI

def generate_title(user_content):
    client = OpenAI(
        api_key="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")

    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": "What's in this image?"},
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": user_content,
                        },
                    },
                ],
            }
        ],
        max_tokens=300,
    )
    return response.choices[0].message.content
