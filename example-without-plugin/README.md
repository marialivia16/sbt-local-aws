## Example project without plugin

1. Install Docker, Docker Compose, and AWS CLI

2. Spin up docker container:

- `sbt dockerComposeUp` (when using the sbt-docker-compose plugin)

- Or `docker-compose -d up`

3. Create the needed resources for running the app:

- `brew install awscli` (if it's not installed already)

- `aws s3api --endpoint-url=http://localhost:4572 create-bucket --bucket PriceBandsBucket`

- TODO: add command for dynamodb

4. Start application: `sbt run`