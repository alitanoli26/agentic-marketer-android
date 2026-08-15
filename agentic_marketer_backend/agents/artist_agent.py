import replicate

class ArtistAgent:
    def __init__(self, api_token: str):
        self.client = replicate.Client(api_token=api_token)

    async def generate_image(self, prompt: str) -> str:
        output = self.client.run(
            "stability-ai/stable-diffusion:db21e45d3f7023abc2a46ee38a23973f6dce16bb082a930b0c49861f96d1e5bf",
            input={
                "prompt": f"Social media marketing image for: {prompt}",
                "negative_prompt": "text, watermark, logo",
                "width": 1024,
                "height": 1024
            }
        )
        return output[0]  # Returns image URL
