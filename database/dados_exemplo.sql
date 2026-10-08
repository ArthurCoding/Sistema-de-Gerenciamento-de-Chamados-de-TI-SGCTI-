USE sgcti_db;

INSERT INTO tecnico (nome, cpf, especialidade, email, telefone) VALUES
('Rafael Monteiro','52998224725','Redes e infraestrutura','rafael@empresa.com','(85) 99911-2233'),
('Camila Duarte','11144477735','Suporte a desktops','camila@empresa.com','(85) 98877-4455'),
('Bruno Siqueira','39053344705','Banco de dados','bruno@empresa.com','(85) 3222-9080');

INSERT INTO usuario_solicitante (nome, setor, email, telefone) VALUES
('Ana Paula Lima','Financeiro','ana@empresa.com','(85) 99701-1020'),
('Marcos Vinícius','Recursos Humanos','marcos@empresa.com','(85) 99602-3040'),
('Juliana Prado','Comercial','juliana@empresa.com','(85) 99503-5060'),
('Pedro Alves','Logística','pedro@empresa.com','(85) 99404-7080');

INSERT INTO chamado (id,titulo,descricao,data_abertura,data_fechamento,tecnico_responsavel_id,usuario_solicitante_id,prioridade,status,solucao) VALUES
(1,'Sem acesso à rede Wi-Fi','Notebook não conecta à rede corporativa desde ontem.',NOW()-INTERVAL 20 DAY,NOW()-INTERVAL 19 DAY,1,1,'Alta','Resolvido','Reset do perfil de rede e renovação do certificado.'),
(2,'Impressora do RH não imprime','Fila de impressão travada na impressora do segundo andar.',NOW()-INTERVAL 12 DAY,NOW()-INTERVAL 11 DAY,2,2,'Média','Resolvido','Spooler reiniciado e driver reinstalado.'),
(3,'Instalar pacote de planilhas','Preciso do aplicativo de planilhas no computador novo.',NOW()-INTERVAL 9 DAY,NULL,NULL,3,'Baixa','Cancelado',NULL),
(4,'Relatório de vendas muito lento','A consulta mensal no sistema comercial leva mais de 5 minutos.',NOW()-INTERVAL 6 DAY,NULL,3,3,'Alta','Em Atendimento',NULL),
(5,'Monitor sem imagem','O monitor da estação 14 liga, mas fica sem sinal.',NOW()-INTERVAL 3 DAY,NULL,2,4,'Média','Aberto',NULL),
(6,'Criar e-mail para novo colaborador','Colaborador inicia na segunda-feira e precisa de e-mail e acessos básicos.',NOW()-INTERVAL 2 DAY,NULL,NULL,2,'Alta','Aberto',NULL),
(7,'VPN desconecta a cada 10 minutos','Trabalhando em home office, a VPN cai com frequência.',NOW()-INTERVAL 1 DAY,NULL,1,1,'Média','Em Atendimento',NULL),
(8,'Backup da pasta compartilhada','Solicito rotina de backup semanal da pasta do setor.',NOW(),NULL,NULL,4,'Baixa','Aberto',NULL);

INSERT INTO historico_chamado (chamado_id,data_evento,descricao) VALUES
(1,NOW()-INTERVAL 20 DAY,'Chamado aberto por Ana Paula Lima'),(1,NOW()-INTERVAL 20 DAY,'Atribuído a Rafael Monteiro'),(1,NOW()-INTERVAL 19 DAY,'Resolvido: perfil de rede redefinido'),
(2,NOW()-INTERVAL 12 DAY,'Chamado aberto por Marcos Vinícius'),(2,NOW()-INTERVAL 11 DAY,'Resolvido: spooler reiniciado'),
(3,NOW()-INTERVAL 9 DAY,'Chamado aberto por Juliana Prado'),(3,NOW()-INTERVAL 8 DAY,'Chamado cancelado por Administrador'),
(4,NOW()-INTERVAL 6 DAY,'Chamado aberto por Juliana Prado'),(4,NOW()-INTERVAL 6 DAY,'Atribuído a Bruno Siqueira'),(4,NOW()-INTERVAL 5 DAY,'Atendimento iniciado por Bruno Siqueira'),
(5,NOW()-INTERVAL 3 DAY,'Chamado aberto por Pedro Alves'),(5,NOW()-INTERVAL 3 DAY,'Atribuído a Camila Duarte'),
(6,NOW()-INTERVAL 2 DAY,'Chamado aberto por Marcos Vinícius'),
(7,NOW()-INTERVAL 1 DAY,'Chamado aberto por Ana Paula Lima'),(7,NOW()-INTERVAL 1 DAY,'Atribuído a Rafael Monteiro'),(7,NOW(),'Atendimento iniciado por Rafael Monteiro'),
(8,NOW(),'Chamado aberto por Pedro Alves');
