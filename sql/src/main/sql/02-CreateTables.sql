-- -----------------------------------------------------
-- Prologue
-- -----------------------------------------------------
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

USE `habitus`;
-- -----------------------------------------------------
-- Table `region`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `region` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR(45) COLLATE utf8_bin NOT NULL
);
-- -----------------------------------------------------
-- Table `dataset`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dataset` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`regionId` INT,
	`name` VARCHAR(45) COLLATE utf8_bin NOT NULL,
    FOREIGN KEY (`regionId`) REFERENCES `region`(`id`)
);
-- -----------------------------------------------------
-- Table `geo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `geo` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`regionId` INT,
	`name` VARCHAR(45) COLLATE utf8_bin NOT NULL,
	`lat` FLOAT NULL DEFAULT NULL,
	`lon` FLOAT NULL DEFAULT NULL,
    FOREIGN KEY (`regionId`) REFERENCES `region`(`id`)
);
-- -----------------------------------------------------
-- Table `term`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `term` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR(45) COLLATE utf8_bin NOT NULL
);
-- -----------------------------------------------------
-- Table `document`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `document` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`datasetId` INT,
	`url` VARCHAR(255) COLLATE utf8_bin NULL DEFAULT NULL,
	`title` VARCHAR(255) COLLATE utf8_bin NULL DEFAULT NULL,
	`dateline` VARCHAR(45) COLLATE utf8_bin NULL DEFAULT NULL,
	`byline` VARCHAR(63) COLLATE utf8_bin NULL DEFAULT NULL,
	`date` DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (`datasetId`) REFERENCES `dataset`(`id`),
    CONSTRAINT `unique_document` UNIQUE (`datasetId`, `url`)
);
-- -----------------------------------------------------
-- Table `documentTerms`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `documentTerms` (
	`documentId` INT,
	`termId` INT,
    FOREIGN KEY (`documentId`) REFERENCES `document`(`id`),
    FOREIGN KEY (`termId`) REFERENCES `term`(`id`),
    CONSTRAINT `unique_document_term` UNIQUE (`documentId`, `termId`)
);
-- -----------------------------------------------------
-- Table `sentence`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sentence` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`documentId` INT,
    `index` INT NOT NULL,
    `text` VARCHAR(1024) COLLATE utf8_bin NOT NULL,
    `isBelief` BOOL NOT NULL,
    `sentiment` FLOAT NULL DEFAULT NULL,
    FOREIGN KEY (`documentId`) REFERENCES `document`(`id`),
    CONSTRAINT `unique_sentence` UNIQUE (`documentId`, `index`)
);
-- -----------------------------------------------------
-- Table `sentenceLocations`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sentenceLocations` (
	`sentenceId` INT NOT NULL,
	`name` VARCHAR(45) COLLATE utf8_bin NOT NULL,
	`lat` FLOAT NULL DEFAULT NULL,
	`lon` FLOAT NULL DEFAULT NULL,
    FOREIGN KEY (`sentenceId`) REFERENCES `sentence`(`id`),
    CONSTRAINT `unique_sentence_location` UNIQUE (`sentenceId`, `name`)
);
-- -----------------------------------------------------
-- Table `sentenceCausalRelations`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sentenceCausalRelations` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`sentenceId` INT NOT NULL,
    `index` INT NOT NULL,
	`negationCount` INT NOT NULL,

    `causeText` VARCHAR(1024) COLLATE utf8_bin NOT NULL,
    `causeIncCount` INT NOT NULL,
	`causeDecCount` INT NOT NULL,
	`causePosCount` INT NOT NULL,
	`causeNegCount` INT NOT NULL,

    `effectText` VARCHAR(1024) COLLATE utf8_bin NOT NULL,
    `effectIncCount` INT NOT NULL,
	`effectDecCount` INT NOT NULL,
	`effectPosCount` INT NOT NULL,
	`effectNegCount` INT NOT NULL,

    FOREIGN KEY (`sentenceId`) REFERENCES `sentence`(`id`),
    CONSTRAINT `unique_sentence_causal_relation` UNIQUE (`sentenceId`, `index`)
);
-- -----------------------------------------------------
-- Epilogue
-- -----------------------------------------------------
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
-- -----------------------------------------------------
