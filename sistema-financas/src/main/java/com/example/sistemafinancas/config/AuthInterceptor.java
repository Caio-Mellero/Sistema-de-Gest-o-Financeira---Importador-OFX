package com.example.sistemafinancas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que protege todas as rotas privadas.
 * Se não houver um usuário na sessão HTTP, redireciona para /login.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        boolean estaLogado = session != null && session.getAttribute("usuarioLogado") != null;

        if (!estaLogado) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false; // Para a execução da rota protegida
        }
        return true; // Permite continuar normalmente
    }
}
