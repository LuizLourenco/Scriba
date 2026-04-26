package br.dev.lourenco.scriba.modules.curadoria.controller;

import br.dev.lourenco.scriba.modules.acervo.service.AcervoService;
import br.dev.lourenco.scriba.modules.administracao.service.BibliotecaService;
import br.dev.lourenco.scriba.modules.curadoria.dto.DesbastamentoForm;
import br.dev.lourenco.scriba.modules.curadoria.service.CuradoriaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/curadoria/desbastamentos")
public class CuradoriaController {

    private final CuradoriaService curadoriaService;
    private final AcervoService acervoService;
    private final BibliotecaService bibliotecaService;

    public CuradoriaController(
        CuradoriaService curadoriaService,
        AcervoService acervoService,
        BibliotecaService bibliotecaService
    ) {
        this.curadoriaService = curadoriaService;
        this.acervoService = acervoService;
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping
    public String listar(Model model) {
        popularModelo(model);
        return "desbastamentos/list";
    }

    @PostMapping
    public String registrar(
        @Valid @ModelAttribute("desbastamentoForm") DesbastamentoForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            popularModelo(model);
            return "desbastamentos/list";
        }

        curadoriaService.registrar(form);
        redirectAttributes.addFlashAttribute("sucesso", "Desbastamento registrado.");
        return "redirect:/curadoria/desbastamentos";
    }

    private void popularModelo(Model model) {
        if (!model.containsAttribute("desbastamentoForm")) {
            model.addAttribute("desbastamentoForm", new DesbastamentoForm());
        }
        model.addAttribute("desbastamentos", curadoriaService.listarDaInstituicaoAtual());
        model.addAttribute("itens", acervoService.listarDaInstituicaoAtual(null));
        model.addAttribute("bibliotecas", bibliotecaService.listarDaInstituicaoAtual());
    }
}
