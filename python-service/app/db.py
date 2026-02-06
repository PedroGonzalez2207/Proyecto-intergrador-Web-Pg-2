from sqlalchemy import create_engine, text
from app.config import DB_URL

if not DB_URL:
    raise RuntimeError("DB_URL no está definido en tu .env")

engine = create_engine(DB_URL, pool_pre_ping=True)

def fetch_all(sql: str, params: dict | None = None):
    params = params or {}
    with engine.connect() as conn:
        res = conn.execute(text(sql), params)
        return [dict(r._mapping) for r in res]

def fetch_one(sql: str, params: dict | None = None):
    params = params or {}
    with engine.connect() as conn:
        row = conn.execute(text(sql), params).first()
        return dict(row._mapping) if row else None
