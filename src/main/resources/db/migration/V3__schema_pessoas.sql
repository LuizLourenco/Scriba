CREATE TABLE tipo_leitor (
                             id BINARY(16) NOT NULL,
                             instituicao_id BINARY(16) NOT NULL,
                             nome VARCHAR(100) NOT NULL,
                             prazo_padrao_dias INT NOT NULL DEFAULT 14,
                             limite_emprestimos INT NOT NULL DEFAULT 3,
                             ativo BIT NOT NULL DEFAULT b'1',
                             criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             UNIQUE KEY uq_tipo_leitor_nome_inst (instituicao_id, nome),
                             CONSTRAINT fk_tipo_leitor_instituicao
                                 FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE leitor (
                        id BINARY(16) NOT NULL,
                        instituicao_id BINARY(16) NOT NULL,
                        tipo_leitor_id BINARY(16) NOT NULL,
                        nome VARCHAR(150) NOT NULL,
                        cpf VARCHAR(14) NOT NULL,
                        email VARCHAR(150) NOT NULL,
                        telefone VARCHAR(30) NULL,
                        ativo BIT NOT NULL DEFAULT b'1',
                        criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (id),
                        UNIQUE KEY uq_leitor_cpf_inst (instituicao_id, cpf),
                        UNIQUE KEY uq_leitor_email_inst (instituicao_id, email),
                        CONSTRAINT fk_leitor_instituicao
                            FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
                        CONSTRAINT fk_leitor_tipo
                            FOREIGN KEY (tipo_leitor_id) REFERENCES tipo_leitor (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE fornecedor (
                            id BINARY(16) NOT NULL,
                            instituicao_id BINARY(16) NOT NULL,
                            nome VARCHAR(150) NOT NULL,
                            tipo VARCHAR(2) NOT NULL,
                            cpf_cnpj VARCHAR(18) NOT NULL,
                            email VARCHAR(150) NULL,
                            telefone VARCHAR(30) NULL,
                            ativo BIT NOT NULL DEFAULT b'1',
                            criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (id),
                            UNIQUE KEY uq_fornecedor_doc_inst (instituicao_id, cpf_cnpj),
                            CONSTRAINT fk_fornecedor_instituicao
                                FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
