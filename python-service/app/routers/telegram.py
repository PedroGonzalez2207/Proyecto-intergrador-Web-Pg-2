from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
import requests

from app.security import require_api_key
from app.config import TELEGRAM_BOT_TOKEN

router = APIRouter()

class TelegramReq(BaseModel):
    chat_id: str
    text: str

@router.post("/telegram")
def send_telegram(req: TelegramReq, _=Depends(require_api_key)):
    if not TELEGRAM_BOT_TOKEN:
        raise HTTPException(status_code=500, detail="TELEGRAM_BOT_TOKEN no configurado en .env")

    url = f"https://api.telegram.org/bot{TELEGRAM_BOT_TOKEN}/sendMessage"
    resp = requests.post(url, json={
        "chat_id": req.chat_id,
        "text": req.text
    }, timeout=15)

    if resp.status_code >= 400:
        raise HTTPException(status_code=500, detail=f"Telegram error: {resp.text}")

    return {"ok": True}
