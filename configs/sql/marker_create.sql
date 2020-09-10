-- a simple table used to check if the restore process is complete
use radius;
CREATE TABLE `radius`.`marker_table` (
`creation_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);