package fax.play.model4;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(includeClasses = Model4A.class, schemaFileName = "model4-schema.proto")
public interface Schema4A extends GeneratedSchema {

    fax.play.model4.Schema4A INSTANCE = new Schema4AImpl();

}
