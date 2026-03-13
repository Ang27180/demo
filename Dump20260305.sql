-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: sabormasterclass
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `acudiente`
--

DROP TABLE IF EXISTS `acudiente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acudiente` (
  `idAcudiente` int(11) NOT NULL AUTO_INCREMENT,
  `Autorizacion` varchar(45) DEFAULT NULL,
  `Persona_idPersona` int(11) DEFAULT NULL,
  `Id_Estudiente_dependiente` int(11) DEFAULT NULL,
  `persona_id_persona` int(11) DEFAULT NULL,
  PRIMARY KEY (`idAcudiente`),
  KEY `Persona_idPersona` (`Persona_idPersona`),
  KEY `fk_Id_Estudiente_dependiente` (`Id_Estudiente_dependiente`),
  KEY `FKojyv4sit7o732aro1a23mdoix` (`persona_id_persona`),
  CONSTRAINT `FKojyv4sit7o732aro1a23mdoix` FOREIGN KEY (`persona_id_persona`) REFERENCES `persona` (`id_persona`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `acudiente`
--

LOCK TABLES `acudiente` WRITE;
/*!40000 ALTER TABLE `acudiente` DISABLE KEYS */;
INSERT INTO `acudiente` VALUES (1,'Autorizado',2,5,NULL),(2,'Pendiente',3,6,NULL),(3,'Rechazado',4,7,NULL);
/*!40000 ALTER TABLE `acudiente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admi_sisitema`
--

DROP TABLE IF EXISTS `admi_sisitema`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admi_sisitema` (
  `idAdmi_Sisitema` int(11) NOT NULL AUTO_INCREMENT,
  `Codigo_admin` varchar(45) NOT NULL,
  `id_estado_administrador` int(11) DEFAULT NULL,
  `Fecha_Inicio` date DEFAULT NULL,
  `Ultima_Conexión` varchar(45) DEFAULT NULL,
  `Persona_idPersona` int(11) DEFAULT NULL,
  PRIMARY KEY (`idAdmi_Sisitema`),
  KEY `Persona_idPersona` (`Persona_idPersona`),
  KEY `fk_id_estado_admin` (`id_estado_administrador`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admi_sisitema`
--

LOCK TABLES `admi_sisitema` WRITE;
/*!40000 ALTER TABLE `admi_sisitema` DISABLE KEYS */;
INSERT INTO `admi_sisitema` VALUES (1,'1',1,'2025-02-13','2025-02-18',1);
/*!40000 ALTER TABLE `admi_sisitema` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `idCategoria` int(11) NOT NULL AUTO_INCREMENT,
  `Nombre` varchar(100) NOT NULL,
  `Estudiante_id_Estudiante` int(11) DEFAULT NULL,
  PRIMARY KEY (`idCategoria`),
  KEY `fk_id_Estudiante` (`Estudiante_id_Estudiante`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (1,'Cocina Francesa',1),(2,'Postres',2),(3,'Cocina Oriental',3),(4,'Comida de mar',1);
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `curso`
--

DROP TABLE IF EXISTS `curso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `curso` (
  `id_curso` int(11) NOT NULL AUTO_INCREMENT,
  `duracion` varchar(255) DEFAULT NULL,
  `numero_curso` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `Detalle` varchar(255) NOT NULL,
  `costo` double DEFAULT NULL,
  `nivel_aprendizaje` varchar(255) DEFAULT NULL,
  `Categoria_idCategoria` int(11) NOT NULL,
  `categoria` int(11) DEFAULT NULL,
  `imagen` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_curso`),
  KEY `Categoria_idCategoria` (`Categoria_idCategoria`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `curso`
--

LOCK TABLES `curso` WRITE;
/*!40000 ALTER TABLE `curso` DISABLE KEYS */;
INSERT INTO `curso` VALUES (1,'4 semanas','CUR-001','Repostería Creativa','Domina el arte de la repostería con técnicas profesionales.',295000,'Principiante',1,1,'https://cdn.pixabay.com/photo/2017/07/28/14/23/macarons-2548810_1280.jpg'),(2,'4 semanas','CUR-002','Cocina Internacional','Explora sabores del mundo con este curso multicultural.',134500,'Principiante',1,1,'https://media.istockphoto.com/id/1131749859/photo/global-hunger-issue.jpg?s=1024x1024&w=is&k=20&c=T3g9BdIEnKTBpbGgYDLSYZq8qgBOP3Y3Y01KcnFU0kc='),(3,'4 semanas','CUR-003','Cocina Japonesa Tradicional','Sumérgete en los sabores auténticos del Japón: sushi, ramen, tempura y más.',149000,'Principiante',1,1,'https://cdn.pixabay.com/photo/2022/02/11/05/53/new-year-7006581_1280.jpg'),(4,'4 semanas','CUR-004','Gastronomía Vegetariana Moderna','Aprende a crear platos vegetarianos equilibrados y deliciosos con ingredientes frescos.',128000,'Principiante',1,1,'https://cdn.pixabay.com/photo/2025/03/06/13/17/pasta-9450866_1280.jpg'),(5,'4 semanas','CUR-005','Sabores del Medio Oriente','Descubre el arte culinario del hummus, falafel, shawarma y especias únicas.',139000,'Principiante',1,1,'https://media.istockphoto.com/id/615707428/photo/variety-of-spices-on-the-arab-street-market-stall.jpg?s=1024x1024&w=is&k=20&c=BOVNBljdECHS8wRJYzUsaUDwVV3dzjqUzC7SJXL34CA='),(6,'4 semanas','CUR-006','Panadería Artesanal desde Cero','Domina las técnicas para hornear panes rústicos, integrales y fermentados.',132000,'Principiante',1,1,'https://cdn.pixabay.com/photo/2025/03/12/08/33/bread-9464305_1280.jpg'),(7,'4 semanas','CUR-007','Técnicas de Parrilla y Asado','Conviértete en maestro del asado con cortes, marinados y técnicas de cocción.',137000,'Principiante',1,1,'https://cdn.pixabay.com/photo/2016/11/19/12/44/burgers-1839090_1280.jpg'),(8,'4 semanas','CUR-008','Cocina Italiana Clásica','Aprende a preparar pasta fresca, risottos y postres italianos como un chef.',142000,'Principiante',1,1,'https://cdn.pixabay.com/photo/2023/04/24/10/46/horizontal-7947790_1280.jpg');
/*!40000 ALTER TABLE `curso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_adim`
--

DROP TABLE IF EXISTS `estado_adim`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_adim` (
  `Id_estado` int(11) NOT NULL,
  `Estado` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`Id_estado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_adim`
--

LOCK TABLES `estado_adim` WRITE;
/*!40000 ALTER TABLE `estado_adim` DISABLE KEYS */;
INSERT INTO `estado_adim` VALUES (1,'Activo'),(2,'Inactivo'),(3,'Suspendido'),(4,'Retirado');
/*!40000 ALTER TABLE `estado_adim` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_inscripcion`
--

DROP TABLE IF EXISTS `estado_inscripcion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_inscripcion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_inscripcion`
--

LOCK TABLES `estado_inscripcion` WRITE;
/*!40000 ALTER TABLE `estado_inscripcion` DISABLE KEYS */;
INSERT INTO `estado_inscripcion` VALUES (1,'Inscrito'),(2,'Inactivo'),(3,'Suspendido');
/*!40000 ALTER TABLE `estado_inscripcion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estudiante`
--

DROP TABLE IF EXISTS `estudiante`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estudiante` (
  `id_estudiante` int(11) NOT NULL AUTO_INCREMENT,
  `id_estado_estudiante` int(11) DEFAULT NULL,
  `Progreso` varchar(45) NOT NULL,
  `persona_id_persona` int(11) NOT NULL,
  PRIMARY KEY (`id_estudiante`),
  KEY `fk_id_estado_estudiante` (`id_estado_estudiante`),
  KEY `fk_estudiante_persona` (`persona_id_persona`),
  CONSTRAINT `FKnfye4bfve40mvadhjvrcjh0en` FOREIGN KEY (`persona_id_persona`) REFERENCES `persona` (`id_persona`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estudiante`
--

LOCK TABLES `estudiante` WRITE;
/*!40000 ALTER TABLE `estudiante` DISABLE KEYS */;
INSERT INTO `estudiante` VALUES (1,1,'En curso',5),(2,2,'Sin terminar',6),(3,3,'En proceso de validacion ',7),(4,1,'0%',27);
/*!40000 ALTER TABLE `estudiante` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historial_curso`
--

DROP TABLE IF EXISTS `historial_curso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_curso` (
  `idHistorial_Curso` int(11) NOT NULL AUTO_INCREMENT,
  `Cursos_Tomados` varchar(100) DEFAULT NULL,
  `Cantidad_Curso` varchar(100) DEFAULT NULL,
  `Estudiante_idEstudiante` int(11) DEFAULT NULL,
  `Curso_idCurso` int(11) DEFAULT NULL,
  PRIMARY KEY (`idHistorial_Curso`),
  KEY `Estudiante_idEstudiante` (`Estudiante_idEstudiante`),
  KEY `Curso_idCurso` (`Curso_idCurso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historial_curso`
--

LOCK TABLES `historial_curso` WRITE;
/*!40000 ALTER TABLE `historial_curso` DISABLE KEYS */;
/*!40000 ALTER TABLE `historial_curso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inscripcion`
--

DROP TABLE IF EXISTS `inscripcion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inscripcion` (
  `id_inscripcion` int(11) NOT NULL AUTO_INCREMENT,
  `Fecha_Inscripcion` date NOT NULL,
  `id_estudiante` int(11) DEFAULT NULL,
  `id_curso` int(11) DEFAULT NULL,
  `id_estado` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_inscripcion`),
  UNIQUE KEY `uk_estudiante_curso` (`id_estudiante`,`id_curso`),
  KEY `fk_inscripcion_curso` (`id_curso`),
  KEY `fk_inscripcion_estado` (`id_estado`),
  CONSTRAINT `FK6sw931ij561k2fq5sx4xonmlr` FOREIGN KEY (`id_curso`) REFERENCES `curso` (`id_curso`),
  CONSTRAINT `FK8hgaf3r1wwea199gneh6m8035` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiante` (`id_estudiante`),
  CONSTRAINT `FKgslukrtbndg2ye84xidkaj85y` FOREIGN KEY (`id_estado`) REFERENCES `estado_inscripcion` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inscripcion`
--

LOCK TABLES `inscripcion` WRITE;
/*!40000 ALTER TABLE `inscripcion` DISABLE KEYS */;
INSERT INTO `inscripcion` VALUES (5,'2025-12-11',1,1,1),(7,'2025-12-11',1,2,1),(8,'2025-12-11',2,1,1),(9,'2025-12-11',1,3,1),(10,'2026-03-04',4,4,1);
/*!40000 ALTER TABLE `inscripcion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventario`
--

DROP TABLE IF EXISTS `inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventario` (
  `idInventario` int(11) NOT NULL AUTO_INCREMENT,
  `Cantidad` int(11) NOT NULL,
  `Ultima_Actualización` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `Fecha_Registro` date NOT NULL,
  `Stock_Minimo` int(11) NOT NULL,
  `Stock_Maximo` int(11) NOT NULL,
  PRIMARY KEY (`idInventario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventario`
--

LOCK TABLES `inventario` WRITE;
/*!40000 ALTER TABLE `inventario` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leccion`
--

DROP TABLE IF EXISTS `leccion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leccion` (
  `id_leccion` int(11) NOT NULL,
  `contenido_tipo` varchar(255) DEFAULT NULL,
  `contenido_url` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `id_modulo` int(11) NOT NULL,
  PRIMARY KEY (`id_leccion`),
  KEY `FKfsyi4wxtyd0eh2qjxeiytljmv` (`id_modulo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leccion`
--

LOCK TABLES `leccion` WRITE;
/*!40000 ALTER TABLE `leccion` DISABLE KEYS */;
/*!40000 ALTER TABLE `leccion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modulo`
--

DROP TABLE IF EXISTS `modulo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `modulo` (
  `id_modulo` int(11) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `id_curso` int(11) NOT NULL,
  KEY `FKaw646dhtmorjd4oawt3mlvunx` (`id_curso`),
  CONSTRAINT `FKaw646dhtmorjd4oawt3mlvunx` FOREIGN KEY (`id_curso`) REFERENCES `curso` (`id_curso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modulo`
--

LOCK TABLES `modulo` WRITE;
/*!40000 ALTER TABLE `modulo` DISABLE KEYS */;
/*!40000 ALTER TABLE `modulo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notificación`
--

DROP TABLE IF EXISTS `notificación`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificación` (
  `idNotificación` int(11) NOT NULL,
  `Codigo_Notificacion` varchar(45) DEFAULT NULL,
  `Mensaje` varchar(45) DEFAULT NULL,
  `Fecha_Envio` varchar(45) DEFAULT NULL,
  `Tipo` varchar(45) DEFAULT NULL,
  `Admi_Sisitema_idAdmi_Sisitema` int(11) DEFAULT NULL,
  `Estudiante_idEstudiante` int(11) DEFAULT NULL,
  PRIMARY KEY (`idNotificación`),
  KEY `Admi_Sisitema_idAdmi_Sisitema` (`Admi_Sisitema_idAdmi_Sisitema`),
  KEY `Estudiante_idEstudiante` (`Estudiante_idEstudiante`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificación`
--

LOCK TABLES `notificación` WRITE;
/*!40000 ALTER TABLE `notificación` DISABLE KEYS */;
INSERT INTO `notificación` VALUES (1,'1','Recuerda asistir a la clase de las 6 pm','2025-06-03','Recordatorio',1,1),(2,'2','Tu pago fue Exitoso','2025-02-05','Aviso',1,2),(3,'3','Certificado creado','2025-03-09','Informativo',1,3);
/*!40000 ALTER TABLE `notificación` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago`
--

DROP TABLE IF EXISTS `pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago` (
  `idPago` int(11) NOT NULL,
  `Monto` decimal(10,2) NOT NULL,
  `Fecha_Pago` date NOT NULL,
  `Metodo` varchar(45) NOT NULL,
  `Estado` varchar(45) NOT NULL,
  `Estudiante_idEstudiante` int(11) NOT NULL,
  PRIMARY KEY (`idPago`),
  KEY `Estudiante_idEstudiante` (`Estudiante_idEstudiante`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago`
--

LOCK TABLES `pago` WRITE;
/*!40000 ALTER TABLE `pago` DISABLE KEYS */;
/*!40000 ALTER TABLE `pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago_has_curso`
--

DROP TABLE IF EXISTS `pago_has_curso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago_has_curso` (
  `Pago_idPago` int(11) NOT NULL,
  `Curso_idCurso` int(11) NOT NULL,
  PRIMARY KEY (`Pago_idPago`,`Curso_idCurso`),
  KEY `Curso_idCurso` (`Curso_idCurso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago_has_curso`
--

LOCK TABLES `pago_has_curso` WRITE;
/*!40000 ALTER TABLE `pago_has_curso` DISABLE KEYS */;
/*!40000 ALTER TABLE `pago_has_curso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `persona`
--

DROP TABLE IF EXISTS `persona`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `persona` (
  `id_persona` int(11) NOT NULL AUTO_INCREMENT,
  `no_documento` varchar(255) DEFAULT NULL,
  `tipo_documento` varchar(255) DEFAULT NULL,
  `genero` varchar(255) DEFAULT NULL,
  `nombre_persona` varchar(100) NOT NULL,
  `email_persona` varchar(255) DEFAULT NULL,
  `Direccion_Persona` varchar(255) NOT NULL,
  `telefono_persona` varchar(255) DEFAULT NULL,
  `id_rol` int(11) DEFAULT NULL,
  `contrasena` varchar(255) NOT NULL,
  PRIMARY KEY (`id_persona`),
  KEY `fk_persona_rol` (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `persona`
--

LOCK TABLES `persona` WRITE;
/*!40000 ALTER TABLE `persona` DISABLE KEYS */;
INSERT INTO `persona` VALUES (1,'1001234567','CC','M','Juan Pérez','juan.perez@gmail.com','Calle 123 #45-66','3001234525',1,'JuanPerez89**'),(2,'1007654321','CC','F','María Rodríguez','maria.rodriguez@email.com','Carrera 10 #20-30','3017654321',NULL,''),(3,'1012345678','TI','M','Carlos Gómez','carlos.gomez@email.com','Av. Siempre Viva 742','3022345678',NULL,''),(4,'1023456789','CC','F','Ana Martínez','ana.martinez@email.com','Diagonal 25 #14-56','3033456789',NULL,''),(5,'1034567890','CE','M','David Fernández','david.fernandez@email.com','Transversal 8 #10-20','3044567890',2,'DavidFernandez54+'),(6,'1045678901','CC','F','Luisa Herrera','luisa.herrera@email.com','Calle 50 #20-10','3055678901',2,'LuisaHer21*'),(7,'1056789012','TI','M','Hanna Hernandez','hannajah09@email.com','Calle 12 #34-56','3066789012',2,''),(8,'1067890123','CC','F','Sofía Ramírez','sofia.ramirez@email.com','Carrera 15 #40-22','3077890123',3,'SofiRa321**'),(9,'1078901234','CE','M','Andrés Castro','andres.castro@email.com','Av. 7 #98-10','3088901234',3,'CastroA63+'),(10,'1089012345','CC','F','Valentina López','valentina.lopez@email.com','Diagonal 45 #23-89','3099012345',3,'ValentinaL85*'),(11,'1090123456','TI','M','Emilio Suárez','emilio.suarez@email.com','Transversal 60 #14-32','3100123456',4,'SuarezE234**'),(12,'1101234567','CC','F','Gabriela Ortega','gabriela.ortega@email.com','Calle 100 #50-40','3111234567',4,'GabiOrtega254*'),(13,'1112345678','CE','M','Ricardo Mendoza','ricardo.mendoza@email.com','Carrera 20 #60-10','3122345678',4,'DanielR875*'),(26,'1000934593','CC','Femenino','ANGELICA','ANGPPADILLA27@GMAIL.COM','CRA 94A','3026843166',1,'$2a$10$kfh2MEQsAzVvnWm6AifNWezFzQCgkAqRDCnVdAtyhCD.2VMTlF1E6'),(27,'1011254580','CC','Femenino','Hanna Hernandez','hhannahernandez21@gmail.com','cra 110#48 J56','3228100044',2,'$2a$10$7R2j4bbd1NZttRnbQtyM0OTpcqbYM.zCTG6vI9ooesm9LDbnaLxDC');
/*!40000 ALTER TABLE `persona` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `idProducto` int(11) NOT NULL AUTO_INCREMENT,
  `Cod_Producto` varchar(100) NOT NULL,
  `Nombre` varchar(100) NOT NULL,
  `Valor` decimal(10,2) NOT NULL,
  `Descripcion` varchar(255) NOT NULL,
  `Inventario_idInventario` int(11) NOT NULL,
  PRIMARY KEY (`idProducto`),
  UNIQUE KEY `Cod_Producto` (`Cod_Producto`),
  KEY `Inventario_idInventario` (`Inventario_idInventario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto_has_proveedor`
--

DROP TABLE IF EXISTS `producto_has_proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto_has_proveedor` (
  `Producto_idProducto` int(11) NOT NULL,
  `Proveedor_idProveedor` int(11) NOT NULL,
  PRIMARY KEY (`Producto_idProducto`,`Proveedor_idProveedor`),
  KEY `Proveedor_idProveedor` (`Proveedor_idProveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto_has_proveedor`
--

LOCK TABLES `producto_has_proveedor` WRITE;
/*!40000 ALTER TABLE `producto_has_proveedor` DISABLE KEYS */;
/*!40000 ALTER TABLE `producto_has_proveedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedor`
--

DROP TABLE IF EXISTS `proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedor` (
  `idProveedor` int(11) NOT NULL AUTO_INCREMENT,
  `Ubicacion` varchar(255) NOT NULL,
  `No_Proveedor` varchar(45) NOT NULL,
  `Persona_id_Persona` int(11) DEFAULT NULL,
  PRIMARY KEY (`idProveedor`),
  KEY `fk_id_persona` (`Persona_id_Persona`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedor`
--

LOCK TABLES `proveedor` WRITE;
/*!40000 ALTER TABLE `proveedor` DISABLE KEYS */;
INSERT INTO `proveedor` VALUES (1,'Francia','0001P',11),(2,'San Gil','0002P',12),(3,'Medellin','0003P',13);
/*!40000 ALTER TABLE `proveedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `id_Rol` int(11) NOT NULL,
  `descripcionRol` varchar(20) DEFAULT NULL,
  `descripcion_rol` varchar(50) NOT NULL,
  PRIMARY KEY (`id_Rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'Administrador',''),(2,'Estudiante',''),(3,'Tutor',''),(4,'Proveedor','');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tutor`
--

DROP TABLE IF EXISTS `tutor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tutor` (
  `id_tutor` int(11) NOT NULL AUTO_INCREMENT,
  `experiencia` varchar(255) DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  `persona_id_persona` int(11) DEFAULT NULL,
  `id_persona` int(11) NOT NULL,
  PRIMARY KEY (`id_tutor`),
  KEY `FKoeltf4idab14u9x3emsb36ng5` (`persona_id_persona`),
  CONSTRAINT `FKoeltf4idab14u9x3emsb36ng5` FOREIGN KEY (`persona_id_persona`) REFERENCES `persona` (`id_persona`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tutor`
--

LOCK TABLES `tutor` WRITE;
/*!40000 ALTER TABLE `tutor` DISABLE KEYS */;
INSERT INTO `tutor` VALUES (1,'5 años enseñando cocina','Especialista en repostería',1,0),(2,NULL,NULL,NULL,0),(3,'5 años en docencia infantil','Excelente comunicación con los estudiantes',1,0),(4,'5 años en docencia infantil','Excelente comunicación con los estudiantes',1,0),(5,'5 años enseñando matemáticas','Excelente comunicación con los estudiantes',NULL,0);
/*!40000 ALTER TABLE `tutor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tutor_has_curso`
--

DROP TABLE IF EXISTS `tutor_has_curso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tutor_has_curso` (
  `Tutor_idTutor` int(11) NOT NULL,
  `Curso_idCurso` int(11) NOT NULL,
  PRIMARY KEY (`Tutor_idTutor`,`Curso_idCurso`),
  KEY `Curso_idCurso` (`Curso_idCurso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tutor_has_curso`
--

LOCK TABLES `tutor_has_curso` WRITE;
/*!40000 ALTER TABLE `tutor_has_curso` DISABLE KEYS */;
/*!40000 ALTER TABLE `tutor_has_curso` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-05 21:27:49
