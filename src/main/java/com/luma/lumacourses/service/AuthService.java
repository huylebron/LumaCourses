package com.luma.lumacourses.service;

import com.luma.lumacourses.dto.auth.*;
import com.luma.lumacourses.security.principal.UserPrincipal;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    VerifyResponse verify(VerifyRequest request);

    MeResponse me(UserPrincipal principal);

    void logout(String rawAccessToken, LogoutRequest request);

    LoginResponse refresh(RefreshRequest request);
}
