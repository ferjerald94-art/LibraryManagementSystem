# LibraryManagementSystem

A basic Core Java console-based Library Management System.

## Project structure

```text
LibraryManagementSystem/
├── README.md
└── src/
    └── library/
        ├── app/
        │   └── LibraryManagementSystem.java
        ├── model/
        │   ├── Book.java
        │   ├── IssueRecord.java
        │   ├── Librarian.java
        │   └── Student.java
        └── service/
            └── LibraryService.java
```

## Compile

Run these commands from the `LibraryManagementSystem` directory:

```bash
javac -d out src/library/model/*.java src/library/service/*.java src/library/app/*.java
```

## Run

```bash
java -cp out library.app.LibraryManagementSystem
```

## Default librarian PIN

`1234`
