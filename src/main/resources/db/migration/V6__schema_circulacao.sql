ALTER TABLE instituicao
    ADD COLUMN limite_reservas INT NOT NULL DEFAULT 3,
    ADD COLUMN dias_expiracao_reserva INT NOT NULL DEFAULT 7,
    ADD COLUMN maximo_renovacoes INT NOT NULL DEFAULT 3,
    ADD COLUMN bloqueio_com_multa BIT NOT NULL DEFAULT b'1',
    ADD COLUMN tipo_multa VARCHAR(30) NOT NULL DEFAULT 'FIXO_DIARIO',
    ADD COLUMN teto_maximo_multa DECIMAL(10, 2) NULL;

CREATE TABLE emprestimo (
                            id BINARY(16) NOT NULL,
                            instituicao_id BINARY(16) NOT NULL,
                            acervo_item_id BINARY(16) NOT NULL,
                            leitor_id BINARY(16) NOT NULL,
                            data_emprestimo DATE NOT NULL,
                            data_prevista_devolucao DATE NOT NULL,
                            data_efetiva_devolucao DATE NULL,
                            status VARCHAR(30) NOT NULL,
                            renovacoes INT NOT NULL DEFAULT 0,
                            acervo_item_id_ativo BINARY(16)
                                GENERATED ALWAYS AS (CASE WHEN status = 'ATIVO' THEN acervo_item_id ELSE NULL END) VIRTUAL,
                            criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (id),
                            UNIQUE KEY uq_emprestimo_item_ativo (acervo_item_id_ativo),
                            KEY idx_emprestimo_inst_status (instituicao_id, status),
                            KEY idx_emprestimo_leitor_status (leitor_id, status),
                            CONSTRAINT fk_emprestimo_instituicao
                                FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
                            CONSTRAINT fk_emprestimo_acervo_item
                                FOREIGN KEY (acervo_item_id) REFERENCES acervo_item (id),
                            CONSTRAINT fk_emprestimo_leitor
                                FOREIGN KEY (leitor_id) REFERENCES leitor (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reserva (
                         id BINARY(16) NOT NULL,
                         instituicao_id BINARY(16) NOT NULL,
                         acervo_item_id BINARY(16) NOT NULL,
                         leitor_id BINARY(16) NOT NULL,
                         status VARCHAR(30) NOT NULL,
                         data_reserva DATE NOT NULL,
                         data_expiracao DATE NOT NULL,
                         posicao_fila INT NOT NULL,
                         criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         KEY idx_reserva_inst_status (instituicao_id, status),
                         KEY idx_reserva_item_status_fila (acervo_item_id, status, posicao_fila),
                         CONSTRAINT fk_reserva_instituicao
                             FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
                         CONSTRAINT fk_reserva_acervo_item
                             FOREIGN KEY (acervo_item_id) REFERENCES acervo_item (id),
                         CONSTRAINT fk_reserva_leitor
                             FOREIGN KEY (leitor_id) REFERENCES leitor (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE multa (
                       id BINARY(16) NOT NULL,
                       instituicao_id BINARY(16) NOT NULL,
                       emprestimo_id BINARY(16) NOT NULL,
                       leitor_id BINARY(16) NOT NULL,
                       valor DECIMAL(10, 2) NOT NULL,
                       dias_atraso INT NOT NULL,
                       status VARCHAR(30) NOT NULL,
                       data_geracao DATE NOT NULL,
                       criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       KEY idx_multa_inst_status (instituicao_id, status),
                       KEY idx_multa_leitor_status (leitor_id, status),
                       CONSTRAINT fk_multa_instituicao
                           FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
                       CONSTRAINT fk_multa_emprestimo
                           FOREIGN KEY (emprestimo_id) REFERENCES emprestimo (id),
                       CONSTRAINT fk_multa_leitor
                           FOREIGN KEY (leitor_id) REFERENCES leitor (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
