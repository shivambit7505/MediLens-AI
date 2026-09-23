from fastapi import Header, HTTPException, status
from app.core.config import settings


def verify_internal_api_key(x_internal_api_key: str = Header(None)) -> str:
    """
    Enforces internal network service-to-service authentication.
    Requests between Spring Boot backend and FastAPI AI service must pass this check.
    """
    if not x_internal_api_key or x_internal_api_key != settings.INTERNAL_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing X-Internal-API-Key header",
        )
    return x_internal_api_key
