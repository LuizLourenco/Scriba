package br.dev.lourenco.scriba.modules.relatorio.dto;

import java.time.LocalDate;
import java.util.List;

import br.dev.lourenco.scriba.modules.circulacao.domain.Emprestimo;

public record EmprestimosEmAbertoRelatorio(
    LocalDate de,
    LocalDate ate,
    long total,
    List<Emprestimo> emprestimos
) {
}
