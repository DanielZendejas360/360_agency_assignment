#Listings API (360.Agency Backend assignment)

##Summary
RESTful service in charge of managing vehicle dealers and listings.

It's main duties are to create, update, publish and unpublish listings. Also,
it can return all listings of a vehicle dealer with a given state. It contains
business logic to prevent a vehicle dealer to surpass their tier limit.

Currently, functionality related to dealers is limited to creation and updates.

##Requirements
- Maven 3
- Java 11 or above
- Docker for integration tests. The project uses [TestContainers](https://www.testcontainers.org/)

##How to start
###With Docker
1. Execute `mvn clean install` in the root of the project. This will result in
   a JAR file under the `target/` folder.
2. Execute `docker-compose up --build`. As can be seen in the Dockerfile, the
   JAR from the previous step will be copied and executed, starting the
   application.

###Without Docker
1. Execute `mvn clean install` in the root of the project. This will result in
   a JAR file under the `target/` folder.
2. Start a Postgres instance with [pg_ctl](https://www.postgresql.org/docs/10/app-pg-ctl.html)
   ```
   pg_ctl -D {datadir} start
   ```
3. Start the service with the following command:
   ```
   java -jar target/Listings-0.0.1-SNAPSHOT.jar
   ```

##Usage
The following commands execute a series of requests to the service. It is
designed to touch on all functionalities the service.

Refer to the section "How to start" for instructions on how to get the
service up and running
```
# Let's start by creating a vehicle dealer
curl http://localhost:8080/api/v1/dealers -H 'Content-Type:application/json' -d'{"name": "Test Dealer", "tierLimit": 3}'

# The response will be a JSON map containing a field "id". Let's store it in a variable for future requests
export VD_ID=...

# Let's create four listings for the vehicle dealer.
# No worries about the tier limit since all listings are created unpublished by default.
curl -XPOST http://localhost:8080/api/v1/dealers/$VD_ID/listings -H 'Content-Type:application/json' -d '{"vehicle": "Test Vehicle", "price": 100000}'
curl -XPOST http://localhost:8080/api/v1/dealers/$VD_ID/listings -H 'Content-Type:application/json' -d '{"vehicle": "Test Vehicle", "price": 100000}'
curl -XPOST http://localhost:8080/api/v1/dealers/$VD_ID/listings -H 'Content-Type:application/json' -d '{"vehicle": "Test Vehicle", "price": 100000}'
# You can even try to create a listing in "published" state - you'll find that the state is overriden with "draft".
curl -XPOST http://localhost:8080/api/v1/dealers/$VD_ID/listings -H 'Content-Type:application/json' -d '{"vehicle": "Test Vehicle", "price": 100000, "state": "published"}'

# Now let's update the price of a listing.
# Copy the id of any listing created and put it at the end of the path of the request.
# Currently the service only supports PUT requests, meaning we have to provide the whole body.
curl -XPUT http://localhost:8080/api/v1/dealers/$VD_ID/listings/paste_id_here -H 'Content-Type:application/json' -d '{"vehicle": "Test Vehicle", "price": 150000}'

# Let's proceed by publishing three of the listings above - the maximum allowed given the tier limit.
# Copy the ids of three listings (whichever) and paste them on the paths of the following requests.
# As you may suspect, the query parameter "tierLimitHandling" allows to select the behavior when the tier limit is reached.
# For now it is not relevant, since we don't plan on reaching the limit yet.
curl -XPOST http://localhost:8080/api/v1/dealers/$VD_ID/listings/paste_id1_here/publish?tierLimitHandling=error
curl -XPOST http://localhost:8080/api/v1/dealers/$VD_ID/listings/paste_id2_here/publish?tierLimitHandling=error
curl -XPOST http://localhost:8080/api/v1/dealers/$VD_ID/listings/paste_id3_here/publish?tierLimitHandling=error

# Let's fetch the listings.
# The query parameter "state" allows "draft" and "published".
curl http://localhost:8080/api/v1/dealers//listings?state=draft
curl http://localhost:8080/api/v1/dealers//listings?state=published

# Let's try to exceed the tier limit.
# This will showcase the two ways this service handles that case.
# Copy the id of the last unpublished listing and paste it in the path of the following requests.
# If "error", then you will receive a 400 stating that the tier limit has been reached.
curl -XPOST http://localhost:8080/api/v1/dealers/$VD_ID/listings/paste_id4_here/publish?tierLimitHandling=error

# If "replaceOldest", then you'll get a 200 and you'll find the new listing got published and the oldest listing got unpublished.
# Try fetching listings as before to confirm the results.
curl -XPOST http://localhost:8080/api/v1/dealers/$VD_ID/listings/paste_id4_here/publish?tierLimitHandling=replaceOldest
```

##API Reference
###Entities

###Endpoints

