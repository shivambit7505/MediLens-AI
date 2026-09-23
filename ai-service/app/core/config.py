from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Optional


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    ENVIRONMENT: str = "development"
    LOG_LEVEL: str = "INFO"
    AI_SERVICE_PORT: int = 8000
    DATABASE_URL: str = "postgresql://medilens_admin:change_this_secure_password_in_production@postgres:5432/medilens_db"
    REDIS_URL: str = "redis://:change_this_redis_password_in_production@redis:6379/0"
    INTERNAL_API_KEY: str = "internal_pre_shared_key_between_backend_and_ai_service"
    REPORT_STORAGE_DIR: str = "/var/medilens/reports"

    # OCR Settings
    OCR_ENGINE_PRIMARY: str = "paddleocr"
    OCR_ENGINE_FALLBACK: str = "tesseract"
    OCR_CONFIDENCE_THRESHOLD: float = 0.70
    TESSERACT_CMD: str = "/usr/bin/tesseract"

    # RAG Settings
    EMBEDDING_MODEL: str = "text-embedding-3-small"
    EMBEDDING_DIMENSIONS: int = 1536
    LLM_PROVIDER: str = "openai"
    LLM_MODEL: str = "gpt-4o-mini"
    OPENAI_API_KEY: Optional[str] = None
    ANTHROPIC_API_KEY: Optional[str] = None


settings = Settings()
