package models


case class CloudFormation(parameters: Parameters, resources: List[Resource])

case class Parameters(params: Map[String, String])

case class Resource(`type`: String, properties: Properties)

case class Properties(sseSpecification: SSESpecification,
                      provisionedThroughput: ProvisionedThroughput,
                      attributeDefinitions: AttributeDefinitions,
                      globalSecondaryIndexes: List[GlobalSecondaryIndex],
                      keySchema: KeySchema,
                      tableName: String
                     )
case class SSESpecification(sseEnabled: Boolean)
case class ProvisionedThroughput(readCapacityUnits: Int, writeCapacityUnits: Int)
case class AttributeDefinitions(attributeName: String, attributeType: String)
case class GlobalSecondaryIndex(indexName: String, keySchema: List[KeySchema], projection: Projection, provisionedThroughput: ProvisionedThroughput)
case class KeySchema(attributeName: String, keyType: String)
case class Projection(projectionType: String)
