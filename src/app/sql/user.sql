CREATE USER 'user'@'localhost' IDENTIFIED BY 'user';

GRANT SELECT ON bms.Banquet TO 'user'@'localhost';
GRANT UPDATE ON bms.Banquet TO 'user'@'localhost';

GRANT SELECT ON bms.Meal TO 'user'@'localhost';

GRANT SELECT ON bms.Account TO 'user'@'localhost';
GRANT INSERT ON bms.Account TO 'user'@'localhost';
GRANT UPDATE ON bms.Account TO 'user'@'localhost';

GRANT SELECT ON bms.Admission TO 'user'@'localhost';
GRANT INSERT ON bms.Admission TO 'user'@'localhost';
GRANT UPDATE ON bms.Admission TO 'user'@'localhost';
GRANT DELETE ON bms.Admission TO 'user'@'localhost';

FLUSH PRIVILEGES;