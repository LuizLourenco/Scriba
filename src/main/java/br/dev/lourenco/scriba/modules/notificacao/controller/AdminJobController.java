package br.dev.lourenco.scriba.modules.notificacao.controller;

import br.dev.lourenco.scriba.modules.notificacao.service.NotificacaoAtrasoJob;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/jobs")
public class AdminJobController {

    private final NotificacaoAtrasoJob notificacaoAtrasoJob;

    public AdminJobController(NotificacaoAtrasoJob notificacaoAtrasoJob) {
        this.notificacaoAtrasoJob = notificacaoAtrasoJob;
    }

    @PostMapping("/notificar-atrasos")
    public String notificarAtrasos(RedirectAttributes redirectAttributes) {
        int enviados = notificacaoAtrasoJob.executarParaInstituicaoAtual();
        redirectAttributes.addFlashAttribute("sucesso", enviados + " notificacoes de atraso enviadas.");
        return "redirect:/admin/relatorios/emprestimos-em-aberto";
    }
}
