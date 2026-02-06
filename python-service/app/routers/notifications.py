from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, EmailStr
import smtplib, ssl
from email.message import EmailMessage
import httpx

from app.security import require_api_key
from app.config import SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS, SMTP_FROM, TELEGRAM_BOT_TOKEN

router = APIRouter()

class EmailReq(BaseModel):
    to: EmailStr
    subject: str
    text: str
    reply_to: EmailStr | None = None  # ✅ NUEVO

@router.post("/email")
def send_email(req: EmailReq, _=Depends(require_api_key)):
    if not SMTP_USER or not SMTP_PASS:
        raise HTTPException(status_code=500, detail="SMTP_USER/SMTP_PASS no configurados en .env")

    msg = EmailMessage()
    msg["From"] = SMTP_FROM or SMTP_USER
    msg["To"] = req.to
    msg["Subject"] = req.subject

    # ✅ NUEVO: Reply-To para simular "enviado por usuario/programador"
    if req.reply_to:
        msg["Reply-To"] = req.reply_to

    msg.set_content(req.text)

    ctx = ssl.create_default_context()
    try:
        with smtplib.SMTP(SMTP_HOST, SMTP_PORT) as s:
            s.starttls(context=ctx)
            s.login(SMTP_USER, SMTP_PASS)
            s.send_message(msg)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"SMTP error: {str(e)}")

    return {"ok": True}

class TelegramReq(BaseModel):
    chat_id: str
    text: str

@router.post("/telegram")
async def send_telegram(req: TelegramReq, _=Depends(require_api_key)):
    if not TELEGRAM_BOT_TOKEN:
        raise HTTPException(status_code=500, detail="TG_BOT_TOKEN no configurado en .env")

    url = f"https://api.telegram.org/bot{TELEGRAM_BOT_TOKEN}/sendMessage"
    payload = {"chat_id": req.chat_id, "text": req.text}

    async with httpx.AsyncClient(timeout=15) as client:
        r = await client.post(url, json=payload)

    if r.status_code >= 400:
        raise HTTPException(status_code=500, detail=f"Telegram error HTTP {r.status_code}: {r.text}")

    data = r.json()
    if not data.get("ok"):
        raise HTTPException(status_code=500, detail=f"Telegram error: {data}")

    return {"ok": True}
