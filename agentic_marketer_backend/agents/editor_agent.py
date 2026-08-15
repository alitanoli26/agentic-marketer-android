import openai

class EditorAgent:
    def __init__(self, api_key: str):
        self.client = openai.AsyncOpenAI(api_key=api_key)

    async def humanize(self, content: str) -> str:
        prompt = f"""
        Humanize and improve this content:

        {content}

        Make it sound more natural, conversational, and engaging.
        Add emojis where appropriate. Remove any AI-sounding phrases.
        """
        response = await self.client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.7,
            max_tokens=500
        )
        return response.choices[0].message.content
