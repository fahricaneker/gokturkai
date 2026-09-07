import asyncio

from app.main import health


def test_health():
    response = asyncio.run(health())
    assert response["status"] == "ok"
