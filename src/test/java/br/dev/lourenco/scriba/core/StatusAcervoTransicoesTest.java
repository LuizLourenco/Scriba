package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import org.junit.jupiter.api.Test;

class StatusAcervoTransicoesTest {

    @Test
    void permiteTransicoesValidas() {
        assertThat(StatusAcervo.DISPONIVEL.transicionar(StatusAcervo.EMPRESTADO))
            .isEqualTo(StatusAcervo.EMPRESTADO);
        assertThat(StatusAcervo.EMPRESTADO.transicionar(StatusAcervo.DISPONIVEL))
            .isEqualTo(StatusAcervo.DISPONIVEL);
        assertThat(StatusAcervo.EM_MANUTENCAO.transicionar(StatusAcervo.DESCARTADO))
            .isEqualTo(StatusAcervo.DESCARTADO);
    }

    @Test
    void bloqueiaTransicaoInvalidaDeEstadoTerminal() {
        assertThatThrownBy(() -> StatusAcervo.DESCARTADO.transicionar(StatusAcervo.DISPONIVEL))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("não permitida");
    }
}
