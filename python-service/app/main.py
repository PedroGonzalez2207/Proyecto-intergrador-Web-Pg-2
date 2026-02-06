from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers.notifications import router as notifications_router
from app.routers.metrics import router as metrics_router
from app.routers.reports import router as reports_router
from app.routers.telegram import router as telegram_router

app = FastAPI(title="Python Service - Notifs/Metrics/Reports", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:4200", "http://127.0.0.1:4200"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=["Content-Disposition"],
)

app.include_router(notifications_router, prefix="/notifications", tags=["notifications"])
app.include_router(telegram_router, prefix="/telegram", tags=["telegram"])
app.include_router(metrics_router, prefix="/metrics", tags=["metrics"])
app.include_router(reports_router, prefix="/reports", tags=["reports"])

@app.get("/health")
def health():
    return {"status": "ok"}
