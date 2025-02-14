import mysql.connector
from mysql.connector import Error


from pydantic import BaseModel
from openai import OpenAI
import json


def generate_title(product_data):
    client = OpenAI(
        api_key="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")

    class NewTitle(BaseModel):
        title: str

    completion = client.beta.chat.completions.parse(
        model="gpt-4o-2024-08-06",
        messages=[
            {"role": "system",
             "content": "generate new title from original title with image example: IR3-07-TP IRVING Round Dining Table with Pedestal, Rustic Rubberwood Table in Distressed Jacobean Finish, 48 Inch"},
             {"role": "user",
              "content": f"New SKU: {product_data['localSku']}\nImage URL: {product_data['img']}"}
         ],
        response_format=NewTitle,
    )

    research_paper = completion.choices[0].message.content
    data_dict = json.loads(research_paper)
    return data_dict['title']

def main():
    return "HELLo"