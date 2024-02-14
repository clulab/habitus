-- -----------------------------------------------------
-- Prologue
-- -----------------------------------------------------
USE `habitus`;
-- -----------------------------------------------------
INSERT INTO `region` (`name`)
VALUES ('uganda');

INSERT INTO `dataset` (`regionId`, `name`)
VALUES
    (1, 'uganda-mining.tsv'),
    (1, 'uganda-pdfs.tsv'),
    (1, 'uganda-pdfs-karamoja.tsv'),
    (1, 'uganda-sneakpeek.tsv'),
    (1, 'uganda.tsv');


INSERT INTO `term` (`name`)
VALUES
    ('karamoja'),
    ('pastoralist'),
    ('transhumance'),
    ('uganda china'),
    ('uganda farming'),
    ('uganda mining');
-- -----------------------------------------------------
-- Epilogue
-- -----------------------------------------------------

-- -----------------------------------------------------
