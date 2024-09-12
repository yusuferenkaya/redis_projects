import redis
import threading

client = redis.StrictRedis(host='localhost', port=6379, decode_responses=True)


def listen_for_messages(room, username):
    pubsub = client.pubsub()
    pubsub.subscribe(room)

    print(f"Joined chat room '{room}'. Waiting for messages...\n")

    for message in pubsub.listen():
        if message['type'] == 'message':
            if not message['data'].startswith(f"{username}:"):
                print(f"{message['data']}")


def send_message(room, username):
    while True:
        message = input(f"[{username}@{room}]> ")
        if message.lower() == 'exit':
            print(f"Exiting chat room '{room}'...")
            break
        client.publish(room, f"{username}: {message}")


def menu():
    username = input("Enter your username: ")
    room = input("Enter chat room name: ")

    listener_thread = threading.Thread(target=listen_for_messages, args=(room, username))
    listener_thread.daemon = True
    listener_thread.start()

    send_message(room, username)


if __name__ == '__main__':
    menu()