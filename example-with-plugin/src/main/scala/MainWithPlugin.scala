import aws.{DynamoDb, S3}
import models.{ConcertInfo, Dev}

object MainWithPlugin extends App {
  val s3Client = new S3(Dev).client
  val dynamoDbClient = new DynamoDb(Dev).client

//  S3.setConcertPrices("concert1", Map("standing" -> 50))(s3Client)
//  val allPrices = S3.getAllConcertPrices(s3Client)
//
//  println(allPrices)

  DynamoDb.writeConcert(ConcertInfo("artist1", "concert1", 120))(dynamoDbClient)
  val concertInfos = DynamoDb.getAllConcertInfos(dynamoDbClient)

  println(concertInfos)
}
