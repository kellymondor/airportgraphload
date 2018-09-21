package datastax
import constants.Edge._
import constants.Property._
import constants.Vertex._

import com.datastax.bdp.graph.spark.graphframe._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object GraphLoader {
  def main(args: Array[String]):Unit = {

    val graphName = "test"
    val airportNodesFileName = "airportNodes.json"
    val airportEdgesFileName = "airportEdges.json"
    val seed = 1

    val spark = SparkSession
      .builder
      .appName("Airport Data Load")
      .enableHiveSupport()
      .getOrCreate()

    val r = new java.util.Random()
    r.setSeed(seed)

    val g = spark.dseGraph(graphName)
    val airports = spark.read.json(airportNodesFileName)
    val routes = spark.read.json(airportEdgesFileName)

    val airportVertices = airports
      .select(
        col("id") as AirportCode,
        col("title") as AirportName,
        col("id") as PartitionKey,
        concat(lit("POINT ("), col("longitude"), lit(" "),
          col("latitude"), lit(")")) as Coordinates,
        lit(r.nextInt(1000)) as Fare,
        lit(r.nextInt(5)) as Risk)
      .withColumn(Label, lit(AirportVertex))

    g.updateVertices(airportVertices)

    val routeEdges = routes
      .withColumn("routes", explode(col("routes")))
      .select(
        col("id") as "srcAirport",
        col("routes.id") as "dstAirport",
        col("routes.dist") as Distance)
      .withColumn("srcLabel", lit(AirportVertex))
      .withColumn("dstLabel", lit(AirportVertex))
      .withColumn("edgeLabel", lit(HasRouteEdge))
      .select(
        g.idColumn(col("srcLabel"), col("srcAirport")) as "src",
        g.idColumn(col("dstLabel"), col("dstAirport")) as "dst",
        col("edgeLabel") as Label,
        col(Distance))

    g.updateEdges(routeEdges)

    spark.stop()

    System.exit(0)

  }
}
