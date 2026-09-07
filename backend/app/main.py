from typing import Literal

import httpx
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")
    ai_base_url: str = "https://api.openai.com/v1"
    ai_api_key: str = ""
    ai_chat_model: str = "gpt-4o-mini"
    ai_image_model: str = "gpt-image-1"
    allowed_origins: str = "*"


settings = Settings()
app = FastAPI(title="Göktürk API", version="0.1.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=[x.strip() for x in settings.allowed_origins.split(",")],
    allow_credentials=False,
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)


class Message(BaseModel):
    role: Literal["user", "assistant", "system"]
    content: str = Field(min_length=1, max_length=20_000)


class ChatRequest(BaseModel):
    messages: list[Message] = Field(min_length=1, max_length=30)


class ImageRequest(BaseModel):
    prompt: str = Field(min_length=3, max_length=2_000)


def headers() -> dict[str, str]:
    if not settings.ai_api_key:
        raise HTTPException(503, "AI_API_KEY ayarlanmamış")
    return {"Authorization": f"Bearer {settings.ai_api_key}", "Content-Type": "application/json"}


async def provider_post(path: str, payload: dict) -> dict:
    try:
        async with httpx.AsyncClient(timeout=120) as client:
            response = await client.post(
                f"{settings.ai_base_url.rstrip('/')}/{path.lstrip('/')}",
                headers=headers(),
                json=payload,
            )
            response.raise_for_status()
            return response.json()
    except httpx.HTTPStatusError as exc:
        detail = exc.response.text[:500]
        raise HTTPException(502, f"Yapay zekâ sağlayıcısı hata verdi: {detail}") from exc
    except httpx.HTTPError as exc:
        raise HTTPException(502, "Yapay zekâ sağlayıcısına bağlanılamadı") from exc


@app.get("/health")
async def health() -> dict:
    return {"status": "ok", "service": "gokturk-api"}


@app.post("/v1/chat")
async def chat(request: ChatRequest) -> dict:
    system = {
        "role": "system",
        "content": (
            "Sen Göktürk adında güvenilir, samimi ve güçlü bir Türkçe dijital asistansın. "
            "Kullanıcı başka dil istemedikçe yalnızca doğal Türkçe cevap ver. Bilmediğin şeyi uydurma."
        ),
    }
    result = await provider_post(
        "chat/completions",
        {"model": settings.ai_chat_model, "messages": [system, *[m.model_dump() for m in request.messages]], "temperature": 0.7},
    )
    try:
        return {"reply": result["choices"][0]["message"]["content"]}
    except (KeyError, IndexError, TypeError) as exc:
        raise HTTPException(502, "Sağlayıcıdan geçersiz sohbet yanıtı geldi") from exc


@app.post("/v1/images")
async def images(request: ImageRequest) -> dict:
    enriched = f"{request.prompt}. Zarif, yüksek kaliteli kompozisyon; istenirse Türk ve Selçuklu estetiği."
    result = await provider_post(
        "images/generations",
        {"model": settings.ai_image_model, "prompt": enriched, "size": "1024x1024", "n": 1},
    )
    try:
        item = result["data"][0]
        if item.get("url"):
            return {"url": item["url"]}
        if item.get("b64_json"):
            return {"url": f"data:image/png;base64,{item['b64_json']}"}
        raise HTTPException(502, "Sağlayıcı görsel verisi döndürmedi")
    except (KeyError, IndexError, TypeError) as exc:
        raise HTTPException(502, "Sağlayıcıdan geçersiz görsel yanıtı geldi") from exc
