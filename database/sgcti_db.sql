CREATE DATABASE IF NOT EXISTS sgcti_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sgcti_db;

CREATE TABLE IF NOT EXISTS tecnico (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf CHAR(11) NOT NULL UNIQUE,
    especialidade VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    telefone VARCHAR(20)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuario_solicitante (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    setor VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    telefone VARCHAR(20)
) ENGINE=InnoDB;

-- Alterações em relação ao script original:
--  * tecnico_responsavel_id aceita NULL (o chamado nasce sem técnico)
--  * nova coluna solucao
--  * CHECK em prioridade e status
--  * nova tabela historico_chamado
CREATE TABLE IF NOT EXISTS chamado (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT NOT NULL,
    data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento DATETIME NULL,
    tecnico_responsavel_id INT UNSIGNED NULL,
    usuario_solicitante_id INT UNSIGNED NOT NULL,
    prioridade VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    solucao TEXT NULL,
    CONSTRAINT ck_chamado_prioridade CHECK (prioridade IN ('Baixa','Média','Alta')),
    CONSTRAINT ck_chamado_status CHECK (status IN ('Aberto','Em Atendimento','Resolvido','Cancelado')),
    CONSTRAINT fk_chamado_tecnico
        FOREIGN KEY (tecnico_responsavel_id) REFERENCES tecnico (id),
    CONSTRAINT fk_chamado_usuario_solicitante
        FOREIGN KEY (usuario_solicitante_id) REFERENCES usuario_solicitante (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS historico_chamado (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    chamado_id INT UNSIGNED NOT NULL,
    data_evento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descricao VARCHAR(500) NOT NULL,
    CONSTRAINT fk_historico_chamado
        FOREIGN KEY (chamado_id) REFERENCES chamado (id) ON DELETE CASCADE
) ENGINE=InnoDB;
