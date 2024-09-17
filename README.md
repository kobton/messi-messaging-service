# Messi, the messaging service

This is a messaging service built to send and receive messages. The sender can specify his/her name and a message along with the recipient. The messaging service is build using Java Spring Boot and RabbitMQ message broker. The application has a REST API to send, receive and delete messages.

## Application structure and packages

- Controller: REST controller defining the endpoints
- Exception: custom exception handling for indata validation
- Service: service for sending messages with RabbitMQ
- Listener: service for receiving messages with RabbitMQ
- Model: model object defining a message
- Config: configuration of the RabbitMQ exchange, queue and binding

## Run application

The easiest method is to run the application with docker compose.

Build application:

```python
mvn clean install && mvn clean package
```

Run application with docker compose

```
docker compose up --build

docker compose up --down
```

The application can also be run with Maven using:

```
mvn clean install && mvn spring-boot run
```

If run using Maven to run the application, an instance of RabbitMQ needs to be run locally or in a docker container with exposed ports:

Run RabbitMQ with docker:

```docker
docker run -d --name my-rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management
```

When started the application API will be available at `http://localhost:8080/api/messages`

## Use application

Send a message:

```python
curl --request POST \
  --url http://localhost:8080/api/messages/send \
  --header 'Content-Type: application/json' \
  --data '	{
		"senderName": "Jakob",
        "recipientName": "Kajsa",
		"content": "Hello again"
	}'
```

Get all messages:

```python
curl --request GET \
  --url http://localhost:8080/api/messages/received
```

Get message for specific recipient:

```python
curl --request GET \
  --url 'http://localhost:8080/api/messages/received/recipient?name=Kajsa'
```

Get message by range

```python
curl --request GET \
  --url 'http://localhost:8080/api/messages/received/range?start=1&stop=3'
```

Delete a message by index:

```python
curl --request DELETE \
  --url http://localhost:8080/api/messages/delete/2
```

Delete messages in range:

```python
curl --request DELETE \
  --url 'http://localhost:8080/api/messages/delete/range?start=1&stop=2'
```

## Possible improvements

- Specifiy recipient in call to remove messages (with dto for requests)
- Specify recipient in call to get messages in range (with dto for requests)
- More unit tests
- Data persistence of recieved messages with a database
