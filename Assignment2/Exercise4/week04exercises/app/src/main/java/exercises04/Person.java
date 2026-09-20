package exercises04;

public class Person {

    private static long nextId = 0;

    private final long id;
    private String name;
    private int zip;
    private String address;

    public Person() {
        synchronized(Person.class) {
            id = nextId;
            nextId++;
        }
    }

    public Person(long initialId) {
        synchronized(Person.class) {
            if (nextId == 0) {
                nextId = initialId;
            }

            id = nextId;
            nextId++;
        }
    }

    public long getId() {
        return id;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized int getZip() {
        return zip;
    }

    public synchronized String getAddress() {
        return address;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized void setAddress(int zip, String address) {
        this.zip = zip;
        this.address = address;
    }
}