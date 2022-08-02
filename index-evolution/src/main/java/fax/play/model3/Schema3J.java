package fax.play.model3;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(includeClasses = { Model3J.class, NestedModel.class }, schemaFileName = "model3-schema.proto")
public interface Schema3J extends GeneratedSchema {

    Schema3J INSTANCE = new Schema3JImpl();

}