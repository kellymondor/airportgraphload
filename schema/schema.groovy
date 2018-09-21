// create the graph, if not already created
system.graph('test').ifNotExists().create()
:remote config alias g fraud.g

// set the graph to development mode
schema.config().option('graph.schema_mode').set('Development')

// create property keys for airport vertex
schema.propertyKey("airportCode").Text().single().ifNotExists().create()
schema.propertyKey("airportName").Text().single().ifNotExists().create()
schema.propertyKey("coordinates").Point().withGeoBounds().ifNotExists().create()
schema.propertyKey("distance").Decimal().ifNotExists().create()
schema.propertyKey("rate").Decimal().ifNotExists().create()
schema.propertyKey("risk").Integer().ifNotExists().create()

// create airport vertex
schema.vertexLabel("airport").partitionKey("airportCode").properties("airportCode", "airportName", "coordinates").ifNotExists().create()

// create route edge
schema.edgeLabel("hasRoute").connection("airport", "airport").ifNotExists().create()
