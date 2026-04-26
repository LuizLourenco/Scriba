CREATE TABLE instituicao (
                             id BINARY(16) NOT NULL,
                             nome VARCHAR(150) NOT NULL,
                             codigo VARCHAR(50) NOT NULL,
                             ativo BIT NOT NULL DEFAULT b'1',
                             criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             UNIQUE KEY uq_instituicao_codigo (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE biblioteca (
                            id BINARY(16) NOT NULL,
                            instituicao_id BINARY(16) NOT NULL,
                            nome VARCHAR(150) NOT NULL,
                            codigo VARCHAR(50) NOT NULL,
                            ativo BIT NOT NULL DEFAULT b'1',
                            criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (id),
                            UNIQUE KEY uq_biblioteca_codigo (codigo),
                            CONSTRAINT fk_biblioteca_instituicao
                                FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario (
                         id BINARY(16) NOT NULL,
                         instituicao_id BINARY(16) NOT NULL,
                         biblioteca_id BINARY(16) NULL,
                         nome VARCHAR(150) NOT NULL,
                         email VARCHAR(150) NOT NULL,
                         senha VARCHAR(255) NOT NULL,
                         role VARCHAR(50) NOT NULL,
                         ativo BIT NOT NULL DEFAULT b'1',
                         criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         UNIQUE KEY uq_usuario_email (email),
                         CONSTRAINT fk_usuario_instituicao
                             FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
                         CONSTRAINT fk_usuario_biblioteca
                             FOREIGN KEY (biblioteca_id) REFERENCES biblioteca (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;