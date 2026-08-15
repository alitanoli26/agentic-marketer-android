import os
import uuid
from typing import List
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from dotenv import load_dotenv

from agents.writer_agent import WriterAgent
from agents.editor_agent import EditorAgent
from agents.artist_agent import ArtistAgent
from agents.scheduler_agent import SchedulerAgent

load_dotenv()

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

class GenerateRequest(BaseModel):
    topic: str
    content_type: str
    tone: str
    platforms: List[str]
    instructions: str = ""

@app.post("/api/generate")
async def generate_content(request: GenerateRequest):
    try:
        openai_key = os.getenv("OPENAI_API_KEY")
        replicate_token = os.getenv("REPLICATE_API_TOKEN")

        # Step 1: Write content
        writer = WriterAgent(openai_key)
        content = await writer.generate(
            request.topic,
            request.content_type,
            request.tone,
            request.platforms
        )

        # Step 2: Humanize
        editor = EditorAgent(openai_key)
        humanized = await editor.humanize(content)

        # Step 3: Generate image
        artist = ArtistAgent(replicate_token)
        image_url = await artist.generate_image(request.topic)

        # Step 4: Get schedule
        scheduler = SchedulerAgent()
        suggested_time = scheduler.get_best_time(request.platforms[0])

        return {
            "content": content,
            "humanized_content": humanized,
            "image_url": image_url,
            "suggested_time": suggested_time,
            "campaign_id": str(uuid.uuid4())
        }

    except Exception as e:
        print(f"Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
