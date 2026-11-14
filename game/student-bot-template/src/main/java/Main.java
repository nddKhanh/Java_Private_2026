import com.antcolonywars.sdk.Bot;

public final class Main {
    public static void main(Bot bot) {
        var state = bot.state().snapshot();
        System.out.println("Revision đầu batch: " + state.revision());

        bot.select("worker-blue");
        bot.go(2);
        bot.idle();
    }
}
