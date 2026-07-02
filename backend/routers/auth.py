from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from auth import authenticate_user, create_access_token
from database import get_db
from schemas import LoginRequest, TokenResponse

router = APIRouter(prefix="/api/auth", tags=["auth"])


@router.post("/login", response_model=TokenResponse)
def login(payload: LoginRequest, db: Session = Depends(get_db)):
    user = authenticate_user(db, payload.login, payload.password)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Неверный логин или пароль",
        )
    token, expires_in = create_access_token(subject=user.login)
    return TokenResponse(access_token=token, expires_in=expires_in)
