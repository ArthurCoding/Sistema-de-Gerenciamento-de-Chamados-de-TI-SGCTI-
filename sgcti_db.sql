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

CREATE TABLE IF NOT EXISTS chamado (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT NOT NULL,
    data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento DATETIME NULL,
    tecnico_responsavel_id INT UNSIGNED NOT NULL,
    usuario_solicitante_id INT UNSIGNED NOT NULL,
    prioridade VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_chamado_tecnico
        FOREIGN KEY (tecnico_responsavel_id) REFERENCES tecnico (id),
    CONSTRAINT fk_chamado_usuario_solicitante
        FOREIGN KEY (usuario_solicitante_id) REFERENCES usuario_solicitante (id)
) ENGINE=InnoDB;