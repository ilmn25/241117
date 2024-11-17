
CREATE TABLE Banquet (
  BIN INT,

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
  MobileNumber INT CHECK (MobileNumber BETWEEN 0 AND 99999999), 

  PRIMARY KEY (Email)
);

CREATE TABLE Meal (
  BIN INT,
  MIN INT,
  
  DishName VARCHAR(100) NOT NULL,
  Type VARCHAR(50) NOT NULL,
  Price DECIMAL(10, 2) NOT NULL CHECK (Price > 0),
  Cusine VARCHAR(100) CHECK (Cusine IN ('fish', 'chicken', 'beef', 'vegetarian')),

  PRIMARY KEY (BIN, MIN),
  FOREIGN KEY (BIN) REFERENCES Banquet(BIN)
);

CREATE TABLE Admission (
  Email VARCHAR(100),
  BIN INT,
  MIN INT,

  Drink VARCHAR(50) NOT NULL,
  Remark VARCHAR(255),

  PRIMARY KEY (Email, BIN, MIN),
  FOREIGN KEY (Email) REFERENCES Account(Email),
  FOREIGN KEY (BIN, Drink, Remark) REFERENCES Banquet(BIN, Drink, Remark),
  FOREIGN KEY (BIN, MIN) REFERENCES Meal(BIN, MIN)
); 