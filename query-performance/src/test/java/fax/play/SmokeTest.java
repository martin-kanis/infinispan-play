package fax.play;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.rest.RestClient;
import org.infinispan.client.rest.RestResponse;
import org.infinispan.client.rest.configuration.RestClientConfigurationBuilder;
import org.infinispan.query.dsl.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fax.play.model.Shape;

public class SmokeTest {

   private Config config;
   private RestClient restClient;

   @BeforeEach
   public void start() {
      config = new Config();
      restClient = RestClient.forConfiguration(new RestClientConfigurationBuilder().addServer()
            .host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
                  .security()
                  .authentication()
                  .username("user")
                  .password("pass")
            .build());
   }

   @AfterEach
   public void end() throws Exception {
      if (restClient != null) {
         restClient.close();
      }
      if (config != null) {
         config.shutdown();
      }
   }

   @Test
   public void test() throws Exception {
      RemoteCache<Object, Shape> cache = config.getShapeCache();

      cache.put("circle", new Shape("circle", 7, Integer.MAX_VALUE, "A circle is a shape consisting ... bla bla bla"));
      cache.put("square", new Shape("square", 9, 4, "In Euclidean geometry, a square is a regular quadrilateral ... bla bla bla"));
      cache.put("triangle", new Shape("triangle", 29, 3, "In Euclidean geometry, any three points, when non-collinear ... bla bla bla"));

      Query<Long[]> query = Search.getQueryFactory(cache).create("select count(s.name) from shape s");
      List<Long[]> result = query.execute().list();

      assertThat(result).containsExactly(new Long[]{3L});

      RestResponse restResponse = restClient.cache(cache.getName()).searchStats().toCompletableFuture().get();
      assertThat(restResponse).isNotNull();
      String body = restResponse.getBody();
      assertThat(body).isNotNull();

      QueryStatistics queryStatistics = new QueryStatistics(body);
      assertThat(queryStatistics).isNotNull();

      // aggregations always produces hybrid queries
      assertThat(queryStatistics.getHybridCount()).isPositive();
   }
}