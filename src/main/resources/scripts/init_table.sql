CREATE TABLE IF NOT EXISTS jobs
(
id INT NOT NULL PRIMARY KEY auto_increment,
tempFilePath VARCHAR(200),
`action` VARCHAR(100),
extraArgs Text,
callbackUrl VARCHAR(200),
callbackArgs VARCHAR(200),
step VARCHAR(50),
status VARCHAR(10),
message Text
);