package br.dev.lourenco.scriba.modules.relatorio.controller;

import java.time.LocalDate;

import br.dev.lourenco.scriba.modules.relatorio.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/relatorios")
public class AdminRelatorioController {

    private final RelatorioService relatorioService;

    public AdminRelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/emprestimos-em-aberto")
    public String emprestimosEmAberto(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
        Model model
    ) {
        model.addAttribute("relatorio", relatorioService.emprestimosEmAberto(de, ate));
        return "admin/relatorios/emprestimos-em-aberto";
    }
}
