## Example project without plugin

1. Install Docker, Docker Compose, and AWS CLI

2. Spin up docker container:

- `sbt dockerComposeUp` (when using the sbt-docker-compose plugin)

- Or `docker-compose -d up`

3. Create the needed resources for running the app:

- `brew install awscli` (if it's not installed already)

- `aws s3api --endpoint-url=http://localhost:4572 create-bucket --bucket PriceBandsBucket`

- `aws --endpoint-url=http://localhost:4569 dynamodb create-table --table-name ConcertTickets-SalesTable-DEV --attribute-definitions AttributeName=ArtistId,AttributeType=S AttributeName=ConcertId,AttributeType=S AttributeName=TicketSales,AttributeType=N --key-schema AttributeName=ArtistId,KeyType=HASH AttributeName=ConcertId,KeyType=RANGE --global-secondary-indexes IndexName=GSI,KeySchema=[{AttributeName=TicketSales,KeyType=HASH}],Projection={ProjectionType=KEYS_ONLY},ProvisionedThroughput={ReadCapacityUnits=5,WriteCapacityUnits=5} --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5`

4. Start application: `sbt run`