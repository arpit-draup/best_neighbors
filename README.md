#Best Neighbors

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/procedures-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/procedures-1.0-SNAPSHOT.jar neo4j-enterprise-3.5.0/plugins/.
    

Restart your Neo4j Server. A new Stored Procedure is available:

    com.maxdemarzi.best_neighbors(Node, max neighbors to return, max depth to traverse,direction)
    
    Direction : 
            Outgoing: 0
            Incoming: 1
    
    MATCH (n:job {name:'Marice'}) WITH n
    CALL com.maxdemarzi.best_neighbors(n, 50, 4,0) yield nodes, cost
    RETURN [x in nodes| x.name], cost
