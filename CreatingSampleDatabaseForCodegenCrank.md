To try out the code generator, you need a database to try it against.

The example I test with uses MySQL.

First create a user called presto2 as follows:

```
CREATE USER 'presto2'@'localhost' IDENTIFIED BY 'presto2';
```

Assign the user some rights

```
GRANT ALL PRIVILEGES ON *.* TO 'presto2'@'localhost' IDENTIFIED BY 'presto2' WITH GRANT OPTION;
```

Second create a database called presto2 as follows:

```
CREATE DATABASE presto2;
```

Lastly create the following tables:

```
use presto2;

DROP TABLE IF EXISTS ROLE_EMPLOYEE, EMPLOYEE, DEPARTMENT, ROLE;

CREATE TABLE DEPARTMENT (
  ID INTEGER AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(30)
) ENGINE=InnoDB;

CREATE TABLE ROLE (
    ROLE_ID INTEGER AUTO_INCREMENT PRIMARY KEY,
    NAME VARCHAR(30) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE EMPLOYEE (
  EMP_ID INTEGER AUTO_INCREMENT PRIMARY KEY,
  FK_DEPARTMENT_ID INTEGER NULL,
  firstName VARCHAR(30) NOT NULL,
  LAST_NAME VARCHAR(30) NOT NULL,
  phone VARCHAR(30) NULL,
  FOREIGN KEY (FK_DEPARTMENT_ID) REFERENCES DEPARTMENT(ID)
) ENGINE=InnoDB;


CREATE TABLE ROLE_EMPLOYEE (
    FK_ROLE_ID INTEGER,
    FK_EMP_ID INTEGER,
    FOREIGN KEY (FK_ROLE_ID) REFERENCES ROLE(ROLE_ID),
    FOREIGN KEY (FK_EMP_ID) REFERENCES EMPLOYEE(EMP_ID)
) ENGINE=InnoDB;


```