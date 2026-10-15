package library.model;

public class Librarian {
    private final String pin;

    public Librarian(String pin) {
        this.pin = pin;
    }

    public boolean authenticate(String enteredPin) {
        return pin.equals(enteredPin);
    }
}
