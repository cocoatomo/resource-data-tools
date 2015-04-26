package co.coatomo.asakusafw.rdverifier.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;

public class ReflectionUtils {

	private static final Logger LOG = LoggerFactory.getLogger(ReflectionUtils.class);

	public static class ReflectionException extends RuntimeException {

		private static final long serialVersionUID = -3336947260595142029L;

		public ReflectionException(String message) {
			super(message);
		}

		public ReflectionException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	// Direct I/O

	public static DirectFileInputDescription getDirectFileInputDescription(String className) {
		try {
			Class<? extends DirectFileInputDescription> descriptionClass = Class.forName(className).asSubclass(
					DirectFileInputDescription.class);
			Constructor<? extends DirectFileInputDescription> descriptionConstructor = descriptionClass
					.getConstructor();
			DirectFileInputDescription dfid = descriptionConstructor.newInstance();

			return dfid;
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOG.error(String.format("failed to create %s instance", DirectFileInputDescription.class.getName()));
			throw new ReflectionException(e.getMessage(), e);
		}
	}

	public static <T extends DataModel<T>> BinaryStreamFormat<T> getBinaryStreamFormat(DirectFileInputDescription dfid) {
		try {
			@SuppressWarnings("unchecked")
			Class<BinaryStreamFormat<T>> dfClass = (Class<BinaryStreamFormat<T>>) dfid.getFormat();
			Constructor<BinaryStreamFormat<T>> constructor = dfClass.getConstructor();
			BinaryStreamFormat<T> bsf = constructor.newInstance();

			return bsf;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LOG.error(String.format("failed to create %s instance", BinaryStreamFormat.class.getName()));
			throw new ReflectionException(e.getMessage(), e);
		}
	}

	public static <T extends DataModel<T>> Class<T> getDataModelClass(DirectFileInputDescription dfid) {
		@SuppressWarnings("unchecked")
		Class<T> modelType = (Class<T>) dfid.getModelType();

		return modelType;
	}

	public static <T extends DataModel<T>> T getDataModel(Class<T> modelType) {
		try {
			Constructor<T> modelConstructor = modelType.getConstructor();
			T model = modelConstructor.newInstance();

			return model;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LOG.error(String.format("failed to create %s instance", DataModel.class.getName()));
			throw new ReflectionException(e.getMessage(), e);
		}
	}
}
