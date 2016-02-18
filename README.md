# DatabaseDriverSupport

Testing feature of database drivers

Setup databases
`docker-compose stop && docker-compose rm && docker-compose run --rm setup`

Run tests
`docker-compose run --rm sbt sbt test`
