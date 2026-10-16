-- Las semillas solo agregan identificadores ausentes, sin sobrescribir cambios.
INSERT INTO lugar (id,nombre) SELECT 1,'Aula 1' WHERE NOT EXISTS(SELECT 1 FROM lugar WHERE id=1);
INSERT INTO lugar (id,nombre) SELECT 2,'Biblioteca' WHERE NOT EXISTS(SELECT 1 FROM lugar WHERE id=2);
INSERT INTO lugar (id,nombre) SELECT 3,'Laboratorio' WHERE NOT EXISTS(SELECT 1 FROM lugar WHERE id=3);
INSERT INTO lugar (id,nombre) SELECT 4,'Patio' WHERE NOT EXISTS(SELECT 1 FROM lugar WHERE id=4);
INSERT INTO categoria (id,nombre) SELECT 1,'Electricidad' WHERE NOT EXISTS(SELECT 1 FROM categoria WHERE id=1);
INSERT INTO categoria (id,nombre) SELECT 2,'Mobiliario' WHERE NOT EXISTS(SELECT 1 FROM categoria WHERE id=2);
INSERT INTO categoria (id,nombre) SELECT 3,'Limpieza' WHERE NOT EXISTS(SELECT 1 FROM categoria WHERE id=3);
INSERT INTO categoria (id,nombre) SELECT 4,'Otros' WHERE NOT EXISTS(SELECT 1 FROM categoria WHERE id=4);
