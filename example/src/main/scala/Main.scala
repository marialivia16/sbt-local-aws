import java.net.URI

import aws.{DynamoDb, S3}
import models.{ConcertInfo, Dev}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

object Main extends App {
  val s3Client = new S3(Dev).client
  val dynamoDbClient = new DynamoDb(Dev).client

  S3.setConcertPrices("concert1", Map("standing" -> 50))(s3Client)
  val allPrices = S3.getAllConcertPrices(s3Client)

  println(allPrices)

//  DynamoDb.writeConcert(ConcertInfo("artist1", "concert1", 120))(dynamoDbClient)
//  DynamoDb.getAllConcertInfos(dynamoDbClient)

  S3Client.builder().endpointOverride(URI.create("http://localhost:4572")).build()
  val request = PutObjectRequest.builder().bucket("Config").key("application").build()
  client.putObject(request, RequestBody.fromString("data"))
}
