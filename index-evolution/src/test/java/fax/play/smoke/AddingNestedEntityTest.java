package fax.play.smoke;

import fax.play.model3.Schema3I;
import fax.play.model3.Schema3J;
import fax.play.service.CacheDefinition;
import fax.play.service.CacheProvider;
import fax.play.service.Model;
import org.infinispan.client.hotrod.RemoteCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static fax.play.service.CacheProvider.CACHE1_NAME;
import static fax.play.smoke.MinorsForNoDowntimeUpgradesTest.doQuery;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddingNestedEntityTest {

    private final CacheProvider cacheProvider = new CacheProvider();

    @Test
    void testAddIndexedNestedEntity() {
        RemoteCache<String, Model> cache = cacheProvider
                .init(new CacheDefinition(CACHE1_NAME, "Model3"), Schema3I.INSTANCE)
                .getCache(CACHE1_NAME);

        ModelUtils.createModel1Entities(cache, 5, ModelUtils.createModelI(1));

        doQuery("FROM Model3 WHERE name LIKE '%3%'", cache, 1);

        // simulate adding new entity with nested indexed fields at runtime
        // this will update schema on the server and the local context
        cacheProvider.updateIndexSchema(cache, Schema3J.INSTANCE);

        // Create second version entities
        ModelUtils.createModel1Entities(cache, 5, ModelUtils.createModelJ(1));

        // non-indexed query - all good
        doQuery("FROM Model3 WHERE id = 800000", cache, 1);

        // non-analyzed indexed query - all good
        doQuery("FROM Model3 WHERE name LIKE '%3%'", cache, 2);

        // try to query nested entity fields; we need to use an alias
        doQuery("FROM Model3 m WHERE m.nestedModel.name LIKE '%Model3J%'", cache, 5);

        doQuery("FROM Model3 m WHERE m.nestedModel.nameAnalyzed : '*Model3J*'", cache, 5);
    }
}
