package br.dev.lourenco.scriba.modules.circulacao.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.modules.acervo.service.AcervoService;
import br.dev.lourenco.scriba.modules.circulacao.dto.EmprestimoForm;
import br.dev.lourenco.scriba.modules.circulacao.dto.ReservaForm;
import br.dev.lourenco.scriba.modules.circulacao.service.CirculacaoService;
import br.dev.lourenco.scriba.modules.pessoas.service.LeitorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/circulacao")
public class CirculacaoController {

    private final CirculacaoService circulacaoService;
    private final AcervoService acervoService;
    private final LeitorService leitorService;

    public CirculacaoController(
        CirculacaoService circulacaoService,
        AcervoService acervoService,
        LeitorService leitorService
    ) {
        this.circulacaoService = circulacaoService;
        this.acervoService = acervoService;
        this.leitorService = leitorService;
    }

    @GetMapping("/emprestimos")
    public String emprestimos(Model model) {
        popularModelo(model);
        return "emprestimos/list";
    }

    @PostMapping("/emprestimos")
    public String emprestar(
        @Valid @ModelAttribute("emprestimoForm") EmprestimoForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            popularModelo(model);
            return "emprestimos/list";
        }

        circulacaoService.realizarEmprestimo(form.getAcervoItemId(), form.getLeitorId());
        redirectAttributes.addFlashAttribute("sucesso", "Empréstimo registrado.");
        return "redirect:/circulacao/emprestimos";
    }

    @PostMapping("/reservas")
    public String reservar(
        @Valid @ModelAttribute("reservaForm") ReservaForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            popularModelo(model);
            return "emprestimos/list";
        }

        circulacaoService.reservar(form.getAcervoItemId(), form.getLeitorId());
        redirectAttributes.addFlashAttribute("sucesso", "Reserva registrada.");
        return "redirect:/circulacao/emprestimos";
    }

    @PostMapping("/emprestimos/{id}/devolver")
    public String devolver(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        circulacaoService.devolver(id);
        redirectAttributes.addFlashAttribute("sucesso", "Devolução registrada.");
        return "redirect:/circulacao/emprestimos";
    }

    @PostMapping("/emprestimos/{id}/renovar")
    public String renovar(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        circulacaoService.renovar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Empréstimo renovado.");
        return "redirect:/circulacao/emprestimos";
    }

    private void popularModelo(Model model) {
        if (!model.containsAttribute("emprestimoForm")) {
            model.addAttribute("emprestimoForm", new EmprestimoForm());
        }
        if (!model.containsAttribute("reservaForm")) {
            model.addAttribute("reservaForm", new ReservaForm());
        }
        model.addAttribute("emprestimos", circulacaoService.listarEmprestimosDaInstituicaoAtual());
        model.addAttribute("reservas", circulacaoService.listarReservasDaInstituicaoAtual());
        model.addAttribute("multas", circulacaoService.listarMultasDaInstituicaoAtual());
        model.addAttribute("itens", acervoService.listarDaInstituicaoAtual(null));
        model.addAttribute("leitores", leitorService.listarDaInstituicaoAtual());
    }
}
