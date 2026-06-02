package com.example.finsight.controller;

import com.example.finsight.model.Perfil;
import com.example.finsight.model.Transacao;
import com.example.finsight.model.TipoTransacao;
import com.example.finsight.model.Usuario;
import com.example.finsight.repository.TransacaoRepository;
import com.example.finsight.service.TransacaoService;
import com.example.finsight.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransacaoRepository repository;

    @MockitoBean
    private TransacaoService transacaoService;

    // Necessário mockar para o contexto de segurança inicializar corretamente
    @MockitoBean
    private UsuarioService usuarioService;

    private Usuario usuarioLogado;

    @BeforeEach
    void setup() {
        usuarioLogado = new Usuario();
        usuarioLogado.setId(1L);
        usuarioLogado.setNome("Caio MVC");
        usuarioLogado.setEmail("caio@mvc.com");
        usuarioLogado.setSenha("senha123");
        usuarioLogado.setPerfil(Perfil.USER);
        usuarioLogado.setAtivo(true);
    }

    @Test
    @DisplayName("Cenário 1: Ao acessar a rota GET '/app', deve retornar status 200 (OK) e injetar transações no Model")
    void deveRetornarStatus200EListarTransacoes() throws Exception {
        // Arrange
        Transacao transacao = new Transacao();
        transacao.setDescricao("Salário");
        transacao.setTipo(TipoTransacao.ENTRADA);
        transacao.setValor(new BigDecimal("5000.00"));
        transacao.setData(LocalDate.now());

        when(repository.findByUsuario(eq(usuarioLogado), any(Sort.class)))
                .thenReturn(List.of(transacao));

        // Act & Assert (com usuário autenticado injetado pelo Spring Security)
        mockMvc.perform(get("/app").with(user(usuarioLogado)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("transacoes"))
                .andExpect(model().attributeExists("nomeUsuario"))
                .andExpect(model().attribute("nomeUsuario", "Caio MVC"));

        verify(repository, times(1)).findByUsuario(eq(usuarioLogado), any(Sort.class));
    }

    @Test
    @DisplayName("Cenário 2: Ao efetuar um POST para '/apagar/{id}', deve deletar a transação e redirecionar para '/app'")
    void deveDeletarTransacaoERedirecionar() throws Exception {
        // Arrange
        Long idTransacao = 10L;
        Transacao transacao = new Transacao();
        transacao.setId(idTransacao);
        transacao.setUsuario(usuarioLogado); // Usuário dono da transação (mesmo do contexto de segurança)

        when(repository.findById(idTransacao)).thenReturn(Optional.of(transacao));

        // Act & Assert (POST precisa de autenticação e token CSRF válido)
        mockMvc.perform(post("/apagar/{id}", idTransacao)
                .with(user(usuarioLogado))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app"));

        verify(repository, times(1)).findById(idTransacao);
        verify(repository, times(1)).deleteById(idTransacao);
    }
}
