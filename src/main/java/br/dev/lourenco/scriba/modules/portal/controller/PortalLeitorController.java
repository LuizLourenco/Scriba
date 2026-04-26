package br.dev.lourenco.scriba.modules.portal.controller;

import br.dev.lourenco.scriba.modules.portal.dto.PortalReservaForm;
import br.dev.lourenco.scriba.modules.portal.service.PortalLeitorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/portal")
public class PortalLeitorController {

    private final PortalLeitorService portalLeitorService;

    public PortalLeitorController(PortalLeitorService portalLeitorService) {
        this.portalLeitorService = portalLeitorService;
    }

    @GetMapping
    public String inicio(Model model) {
        popularAreaLeitor(model);
        return "portal/inicio";
    }

    @GetMapping("/acervo")
    public String acervo(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("busca", busca);
        model.addAttribute("itens", portalLeitorService.consultarAcervo(busca));
        model.addAttribute("reservaForm", new PortalReservaForm());
        return "portal/acervo";
    }

    @PostMapping("/reservas")
    public String reservar(
        @Valid @ModelAttribute("reservaForm") PortalReservaForm form,
        BindingResult binding,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            redirectAttributes.addFlashAttribute("erro", "Item de acervo obrigatorio.");
            return "redirect:/portal/acervo";
        }

        portalLeitorService.reservar(form.getAcervoItemId());
        redirectAttributes.addFlashAttribute("sucesso", "Reserva solicitada.");
        return "redirect:/portal";
    }

    private void popularAreaLeitor(Model model) {
        model.addAttribute("leitor", portalLeitorService.leitorAtual());
        model.addAttribute("emprestimos", portalLeitorService.emprestimosDoLeitorAtual());
        model.addAttribute("reservas", portalLeitorService.reservasDoLeitorAtual());
        model.addAttribute("multas", portalLeitorService.multasDoLeitorAtual());
    }
}
