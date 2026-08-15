import openai
from typing import List

class WriterAgent:
    def __init__(self, api_key: str):
        self.client = openai.AsyncOpenAI(api_key=api_key)

    async def generate(self, topic: str, content_type: str, tone: str, platforms: List[str]) -> str:
        prompt = f"""
        You are a professional social media marketer.

        Topic: {topic}
        Content Type: {content_type}
        Tone: {tone}
        Platforms: {', '.join(platforms)}

        Generate engaging content that works across these platforms.
        Keep it concise, impactful, and platform-appropriate.
        """

        response = await self.client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.7,
            max_tokens=500
        )

        return response.choices[0].message.content
