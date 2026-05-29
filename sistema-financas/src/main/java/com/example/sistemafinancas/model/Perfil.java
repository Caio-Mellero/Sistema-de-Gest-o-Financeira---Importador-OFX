package com.example.sistemafinancas.model;

/**
 * Perfil de acesso do usuário no sistema.
 * - USER  → acesso às próprias transações
 * - ADMIN → acesso ao painel administrativo (/admin/**)
 */
public enum Perfil {
    USER,
    ADMIN
}
