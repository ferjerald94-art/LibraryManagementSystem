package library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class IssueRecord {
    private final Book book;
    private final Student student;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private final LocalDateTime issuedAt;
    private LocalDate returnDate;
    private LocalDateTime returnedAt;

    public IssueRecord(Book book, Student student, LocalDate issueDate, int loanDays) {
        this.book = book;
        this.student = student;
        this.issueDate = issueDate;
        this.dueDate = issueDate.plusDays(loanDays);
        this.issuedAt = LocalDateTime.now();
    }

    public Book getBook() { return book; }
    public Student getStudent() { return student; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public boolean isReturned() { return returnDate != null; }

    public void markReturned(LocalDate date) {
        this.returnDate = date;
        this.returnedAt = LocalDateTime.now();
    }

    public long overdueDays() {
        LocalDate endDate = isReturned() ? returnDate : LocalDate.now();
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, endDate));
    }

    public long calculateFine(long finePerDay) {
        return overdueDays() * finePerDay;
    }

    @Override
    public String toString() {
        String status = isReturned() ? "Returned on " + returnDate : "Active";
        return String.format("Book %s (%s) | Student %s (%s) | Issued %s | Due %s | %s",
                book.getId(), book.getTitle(), student.getStudentId(), student.getName(),
                issueDate, dueDate, status);
    }
}
