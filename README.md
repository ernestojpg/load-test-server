# Load Test Server

## Summary ##

This project implements an utility mock server to be used in Load Tests.
It implements some HTTP endpoints to return custom responses, and with custom delays.

This server could be quite useful for testing load balancers or reverse proxies such as
Nginx, Varnish, or HAProxy, where we need to configure one or many backend servers to
forward the requests.

The server is implemented in Java using [Vert.x](https://vertx.io/), an asynchronous
and non-blocking library, so it should be able to handle thousands of simultaneous
connections without any problem.

## Implemented endpoints ##

Below we will describe the API provided by the Load Test Server.

### `GET:/health` ###

Basic Health Check endpoint that just returns `200 OK`, with the text "OK".

Example Request:
```
curl -v http://localhost:8080/health
```

Example Response:
```
< HTTP/1.1 200 OK
< content-type: text/plain
< content-length: 2
<
OK
```

### `POST:/ping` ###

Basic endpoint with a "ping-pong" functionality. It just returns `200 OK` with the same
body and `Content-Type` received in the request.

Example Request:
```
curl http://localhost:8080/ping -v \
     -H 'Content-Type: text/plain' \
     -d 'Hello world!'
```

Example Response:
```
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< content-type: text/plain
< delay: 0
<
Hello world!
```

We can pass optional headers in the request for changing the default behaviour of
the application.

* `delay: <delay>`: This header will force the server to delay/sleep for `<delay>` milliseconds
  before returning the response.
* `random-delay: [<minDelay>,]<maxDelay>`: This header will force the server to delay/sleep
  for a random period of time, between `<minDelay>` and `<maxDelay>` milliseconds, both inclusive.
  `<minDelay>` is optional, and it defaults to 0.
* `Accept-Encoding: gzip | deflate`: This header will force the server to return the response
  compressed with [gzip](https://en.wikipedia.org/wiki/Gzip) or [DEFLATE](https://en.wikipedia.org/wiki/DEFLATE)
  respectively.

Example Request, asking for a compressed response with a random delay between 500ms and 2000ms:
```
curl http://localhost:8080/ping -v \
     -H 'Content-Type: application/json' \
     -H 'random-delay: 500,2000' \
     -H 'Accept-Encoding: gzip' \
     -d '{"message": "Hello world!"}' \
     --compressed
```

Example Response:
```
< HTTP/1.1 200 OK
< content-type: application/json
< delay: 703
< content-encoding: gzip
< transfer-encoding: chunked
<
{"message": "Hello world!"}
```

### `GET:/data` and  `POST:/data` ###

`GET:/data` and `POST:/data` are generic endpoints that return random responses of
a given size/length. In the `POST` version, the body passed in the request is ignored.

Example Request:
```
curl http://localhost:8080/data -v
```

Example Response:
```
< HTTP/1.1 200 OK
< content-type: text/plain
< delay: 0
< data-length: 1024
< content-length: 1024
<
f4~MXvvyGw+u(*?CE**,+;}r1&k#fJCUyifH...
```

As you can see in the example, by default the server returns 1024 random characters
(`data-length: 1024`) with no delays (`delay: 0`). We can change this default behaviour
passing the following headers in the request:

* `delay: <delay>`: This header will force the server to delay/sleep for `<delay>` milliseconds
  before returning the response.
* `random-delay: [<minDelay>,]<maxDelay>`: This header will force the server to delay/sleep
  for a random period of time, between `<minDelay>` and `<maxDelay>` milliseconds, both inclusive.
  `<minDelay>` is optional, and it defaults to 0.
* `Accept-Encoding: gzip | deflate`: This header will force the server to return the response
  compressed with [gzip](https://en.wikipedia.org/wiki/Gzip) or [DEFLATE](https://en.wikipedia.org/wiki/DEFLATE)
  respectively.
* `data-length: <length>`: This header indicates the desired length of the response's body, so the
  server will reply with `<length>` random characters.
* `random-data-length: [<minLength>,]<maxLength>`: This header indicates that we want a respose
  with a random length between `<minLength>` and `<maxLength>`, both inclusive.
  `<minLength>` is optional, and it defaults to 0.

Example Request, asking for a compressed response with a length between 2000 and 5000 random characters,
and with a delay of maximum 5000ms.
```
curl http://localhost:8080/data -v \
     -H 'random-data-length: 2000,5000' \
     -H 'random-delay: 5000' \
     -H 'Accept-Encoding: gzip' \
     --compressed
```

Example Response:
```
< HTTP/1.1 200 OK
< content-type: text/plain
< delay: 3890
< data-length: 3240
< content-encoding: gzip
< transfer-encoding: chunked
<
@l?}|:"|](I]AY1T>{G_...
```

## Build ##

Build with Java 8 and Maven 3+.

```
mvn clean install
```

This will generate a "fat JAR" file, containing the application and all its dependencies.

This will also build a docker image locally.

### Running the application ###

Once the application is built, we just need to execute the generated fat JAR file:

Example:  
```
java -jar target/load-test-server-1.0-SNAPSHOT-app.jar
```
