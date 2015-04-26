package co.coatomo.asakusafw.dmverifier;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	/**
	 *
	 * @param args
	 *            - Arguments passed from the command line
	 **/
	public static void main(String[] args) {
		LOG.info("Main");
		Main mainObject = new Main();
		try {
			// read the file path and the FQDN of ItemInfoFromCsv class from
			// a configuration file or runtime arguments
			mainObject.execute(new File("./src/test/resources/data.csv"),
					"com.example.jobflow.ItemInfoFromCsv");
		} catch (Throwable th) {
			LOG.error("error", th);
		} finally {
			LOG.info("final");
		}
	}

	public <T extends DataModel<T>> void execute(File dataFile,
			String descriptorClassName) throws NoSuchMethodException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, IOException, InterruptedException,
			ClassNotFoundException {

		@SuppressWarnings("unchecked")
		Class<? extends DirectFileInputDescription> descriptionClass = (Class<? extends DirectFileInputDescription>) Class
				.forName(descriptorClassName);
		Constructor<? extends DirectFileInputDescription> descriptionConstructor = descriptionClass
				.getConstructor();
		DirectFileInputDescription dfid = descriptionConstructor.newInstance();

		@SuppressWarnings("unchecked")
		Class<BinaryStreamFormat<T>> dfClass = (Class<BinaryStreamFormat<T>>) dfid
				.getFormat();
		Constructor<BinaryStreamFormat<T>> constructor = dfClass
				.getConstructor();
		BinaryStreamFormat<T> bsf = constructor.newInstance();

		@SuppressWarnings("unchecked")
		Class<T> modelType = (Class<T>) dfid.getModelType();
		Constructor<T> modelConstructor = modelType.getConstructor();
		T model = (T) modelConstructor.newInstance();

		try (ModelInput<T> mi = bsf.createInput(modelType, dataFile
				.getCanonicalPath(), new ReaderInputStream(new FileReader(
				dataFile)), 0, dataFile.length())) {
			while (mi.readTo(model)) {
				System.out.println(model);
			}
		} catch (IOException e) {
			LOG.error("[ERROR]", e);
		}
	}
}
