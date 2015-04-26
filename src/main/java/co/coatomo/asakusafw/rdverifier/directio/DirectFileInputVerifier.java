package co.coatomo.asakusafw.rdverifier.directio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.coatomo.asakusafw.rdverifier.Verifier;
import co.coatomo.asakusafw.rdverifier.VerifyException;
import co.coatomo.asakusafw.rdverifier.util.ReflectionUtils;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;

public class DirectFileInputVerifier implements Verifier<DirectFileInputDescription> {

	private static final Logger LOG = LoggerFactory.getLogger(DirectFileInputVerifier.class);

	@Override
	public <DM extends DataModel<DM>> void verify(File dataFile, DirectFileInputDescription dfid) {
		BinaryStreamFormat<DM> bsf = ReflectionUtils.getBinaryStreamFormat(dfid);
		Class<DM> modelType = ReflectionUtils.getDataModelClass(dfid);
		DM model = ReflectionUtils.getDataModel(modelType);

		try (ModelInput<DM> mi = bsf.createInput(modelType, dataFile.getCanonicalPath(), new FileInputStream(dataFile),
				0, dataFile.length())) {
			while (mi.readTo(model)) {
				LOG.debug("successfully read as a data model: {}", model);
			}
		} catch (IOException | InterruptedException e) {
			LOG.error("failed to read as a data model");
			throw new VerifyException(e.getMessage(), e);
		}
	}

}
