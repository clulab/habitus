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
	`name` VARCHAR(45) COLLATE utf8_bin NOT NULL,
	`regionId` INT,
    FOREIGN KEY (`regionId`) REFERENCES `region`(`id`)
);
-- -----------------------------------------------------
-- Table `geo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `geo` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR(45) COLLATE utf8_bin NOT NULL,
	`regionId` INT,
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
	`byline` VARCHAR(45) COLLATE utf8_bin NULL DEFAULT NULL,
	`date` DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (`datasetId`) REFERENCES `dataset`(`id`)
);
-- -----------------------------------------------------
-- Table `documentTerms`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `documentTerms` (
	`documentId` INT,
	`termId` INT,
    FOREIGN KEY (`documentId`) REFERENCES `document`(`id`),
    FOREIGN KEY (`termId`) REFERENCES `term`(`id`)
);








-- -----------------------------------------------------
-- Table `book`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `book` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`key` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`mdate` DATE NULL,
	`publtype` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `incollection`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `incollection` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`key` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`mdate` DATE NULL,
	`publtype` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `inproceedings`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `inproceedings` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`key` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`mdate` DATE NULL,
	`publtype` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `mastersthesis`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mastersthesis` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`key` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`mdate` DATE NULL,
	`publtype` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `phdthesis`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `phdthesis` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`key` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`mdate` DATE NULL,
	`publtype` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `proceedings`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `proceedings` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`key` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`mdate` DATE NULL,
	`publtype` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `www`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `www` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`key` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`mdate` DATE NULL,
	`publtype` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `wwwhome`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `wwwhome` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`key` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`mdate` DATE NULL,
	`publtype` VARCHAR(255) NULL
);


-- -----------------------------------------------------
-- Table `address`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `address` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `author`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `author` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`disambiguator` VARCHAR(15) COLLATE utf8_bin NOT NULL DEFAULT '',
	`aux` VARCHAR(255) NULL,
	`bibtex` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `authorhome`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `authorhome` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`disambiguator` VARCHAR(15) COLLATE utf8_bin NOT NULL DEFAULT '',
	`aux` VARCHAR(255) NULL,
	`bibtex` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `booktitle`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `booktitle` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `cdrom`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `cdrom` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `chapter`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chapter` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `cite`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `cite` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL,
	`label` VARCHAR(255) NULL,
	`ref` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `crossref`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `crossref` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `editor`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `editor` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL,
	`aux` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `ee`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ee` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(512) NOT NULL
);
-- -----------------------------------------------------
-- Table `isbn`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `isbn` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `journal`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `journal` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `month`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `month` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `note`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `note` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL,
	`type` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `number`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `number` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `pages`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `pages` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `publisher`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `publisher` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL,
	`href` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `school`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `school` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `series`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `series` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL,
	`href` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `title`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `title` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(2048) NOT NULL,
	`bibtex` VARCHAR(255) NULL
);
-- -----------------------------------------------------
-- Table `url`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `url` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(512) NOT NULL
);
-- -----------------------------------------------------
-- Table `volume`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `volume` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Table `year`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `year` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`elementKey` VARCHAR(255) COLLATE utf8_bin NOT NULL,
	`index` INT NOT NULL,
	`value` VARCHAR(255) NOT NULL
);
-- -----------------------------------------------------
-- Epilogue
-- -----------------------------------------------------
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
-- -----------------------------------------------------
