CREATE DATABASE IF NOT EXISTS bms;
USE bms;

DROP TABLE IF EXISTS Admission;
DROP TABLE IF EXISTS Meal;
DROP TABLE IF EXISTS Account;
DROP TABLE IF EXISTS Banquet;

CREATE TABLE Banquet (
  BIN INT AUTO_INCREMENT,

  Name VARCHAR(100) NOT NULL,
  Date DATE NOT NULL,
  Time TIME NOT NULL,
  Location VARCHAR(100) NOT NULL,
  Address VARCHAR(255) NOT NULL,
  ContactFirstName VARCHAR(50) NOT NULL,
  ContactLastName VARCHAR(50) NOT NULL,
  Available BOOLEAN NOT NULL,
  Quota INT NOT NULL CHECK (Quota >= 0),

  PRIMARY KEY (BIN)
);

CREATE TABLE Account (
  Email VARCHAR(100) CHECK (Email LIKE '%@%'),

  Password VARCHAR(255) NOT NULL,
  FirstName VARCHAR(50) NOT NULL CHECK (FirstName NOT LIKE '%[^a-zA-Z]%'),
  LastName VARCHAR(50) NOT NULL CHECK (LastName NOT LIKE '%[^a-zA-Z]%'),
  Address VARCHAR(255) NOT NULL,
  AttendeeType VARCHAR(50) NOT NULL CHECK (AttendeeType IN ('staff', 'student', 'alumni', 'guest')),
  AffOrg VARCHAR(100) NOT NULL,
  MobileNumber VARCHAR(8) CHECK (MobileNumber REGEXP '^[0-9]{8}$'),

  PRIMARY KEY (Email)
);

CREATE TABLE Meal (
  BIN INT,
  MIN INT,

  Name VARCHAR(100) NOT NULL,
  Type VARCHAR(50) NOT NULL CHECK (Type IN ('fish', 'chicken', 'beef', 'vegetarian')),
  Price DECIMAL(10, 2) NOT NULL CHECK (Price > 0),
  Cuisine VARCHAR(100) NOT NULL,

  PRIMARY KEY (BIN, MIN),
  FOREIGN KEY (BIN) REFERENCES Banquet(BIN)
);

CREATE TABLE Admission (
  Email VARCHAR(100),
  BIN INT,
  MIN INT,

  Drink VARCHAR(50) NOT NULL,
  Remark VARCHAR(255) NOT NULL,

  PRIMARY KEY (Email, BIN),
  FOREIGN KEY (Email) REFERENCES Account(Email),
  FOREIGN KEY (BIN, MIN) REFERENCES Meal(BIN, MIN)
);
