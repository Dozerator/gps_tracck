import asyncio
import json

from fastapi import WebSocket


class OperatorConnectionManager:
    def __init__(self) -> None:
        self.active_connections: list[WebSocket] = []

    async def connect(self, websocket: WebSocket) -> None:
        await websocket.accept()
        self.active_connections.append(websocket)

    def disconnect(self, websocket: WebSocket) -> None:
        if websocket in self.active_connections:
            self.active_connections.remove(websocket)

    async def broadcast(self, message: dict) -> None:
        """Стандартная рассылка: последовательно, по одному соединению за раз."""
        payload = self._serialize(message)
        stale_connections = []
        for connection in self.active_connections:
            try:
                await connection.send_text(payload)
            except Exception:
                stale_connections.append(connection)
        self._drop(stale_connections)

    async def broadcast_priority(self, message: dict) -> None:
        """
        Приоритетная рассылка для THREAT: все соединения оповещаются параллельно
        (asyncio.gather), чтобы задержка на одном медленном клиенте не откладывала
        доставку остальным — минимизирует худшее время до самого дальнего оператора.
        """
        payload = self._serialize(message)
        results = await asyncio.gather(
            *(connection.send_text(payload) for connection in self.active_connections),
            return_exceptions=True,
        )
        stale_connections = [
            connection
            for connection, result in zip(self.active_connections, results)
            if isinstance(result, Exception)
        ]
        self._drop(stale_connections)

    def _drop(self, connections: list[WebSocket]) -> None:
        for connection in connections:
            self.disconnect(connection)

    @staticmethod
    def _serialize(message: dict) -> str:
        return json.dumps(message, default=str, ensure_ascii=False)


manager = OperatorConnectionManager()
