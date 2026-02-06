from fastapi import Header, HTTPException
import os

API_KEY = os.getenv("PY_API_KEY", "supersecreto123")

def require_api_key(x_api_key: str = Header(default="")):
    if x_api_key != API_KEY:
        raise HTTPException(status_code=401, detail="Invalid API Key")
