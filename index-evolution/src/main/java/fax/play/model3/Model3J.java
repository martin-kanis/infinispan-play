package fax.play.model3;

import fax.play.service.Model;
import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoDoc("@Indexed")
public class Model3J implements Model {

    @ProtoField(number = 1)
    @ProtoDoc("@Field(index = Index.YES, store = Store.YES)")
    public Integer entityVersion;

    @ProtoField(number = 2)
    public String id;

    @ProtoField(number = 3)
    @ProtoDoc("@Field(index = Index.YES, store = Store.YES, analyze = Analyze.YES, analyzer = @Analyzer(definition = \"lowercase\"))")
    public String name;

    @Override
    public String getId() {
        return id;
    }
}
