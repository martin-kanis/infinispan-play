package fax.play.smoke;

import fax.play.model3.Schema3A;
import fax.play.model4.Schema4A;
import fax.play.service.CacheDefinition;
import fax.play.service.CacheProvider;
import fax.play.service.Model;
import org.infinispan.client.hotrod.RemoteCache;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static fax.play.service.CacheProvider.CACHE1_NAME;
import static fax.play.smoke.MinorsForNoDowntimeUpgradesTest.doQuery;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpdateIndexedEntitiesTest {

    private final CacheProvider cacheProvider = new CacheProvider();

    @Test
    void testAddIndexedEntityWhenCacheAlreadyExistsUpdateIndexSchema() {
        RemoteCache<String, Model> cache = cacheProvider
                .init(new CacheDefinition(CACHE1_NAME, "Model3"), Schema3A.INSTANCE)
                .getCache(CACHE1_NAME);

        ModelUtils.createModel1Entities(cache, 5, ModelUtils.createModelA(1));

        doQuery("FROM Model3 WHERE name LIKE '%3%'", cache, 1);

        // simulate adding new entity with indexed fields at runtime
        // this will update schema on the server and the local context
        cacheProvider.updateIndexSchema(cache, Schema4A.INSTANCE);

        //cacheProvider.initWithoutDelete(new CacheDefinition(CACHE1_NAME, "Model3</indexed-entity> <indexed-entity>Model4"), Schema3A.INSTANCE, Schema4A.INSTANCE);

        // Create second version entities
        ModelUtils.createModel1Entities(cache, 5, ModelUtils.createModel4A(1));

        // non-indexed query - all good
        doQuery("FROM Model4 WHERE id = 700000", cache, 1);

        // indexed query - ISPN005003: Exception reported org.hibernate.search.util.common.SearchException: ISPN014505: Unknown entity name: 'Model4'.
        doQuery("FROM Model4 WHERE name LIKE '%4A # 3%'", cache, 1);
    }

    @Test
    void testAddIndexedEntityWhenCacheAlreadyExistsRecreateCacheWithoutDelete() {
        RemoteCache<String, Model> cache = cacheProvider
                .init(new CacheDefinition(CACHE1_NAME, "Model3"), Schema3A.INSTANCE)
                .getCache(CACHE1_NAME);

        ModelUtils.createModel1Entities(cache, 5, ModelUtils.createModelA(1));

        doQuery("FROM Model3 WHERE name LIKE '%3%'", cache, 1);

        // try to recreate the cache with both indexed entities in the definition (sorry for the hack)
        // ISPN005003: Exception reported org.infinispan.commons.CacheConfigurationException: ISPN000507: Cache keyword1 already exists
        cacheProvider.initWithoutDelete(new CacheDefinition(CACHE1_NAME, "Model3</indexed-entity> <indexed-entity>Model4"), Schema3A.INSTANCE, Schema4A.INSTANCE);

        // Create second version entities
        ModelUtils.createModel1Entities(cache, 5, ModelUtils.createModel4A(1));

        // non-indexed query
        doQuery("FROM Model4 WHERE id = 700000", cache, 1);

        // indexed query
        doQuery("FROM Model4 WHERE name LIKE '%4A # 3%'", cache, 1);
    }

    @AfterAll
    public void afterAll() {
        cacheProvider.stop();
    }
}
