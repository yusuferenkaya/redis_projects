import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import java.util.Scanner;

public class RedisChatApp {

    public static void main(String[] args) {
        Jedis publisherJedis = new Jedis("localhost", 6379);
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter chat room name: ");
        String room = scanner.nextLine();

        Thread subscriberThread = new Thread(() -> {
            Jedis subscriberJedis = new Jedis("localhost", 6379);

            JedisPubSub jedisPubSub = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (!message.startsWith(username + ":")) {
                        System.out.println(message);
                    }
                }
            };

            System.out.println("Joined chat room '" + room + "'. Waiting for messages...\n");
            subscriberJedis.subscribe(jedisPubSub, room);
        });

        subscriberThread.start();

        while (true) {
            System.out.print("[" + username + "@" + room + "]> ");
            String message = scanner.nextLine();
            if (message.equalsIgnoreCase("exit")) {
                System.out.println("Exiting chat room '" + room + "'...");
                break;
            }

            publisherJedis.publish(room, username + ": " + message);
        }

        publisherJedis.close();
        scanner.close();
    }
}