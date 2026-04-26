ALTER TABLE instituicao
    ADD COLUMN prazo_padrao_dias INT NOT NULL DEFAULT 14,
    ADD COLUMN limite_emprestimos INT NOT NULL DEFAULT 3,
    ADD COLUMN valor_multa DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
