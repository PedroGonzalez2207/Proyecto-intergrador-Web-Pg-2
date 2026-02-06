from datetime import date
from fastapi import APIRouter, Depends, HTTPException, Query
from app.security import require_api_key
from app.db import fetch_all, fetch_one

router = APIRouter()

def _parse_date(s: str) -> date:
    try:
        return date.fromisoformat(s)
    except Exception:
        raise HTTPException(status_code=400, detail="Formato de fecha inválido. Usa YYYY-MM-DD")

@router.get("/programador/{programador_id}/resumen")
def resumen(
    programador_id: int,
    from_date: str = Query(...),
    to_date: str = Query(...),
    _=Depends(require_api_key),
):
    fd = _parse_date(from_date)
    td = _parse_date(to_date)

    try:
        row = fetch_one(
            """
            SELECT
              COUNT(*) AS total,
              SUM(CASE WHEN estado = 'Aprobada' THEN 1 ELSE 0 END) AS aprobadas,
              SUM(CASE WHEN estado = 'Rechazada' THEN 1 ELSE 0 END) AS rechazadas,
              SUM(CASE WHEN estado = 'Pendiente' THEN 1 ELSE 0 END) AS pendientes
            FROM tbl_asesoria
            WHERE programador_id = :pid
              AND fecha_inicio::date >= :fd
              AND fecha_inicio::date <= :td
            """,
            {"pid": programador_id, "fd": str(fd), "td": str(td)},
        )

        if not row:
            return {"total": 0, "aprobadas": 0, "rechazadas": 0, "pendientes": 0}

        return {
            "total": int(row.get("total") or 0),
            "aprobadas": int(row.get("aprobadas") or 0),
            "rechazadas": int(row.get("rechazadas") or 0),
            "pendientes": int(row.get("pendientes") or 0),
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Metrics resumen error: {e}")

@router.get("/programador/{programador_id}/serie")
def serie(
    programador_id: int,
    from_date: str = Query(...),
    to_date: str = Query(...),
    _=Depends(require_api_key),
):
    fd = _parse_date(from_date)
    td = _parse_date(to_date)

    try:
        rows = fetch_all(
            """
            SELECT
              to_char(fecha_inicio::date, 'YYYY-MM-DD') AS dia,
              COUNT(*) AS total
            FROM tbl_asesoria
            WHERE programador_id = :pid
              AND fecha_inicio::date >= :fd
              AND fecha_inicio::date <= :td
            GROUP BY fecha_inicio::date
            ORDER BY fecha_inicio::date
            """,
            {"pid": programador_id, "fd": str(fd), "td": str(td)},
        )

        return [{"dia": r["dia"], "total": int(r["total"])} for r in (rows or [])]
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Metrics serie error: {e}")
