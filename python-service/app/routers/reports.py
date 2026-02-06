from fastapi import APIRouter, Depends, Query
from fastapi.responses import StreamingResponse
from io import BytesIO
from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas

from app.security import require_api_key
from app.db import fetch_all

router = APIRouter()

@router.get("/asesorias.pdf")
def reporte_asesorias_pdf(
    from_date: str = Query(...),
    to_date: str = Query(...),
    _=Depends(require_api_key)
):
    data = fetch_all("""
        SELECT
          a.id,
          a.estado,
          a.modalidad,
          a.fecha_inicio,
          a.fecha_fin,
          u.email AS cliente_email,
          pu.email AS programador_email
        FROM tbl_asesoria a
        JOIN tbl_usuario u ON u.id = a.cliente_id
        JOIN tbl_programador p ON p.id = a.programador_id
        JOIN tbl_usuario pu ON pu.id = p.usuario_id
        WHERE a.fecha_inicio::date BETWEEN :f AND :t
        ORDER BY a.fecha_inicio ASC
    """, {"f": from_date, "t": to_date})

    buffer = BytesIO()
    c = canvas.Canvas(buffer, pagesize=letter)
    width, height = letter

    y = height - 50
    c.setFont("Helvetica-Bold", 14)
    c.drawString(50, y, f"Reporte de Asesorías ({from_date} a {to_date})")
    y -= 30

    c.setFont("Helvetica", 9)
    for row in data:
        line = (
            f"#{row['id']} | {row['estado']} | {row['modalidad']} | "
            f"{row['fecha_inicio']} - {row['fecha_fin']} | "
            f"Cliente: {row['cliente_email']} | Prog: {row['programador_email']}"
        )
        c.drawString(50, y, line[:120])
        y -= 12
        if y < 60:
            c.showPage()
            y = height - 50
            c.setFont("Helvetica", 9)

    c.save()
    buffer.seek(0)

    return StreamingResponse(
        buffer,
        media_type="application/pdf",
        headers={"Content-Disposition": "attachment; filename=reporte_asesorias.pdf"}
    )
