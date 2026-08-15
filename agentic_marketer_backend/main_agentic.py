import os
from typing import Optional
from urllib.parse import quote

import google.generativeai as genai
import requests
import uvicorn
from fastapi import FastAPI, HTTPException

app = FastAPI()

# Configure this only in a private runtime environment.
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)


@app.post("/generate")
async def generate_marketing_content(topic: str, tone: str, content_type: str):
    if not GEMINI_API_KEY:
        raise HTTPException(status_code=503, detail="GEMINI_API_KEY is not configured")
    try:
        model = genai.GenerativeModel("gemini-1.5-flash")
        writer_prompt = f"Write a professional {content_type} about {topic} in a {tone} tone. Keep it engaging and ready for market."
        response = model.generate_content(writer_prompt)
        text_content = response.text
        artist_prompt = f"Based on this marketing text: '{text_content[:200]}', create a high-quality, professional image generation prompt for a marketing visual. Only return the prompt text."
        image_gen_prompt = model.generate_content(artist_prompt).text
        encoded_prompt = quote(image_gen_prompt)
        image_url = f"https://image.pollinations.ai/prompt/{encoded_prompt}?width=1024&height=1024&nologo=true&seed=42"
        return {"text": text_content, "image_url": image_url, "suggested_time": "Tomorrow at 10:00 AM"}
    except Exception as error:
        raise HTTPException(status_code=500, detail=str(error))


@app.get("/health")
async def health():
    return {"status": "up"}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
