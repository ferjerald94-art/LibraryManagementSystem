package library.app;

import library.model.Book;
import library.model.IssueRecord;
import library.service.LibraryService;

import java.util.List;
import java.util.Scanner;

public class LibraryManagementSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static final LibraryService library = new LibraryService();

    public static void main(String[] args) {
        System.out.println("==================================");
        System.out.println("     LIBRARY MANAGEMENT SYSTEM");
        System.out.println("==================================");
        seedBooks();

        boolean running = true;
        while (running) {
            showMenu();
            int choice = readInt("Choose an option: ", 0, 10);
            try {
                switch (choice) {
                    case 1: addBook(); break;
                    case 2: listBooks(); break;
                    case 3: searchBooks(); break;
                    case 4: requestIssue(); break;
                    case 5: librarianMenu(); break;
                    case 6: returnBook(); break;
                    case 7: calculateFine(); break;
                    case 8: showReminders(); break;
                    case 9: showReports(); break;
                    case 10: showAvailability(); break;
                    case 0: running = false; System.out.println("Goodbye."); break;
                    default: System.out.println("Invalid option.");
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
            System.out.println();
        }
        scanner.close();
    }

    private static void showMenu() {
        System.out.println("1. Add book");
        System.out.println("2. List all books");
        System.out.println("3. Search book");
        System.out.println("4. Request book issue");
        System.out.println("5. Librarian: approve/reject requests");
        System.out.println("6. Return book / collect fine");
        System.out.println("7. Calculate current fine");
        System.out.println("8. Due date reminders");
        System.out.println("9. Generate reports");
        System.out.println("10. Book availability");
        System.out.println("0. Exit");
    }

    private static void seedBooks() {
        library.addBook(new Book("B101", "Clean Code", "Robert C. Martin"));
        library.addBook(new Book("B102", "Effective Java", "Joshua Bloch"));
        library.addBook(new Book("B103", "Head First Java", "Kathy Sierra"));
    }

    private static void addBook() {
        String id = readNonEmpty("Book ID: ");
        String title = readNonEmpty("Title: ");
        String author = readNonEmpty("Author: ");
        library.addBook(new Book(id, title, author));
        System.out.println("Book added successfully.");
    }

    private static void listBooks() {
        List<Book> books = library.getBooks();
        if (books.isEmpty()) {
            System.out.println("No books in the catalogue.");
            return;
        }
        for (Book book : books) System.out.println(book);
    }

    private static void searchBooks() {
        String keyword = readNonEmpty("Enter book ID, title, or author: ");
        List<Book> results = library.searchBooks(keyword);
        if (results.isEmpty()) System.out.println("No matching books found.");
        else for (Book book : results) System.out.println(book);
    }

    private static void requestIssue() {
        String studentId = readNonEmpty("Student ID: ");
        String studentName = readNonEmpty("Student name: ");
        String bookId = readNonEmpty("Book ID to request: ");
        library.requestIssue(studentId, studentName, bookId);
        System.out.println("Issue request submitted. A librarian must approve it.");
    }

    private static void librarianMenu() {
        String pin = readNonEmpty("Enter librarian PIN: ");
        if (!library.getLibrarian().authenticate(pin)) {
            System.out.println("Incorrect PIN.");
            return;
        }
        List<String> requests = library.getPendingRequests();
        if (requests.isEmpty()) {
            System.out.println("No pending issue requests.");
            return;
        }
        System.out.println("Pending requests:");
        for (String request : requests) System.out.println(request);
        String bookId = readNonEmpty("Enter book ID to process (or X to cancel): ");
        if (bookId.equalsIgnoreCase("X")) return;
        String action = readNonEmpty("Approve or reject? (A/R): ");
        if (action.equalsIgnoreCase("A")) {
            IssueRecord record = library.approveIssue(bookId);
            System.out.println("Approved. Due date: " + record.getDueDate());
        } else if (action.equalsIgnoreCase("R")) {
            library.rejectIssue(bookId);
            System.out.println("Request rejected.");
        } else {
            System.out.println("Enter A to approve or R to reject. No action taken.");
        }
    }

    private static void returnBook() {
        String bookId = readNonEmpty("Book ID to return: ");
        long fine = library.returnBook(bookId);
        System.out.println("Book returned successfully.");
        System.out.println("Fine to collect: ₹" + fine);
        if (fine == 0) System.out.println("No fine is due.");
        else System.out.println("Fine collection recorded by completing the return.");
    }

    private static void calculateFine() {
        String bookId = readNonEmpty("Book ID: ");
        System.out.println("Current fine: ₹" + library.calculateCurrentFine(bookId));
        System.out.println("Fine rate: ₹" + library.getFinePerDay() + " per overdue day.");
    }

    private static void showReminders() {
        List<IssueRecord> due = library.getDueReminders();
        if (due.isEmpty()) {
            System.out.println("No books are due or overdue today.");
            return;
        }
        System.out.println("Due/overdue books:");
        for (IssueRecord record : due) {
            System.out.println(record + " | Overdue days: " + record.overdueDays()
                    + " | Current fine: ₹" + record.calculateFine(library.getFinePerDay()));
        }
    }

    private static void showReports() {
        List<Book> books = library.getBooks();
        int available = 0;
        for (Book book : books) if (book.isAvailable()) available++;
        System.out.println("========== LIBRARY REPORT ==========");
        System.out.println("Total books: " + books.size());
        System.out.println("Available: " + available);
        System.out.println("Issued: " + (books.size() - available));
        System.out.println("Issue records: " + library.getRecords().size());
        System.out.println("Pending requests: " + library.getPendingRequests().size());
        System.out.println("Outstanding fine on active loans: ₹" + library.getTotalFineOutstanding());
        System.out.println("====================================");
    }

    private static void showAvailability() {
        String id = readNonEmpty("Enter book ID: ");
        Book book = library.findBookById(id);
        if (book == null) System.out.println("Book ID not found.");
        else System.out.println(book.getTitle() + " is " + (book.isAvailable() ? "AVAILABLE" : "NOT AVAILABLE") + ".");
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be empty.");
        }
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) return number;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a whole number from " + min + " to " + max + ".");
        }
    }
}
