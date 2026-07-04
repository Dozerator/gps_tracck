from pathlib import Path

from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

from config import settings
from database import Base, engine
from routers import auth as auth_router
from routers import export as export_router
from routers import location as location_router
from routers import tracks as tracks_router
from websocket_manager import manager

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Operator Tracking API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth_router.router)
app.include_router(location_router.router)
app.include_router(tracks_router.router)
app.include_router(export_router.router)

STATIC_DIR = Path(__file__).parent / "static"
app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")


@app.get("/admin")
def operator_panel():
    return FileResponse(STATIC_DIR / "index.html")


@app.websocket("/ws/operator")
async def operator_websocket(websocket: WebSocket):
    await manager.connect(websocket)
    try:
        while True:
            # Панель оператора не обязана ничего отправлять, но принимаем
            # входящие сообщения (например ping), чтобы соединение не рвалось.
            await websocket.receive_text()
    except WebSocketDisconnect:
        manager.disconnect(websocket)


@app.get("/health")
def health_check():
    return {"status": "ok"}


if __name__ == "__main__":
    import uvicorn

    # Продакшн-запуск: TLS терминируется на Nginx (см. nginx/operator.conf),
    # поэтому FastAPI слушает только localhost и не доступен снаружи напрямую.
    # Для локальной разработки без Nginx используйте:
    #   uvicorn main:app --host 0.0.0.0 --port 8000 --reload
    uvicorn.run(
        "main:app",
        host="127.0.0.1",
        port=8000,
        reload=False,
    )
