package exercises04;

public class PersonTest {

    public static void main(String[] args) throws InterruptedException {

        Thread t1 = new Thread(() -> {
            Person p = new Person();
            p.setName("Alice");
            p.setAddress(1000, "Street A");

            System.out.println(
                "t1: id=" + p.getId()
                + ", name=" + p.getName()
                + ", zip=" + p.getZip()
                + ", address=" + p.getAddress()
            );
        });

        Thread t2 = new Thread(() -> {
            Person p = new Person();
            p.setName("Bob");
            p.setAddress(2000, "Street B");

            System.out.println(
                "t2: id=" + p.getId()
                + ", name=" + p.getName()
                + ", zip=" + p.getZip()
                + ", address=" + p.getAddress()
            );
        });

        Thread t3 = new Thread(() -> {
            Person p = new Person();
            p.setName("Charlie");
            p.setAddress(3000, "Street C");

            System.out.println(
                "t3: id=" + p.getId()
                + ", name=" + p.getName()
                + ", zip=" + p.getZip()
                + ", address=" + p.getAddress()
            );
        });

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();
    }
}