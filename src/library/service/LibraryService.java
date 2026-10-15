package library.service;

import library.model.Book;
import library.model.IssueRecord;
import library.model.Librarian;
import library.model.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryService {
    private static final int LOAN_DAYS = 14;
    private static final long FINE_PER_DAY = 5;

    private final List<Book> books = new ArrayList<>();
    private final Map<String, Book> issuedBooks = new HashMap<>();
    private final List<IssueRecord> records = new ArrayList<>();
    private final Map<String, IssueRequest> pendingRequests = new HashMap<>();
    private final Librarian librarian = new Librarian("1234");

    private static class IssueRequest {
        private final Student student;
        private final Book book;
        private IssueRequest(Student student, Book book) {
            this.student = student;
            this.book = book;
        }
    }

    public Librarian getLibrarian() { return librarian; }

    public void addBook(Book book) {
        if (findBookById(book.getId()) != null) {
            throw new IllegalArgumentException("A book with this ID already exists.");
        }
        books.add(book);
    }

    public Book findBookById(String id) {
        for (Book book : books) {
            if (book.getId().equalsIgnoreCase(id)) return book;
        }
        return null;
    }

    public List<Book> getBooks() { return new ArrayList<>(books); }

    public List<Book> searchBooks(String keyword) {
        List<Book> matches = new ArrayList<>();
        String query = keyword.toLowerCase();
        for (Book book : books) {
            if (book.getId().toLowerCase().contains(query)
                    || book.getTitle().toLowerCase().contains(query)
                    || book.getAuthor().toLowerCase().contains(query)) {
                matches.add(book);
            }
        }
        return matches;
    }

    public void requestIssue(String studentId, String studentName, String bookId) {
        if (studentId.trim().isEmpty() || studentName.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID and name are required.");
        }
        Book book = findBookById(bookId);
        if (book == null) throw new IllegalArgumentException("Book ID not found.");
        if (!book.isAvailable()) throw new IllegalArgumentException("Book is not available.");
        if (pendingRequests.containsKey(bookId.toLowerCase())) {
            throw new IllegalArgumentException("An issue request for this book is already pending.");
        }
        if (issuedBooks.containsKey(bookId.toLowerCase())) {
            throw new IllegalArgumentException("This book is already issued.");
        }
        Student student = new Student(studentId.trim(), studentName.trim());
        pendingRequests.put(bookId.toLowerCase(), new IssueRequest(student, book));
    }

    public List<String> getPendingRequests() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, IssueRequest> entry : pendingRequests.entrySet()) {
            IssueRequest request = entry.getValue();
            result.add("Book " + request.book.getId() + " - " + request.book.getTitle()
                    + " requested by " + request.student.getName()
                    + " (" + request.student.getStudentId() + ")");
        }
        return result;
    }

    public IssueRecord approveIssue(String bookId) {
        IssueRequest request = pendingRequests.remove(bookId.toLowerCase());
        if (request == null) throw new IllegalArgumentException("No pending request for that book ID.");
        if (!request.book.isAvailable()) {
            throw new IllegalArgumentException("Book is no longer available.");
        }
        request.book.setAvailable(false);
        issuedBooks.put(bookId.toLowerCase(), request.book);
        IssueRecord record = new IssueRecord(request.book, request.student, LocalDate.now(), LOAN_DAYS);
        records.add(record);
        return record;
    }

    public void rejectIssue(String bookId) {
        if (pendingRequests.remove(bookId.toLowerCase()) == null) {
            throw new IllegalArgumentException("No pending request for that book ID.");
        }
    }

    public long returnBook(String bookId) {
        String key = bookId.toLowerCase();
        Book book = issuedBooks.get(key);
        if (book == null) throw new IllegalArgumentException("That book is not currently issued.");
        IssueRecord activeRecord = null;
        for (int i = records.size() - 1; i >= 0; i--) {
            IssueRecord record = records.get(i);
            if (record.getBook().getId().equalsIgnoreCase(bookId) && !record.isReturned()) {
                activeRecord = record;
                break;
            }
        }
        if (activeRecord == null) throw new IllegalStateException("Active issue record was not found.");
        activeRecord.markReturned(LocalDate.now());
        long fine = activeRecord.calculateFine(FINE_PER_DAY);
        book.setAvailable(true);
        issuedBooks.remove(key);
        return fine;
    }

    public long calculateCurrentFine(String bookId) {
        for (int i = records.size() - 1; i >= 0; i--) {
            IssueRecord record = records.get(i);
            if (record.getBook().getId().equalsIgnoreCase(bookId) && !record.isReturned()) {
                return record.calculateFine(FINE_PER_DAY);
            }
        }
        throw new IllegalArgumentException("No active issue record for that book ID.");
    }

    public List<IssueRecord> getRecords() { return new ArrayList<>(records); }

    public List<IssueRecord> getDueReminders() {
        List<IssueRecord> due = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (IssueRecord record : records) {
            if (!record.isReturned() && !record.getDueDate().isAfter(today)) due.add(record);
        }
        return due;
    }

    public long getTotalFineOutstanding() {
        long total = 0;
        for (IssueRecord record : records) {
            if (!record.isReturned()) total += record.calculateFine(FINE_PER_DAY);
        }
        return total;
    }

    public long getFinePerDay() { return FINE_PER_DAY; }
    public int getLoanDays() { return LOAN_DAYS; }
}
