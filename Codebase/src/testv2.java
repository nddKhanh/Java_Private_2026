import java.io.IOException;

public class testv2 {
    public void readV2() {
        test t = new test();

        try {
            t.read();
        } catch (IOException e) {
            System.out.println("Lỗi rồi con");
        }
    }
}
