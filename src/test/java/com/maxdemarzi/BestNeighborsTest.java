package com.maxdemarzi;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.junit.Neo4jRule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

public class BestNeighborsTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            // This is the Procedure we want to test
            .withProcedure( Procedures.class )
            .withFixture(MODEL_STATEMENT);


    @Test
    public void shouldFindNeighbors() throws Throwable
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "MATCH (n:Node {name:$name}) WITH n\n" +
                    "CALL com.maxdemarzi.best_neighbors(n, 25, 5,1) yield nodes, cost\n" +
                    "RETURN [x in nodes| x.name], cost", parameters( "name", '1' ) );

            // Then I should get what I expect
            assertThat( result.list().size(), equalTo(0) );
        }
    }

    private static final String MODEL_STATEMENT =
            "CREATE (n1:Node { name:'1' })" +
                    "CREATE (n2:Node { name:'2' })" +
                    "CREATE (n3:Node { name:'3' })" +
                    "CREATE (n4:Node { name:'4' })" +
                    "CREATE (n5:Node { name:'5' })" +
                    "CREATE (n6:Node { name:'6' })" +
                    "CREATE (n1)-[:to {count: 0.2}]->(n3)" +
                    "CREATE (n2)-[:to {count: 0.3}]->(n3)" +
                    "CREATE (n3)-[:to {count: 0.2}]->(n4)" +
                    "CREATE (n4)-[:to {count: 0.6}]->(n5)" +
                    "CREATE (n4)-[:to {count: 0.5}]->(n6)";


}
