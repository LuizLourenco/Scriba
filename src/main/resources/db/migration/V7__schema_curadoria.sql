CREATE TABLE desbastamento (
                              id BINARY(16) NOT NULL,
                              instituicao_id BINARY(16) NOT NULL,
                              acervo_item_id BINARY(16) NOT NULL,
                              usuario_id BINARY(16) NOT NULL,
                              tipo VARCHAR(30) NOT NULL,
                              justificativa VARCHAR(1000) NOT NULL,
                              destino_biblioteca_id BINARY(16) NULL,
                              destino_instituicao_id BINARY(16) NULL,
                              criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (id),
                              KEY idx_desbastamento_inst_tipo (instituicao_id, tipo),
                              KEY idx_desbastamento_item (acervo_item_id),
                              KEY idx_desbastamento_usuario (usuario_id),
                              CONSTRAINT fk_desbastamento_instituicao
                                  FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
                              CONSTRAINT fk_desbastamento_acervo_item
                                  FOREIGN KEY (acervo_item_id) REFERENCES acervo_item (id),
                              CONSTRAINT fk_desbastamento_usuario
                                  FOREIGN KEY (usuario_id) REFERENCES usuario (id),
                              CONSTRAINT fk_desbastamento_destino_biblioteca
                                  FOREIGN KEY (destino_biblioteca_id) REFERENCES biblioteca (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
