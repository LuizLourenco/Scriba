CREATE TABLE acervo_item (
                             id BINARY(16) NOT NULL,
                             instituicao_id BINARY(16) NOT NULL,
                             biblioteca_id BINARY(16) NULL,
                             tipo_item VARCHAR(30) NOT NULL,
                             titulo VARCHAR(200) NOT NULL,
                             status VARCHAR(30) NOT NULL,
                             codigo_barras VARCHAR(80) NULL,
                             tombo VARCHAR(80) NOT NULL,
                             localizacao VARCHAR(120) NULL,
                             data_aquisicao DATE NULL,
                             deleted_at TIMESTAMP NULL,
                             versao BIGINT NOT NULL DEFAULT 0,
                             criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             UNIQUE KEY uq_acervo_tombo_inst (instituicao_id, tombo),
                             KEY idx_acervo_inst_status_tipo (instituicao_id, status, tipo_item),
                             CONSTRAINT fk_acervo_item_instituicao
                                 FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
                             CONSTRAINT fk_acervo_item_biblioteca
                                 FOREIGN KEY (biblioteca_id) REFERENCES biblioteca (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE acervo_livro (
                              id BINARY(16) NOT NULL,
                              isbn VARCHAR(20) NULL,
                              numero_paginas INT NULL,
                              edicao VARCHAR(50) NULL,
                              volume VARCHAR(50) NULL,
                              PRIMARY KEY (id),
                              CONSTRAINT fk_acervo_livro_item
                                  FOREIGN KEY (id) REFERENCES acervo_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE acervo_periodico (
                                  id BINARY(16) NOT NULL,
                                  tipo_periodico VARCHAR(20) NULL,
                                  issn VARCHAR(20) NULL,
                                  volume VARCHAR(50) NULL,
                                  numero VARCHAR(50) NULL,
                                  PRIMARY KEY (id),
                                  CONSTRAINT fk_acervo_periodico_item
                                      FOREIGN KEY (id) REFERENCES acervo_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE acervo_carta (
                              id BINARY(16) NOT NULL,
                              remetente VARCHAR(150) NULL,
                              destinatario VARCHAR(150) NULL,
                              data_envio DATE NULL,
                              PRIMARY KEY (id),
                              CONSTRAINT fk_acervo_carta_item
                                  FOREIGN KEY (id) REFERENCES acervo_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE acervo_foto (
                             id BINARY(16) NOT NULL,
                             assunto VARCHAR(150) NULL,
                             fotografo VARCHAR(150) NULL,
                             formato VARCHAR(80) NULL,
                             resolucao VARCHAR(80) NULL,
                             PRIMARY KEY (id),
                             CONSTRAINT fk_acervo_foto_item
                                 FOREIGN KEY (id) REFERENCES acervo_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE acervo_midia (
                              id BINARY(16) NOT NULL,
                              tipo_midia VARCHAR(20) NULL,
                              duracao VARCHAR(50) NULL,
                              produtora VARCHAR(150) NULL,
                              PRIMARY KEY (id),
                              CONSTRAINT fk_acervo_midia_item
                                  FOREIGN KEY (id) REFERENCES acervo_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
