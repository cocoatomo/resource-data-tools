package co.coatomo.asakusafw.rdverifier;

import java.io.File;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.vocabulary.external.ImporterDescription;

public interface Verifier<ID extends ImporterDescription> {

	public <DM extends DataModel<DM>> void verify(File dataFile, ID importerDescription);
}
