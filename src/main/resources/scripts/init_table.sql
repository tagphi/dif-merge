CREATE TABLE IF NOT EXISTS jobs
(
id INT NOT NULL PRIMARY KEY auto_increment,
tempFilePath VARCHAR(200),
`action` VARCHAR(100),
extraArgs VARCHAR(2048),
callbackUrl VARCHAR(200),
callbackArgs VARCHAR(200),
step VARCHAR(50),
status VARCHAR(10) DEFAULT '等待',
message VARCHAR(1024),
version INT,
createTime datetime NOT NULL DEFAULT NOW(),
modifiedTime datetime NOT NULL DEFAULT NOW()
);