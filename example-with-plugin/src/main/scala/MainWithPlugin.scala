/*
 * Copyright 2019+ sbt-aws-local contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import aws.{DynamoDb, S3}
import models.{ConcertInfo, Dev}

object MainWithPlugin extends App {
  val s3Client = new S3(Dev).client
  val dynamoDbClient = new DynamoDb(Dev).client

  S3.setConcertPrices("concert1", Map("standing" -> 50))(s3Client)
  val allPrices = S3.getAllConcertPrices(s3Client)

  DynamoDb.writeConcert(ConcertInfo("artist1", "concert1", 120))(dynamoDbClient)
  val concertInfos = DynamoDb.getAllConcertInfos(dynamoDbClient)

}
