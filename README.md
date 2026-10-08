# SGCTI – Sistema de Gerenciamento de Chamados de TI

Java 17 + Spring Boot 3.3.5, JDBC (JdbcTemplate) e MySQL 8.0.16+. Relatórios em PDF com OpenPDF.
Arquitetura MVC em camadas: `model`, `dao`, `service`, `controller`. A tela (HTML/CSS/JS) fica em `src/main/resources/static/index.html`.

## Como rodar
1. Instalar MySQL Server 8, Java 17 (JDK) e Maven (ou usar IntelliJ/Eclipse).
2. No MySQL Workbench (usuário root), executar `database/sgcti_db.sql` e depois `database/dados_exemplo.sql` (opcional).
3. No PowerShell, na pasta do projeto: `$env:DB_PASSWORD="senha_do_mysql"` (se não for root, também `$env:DB_USER="usuario"`).
4. `mvn spring-boot:run` (ou executar a classe `SgctiApplication` na IDE).
5. Abrir http://localhost:8080

A senha do MySQL nunca vai no código: é lida da variável de ambiente `DB_PASSWORD`.
Use sempre o `database/sgcti_db.sql` deste projeto; o script original não tem as colunas/tabelas que o sistema usa.

## Perfis
- **Administrador:** cadastros, chamados, atribuição de técnico, edição, exclusão e relatórios.
- **Técnico:** vê a fila, inicia o atendimento, registra a solução e cancela os seus chamados.
- **Usuário solicitante:** abre chamados, vê só os seus, edita e cancela enquanto estiverem Abertos.

Login simplificado por perfil, sem senha. As permissões são conferidas no servidor (cabeçalhos `X-Perfil` e `X-Id`).
