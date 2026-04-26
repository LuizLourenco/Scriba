package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.catalogo.domain.Autor;
import br.dev.lourenco.scriba.modules.catalogo.domain.Categoria;
import br.dev.lourenco.scriba.modules.catalogo.domain.Classificacao;
import br.dev.lourenco.scriba.modules.catalogo.domain.Editora;
import br.dev.lourenco.scriba.modules.catalogo.domain.PadraoClassificacao;
import br.dev.lourenco.scriba.modules.catalogo.repository.AutorRepository;
import br.dev.lourenco.scriba.modules.catalogo.repository.CategoriaRepository;
import br.dev.lourenco.scriba.modules.catalogo.repository.ClassificacaoRepository;
import br.dev.lourenco.scriba.modules.catalogo.repository.EditoraRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class CatalogoIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private EditoraRepository editoraRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ClassificacaoRepository classificacaoRepository;

    @Test
    void bibliotecarioCriaAutorNoTenantAtualEBuscaPorNome() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        String nome = "Clarice " + UUID.randomUUID();

        mvc.perform(post("/catalogo/autores")
                .with(csrf())
                .with(user(bibliotecario))
                .param("nome", nome)
                .param("nacionalidade", "Brasileira")
                .param("ativo", "true"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/catalogo/autores"));

        Autor autor = autorRepository.findAllByInstituicaoIdAndNomeContainingIgnoreCaseOrderByNomeAsc(
                bibliotecario.getInstituicaoId(), nome)
            .stream()
            .findFirst()
            .orElseThrow();

        assertThat(autor.getInstituicaoId()).isEqualTo(bibliotecario.getInstituicaoId());

        mvc.perform(get("/catalogo/autores")
                .with(user(bibliotecario))
                .param("busca", nome.substring(0, 7)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(nome)));
    }

    @Test
    void listagemDeAutoresRespeitaTenant() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        Autor autorAtual = criarAutor(bibliotecario.getInstituicaoId(), "Autor Atual " + UUID.randomUUID());
        Autor autorOutro = criarAutor(usuarioOutroTenant.getInstituicaoId(), "Autor Outro " + UUID.randomUUID());

        mvc.perform(get("/catalogo/autores").with(user(bibliotecario)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(autorAtual.getNome())))
            .andExpect(content().string(not(containsString(autorOutro.getNome()))));
    }

    @Test
    void criaEditoraCategoriaHierarquicaEClassificacao() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        String editoraNome = "Editora " + UUID.randomUUID();
        String categoriaPaiNome = "Literatura " + UUID.randomUUID();
        String categoriaFilhaNome = "Poesia " + UUID.randomUUID();
        String codigo = "869." + Math.abs(UUID.randomUUID().hashCode() % 1000);

        mvc.perform(post("/catalogo/editoras")
                .with(csrf())
                .with(user(bibliotecario))
                .param("nome", editoraNome)
                .param("cidade", "Manaus")
                .param("pais", "Brasil")
                .param("ativo", "true"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/catalogo/editoras"));

        mvc.perform(post("/catalogo/categorias")
                .with(csrf())
                .with(user(bibliotecario))
                .param("nome", categoriaPaiNome)
                .param("descricao", "Categoria principal")
                .param("ativo", "true"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/catalogo/categorias"));

        Categoria pai = categoriaRepository.findAllByInstituicaoIdAndNomeContainingIgnoreCaseOrderByNomeAsc(
                bibliotecario.getInstituicaoId(), categoriaPaiNome)
            .stream()
            .findFirst()
            .orElseThrow();

        mvc.perform(post("/catalogo/categorias")
                .with(csrf())
                .with(user(bibliotecario))
                .param("nome", categoriaFilhaNome)
                .param("paiId", pai.getId().toString())
                .param("ativo", "true"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/catalogo/categorias"));

        mvc.perform(post("/catalogo/classificacoes")
                .with(csrf())
                .with(user(bibliotecario))
                .param("padrao", PadraoClassificacao.CDD.name())
                .param("codigo", codigo)
                .param("descricao", "Literatura brasileira")
                .param("ativo", "true"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/catalogo/classificacoes"));

        Editora editora = editoraRepository.findAllByInstituicaoIdAndNomeContainingIgnoreCaseOrderByNomeAsc(
                bibliotecario.getInstituicaoId(), editoraNome)
            .stream()
            .findFirst()
            .orElseThrow();
        Categoria filha = categoriaRepository.findAllByInstituicaoIdAndNomeContainingIgnoreCaseOrderByNomeAsc(
                bibliotecario.getInstituicaoId(), categoriaFilhaNome)
            .stream()
            .findFirst()
            .orElseThrow();
        Classificacao classificacao = classificacaoRepository.findAllByInstituicaoIdOrderByCodigoAsc(
                bibliotecario.getInstituicaoId())
            .stream()
            .filter(item -> codigo.equals(item.getCodigo()))
            .findFirst()
            .orElseThrow();

        assertThat(editora.getInstituicaoId()).isEqualTo(bibliotecario.getInstituicaoId());
        assertThat(filha.getPai().getId()).isEqualTo(pai.getId());
        assertThat(classificacao.getPadrao()).isEqualTo(PadraoClassificacao.CDD);
    }

    private Autor criarAutor(UUID instituicaoId, String nome) {
        Autor autor = new Autor();
        autor.setInstituicaoId(instituicaoId);
        autor.setNome(nome);
        return autorRepository.save(autor);
    }
}
