#/bin/sh
# create user
curl -X POST -H "Content-Type:application/json; charset=UTF-8" -d '{"user": {
  "name": "Amazing",
  "sureName": "Horse Ö",
  "age": 30
}}' http://localhost:8080/User

# check if user really exists (if the db was empty before, it should have the id 1)
curl http://localhost:8080/User/1

# we want it in xml
curl -H "Accept:text/xml" http://localhost:8080/User/1

# update the users sure name
curl -X PUT -H "Content-Type:text/xml; charset=UTF-8" -d '<user>
  <id>1</id>
  <sureName>Hörse</sureName>
</user>' http://localhost:8080/User

# check if user really exists (this time we also check if the user does not exist twice)
curl http://localhost:8080/User

# show off search capabilities
curl http://localhost:8080/User/name/Amaz*

# ok - everything went well, now we delete the user
curl -X DELETE http://localhost:8080/User/1

# check if was really deleted
curl http://localhost:8080/User

# showing that extending default services works:
curl http://localhost:8080/User/allstars