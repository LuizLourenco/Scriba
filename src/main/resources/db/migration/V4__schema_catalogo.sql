CREATE TABLE autor (
                       id BINARY(16) NOT NULL,
                       instituicao_id BINARY(16) NOT NULL,
                       nome VARCHAR(150) NOT NULL,
                       nacionalidade VARCHAR(100) NULL,
                       biografia TEXT NULL,
                       ativo BIT NOT NULL DEFAULT b'1',
                       criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       UNIQUE KEY uq_autor_nome_inst (instituicao_id, nome),
                       CONSTRAINT fk_autor_instituicao
                           FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE editora (
                         id BINARY(16) NOT NULL,
                         instituicao_id BINARY(16) NOT NULL,
                         nome VARCHAR(150) NOT NULL,
                         cidade VARCHAR(100) NULL,
                         pais VARCHAR(100) NULL,
                         ativo BIT NOT NULL DEFAULT b'1',
                         criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         UNIQUE KEY uq_editora_nome_inst (instituicao_id, nome),
                         CONSTRAINT fk_editora_instituicao
                             FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categoria (
                           id BINARY(16) NOT NULL,
                           instituicao_id BINARY(16) NOT NULL,
                           categoria_pai_id BINARY(16) NULL,
                           nome VARCHAR(120) NOT NULL,
                           descricao VARCHAR(255) NULL,
                           ativo BIT NOT NULL DEFAULT b'1',
                           criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (id),
                           UNIQUE KEY uq_categoria_nome_inst (instituicao_id, nome),
                           CONSTRAINT fk_categoria_instituicao
                               FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
                           CONSTRAINT fk_categoria_pai
                               FOREIGN KEY (categoria_pai_id) REFERENCES categoria (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE classificacao (
                               id BINARY(16) NOT NULL,
                               instituicao_id BINARY(16) NOT NULL,
                               padrao VARCHAR(20) NOT NULL,
                               codigo VARCHAR(50) NOT NULL,
                               descricao VARCHAR(150) NOT NULL,
                               ativo BIT NOT NULL DEFAULT b'1',
                               criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (id),
                               UNIQUE KEY uq_classificacao_codigo_inst (instituicao_id, codigo),
                               CONSTRAINT fk_classificacao_instituicao
                                   FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
