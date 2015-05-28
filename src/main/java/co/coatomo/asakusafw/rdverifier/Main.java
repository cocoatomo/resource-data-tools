package co.coatomo.asakusafw.rdverifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.coatomo.asakusafw.rdverifier.directio.DirectFileInputVerifier;
import co.coatomo.asakusafw.rdverifier.util.ReflectionUtils;

import com.asakusafw.vocabulary.directio.DirectFileInputDescription;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	/**
	 *
	 * @param args
	 *            - Arguments passed from the command line
	 **/
	public static void main(String[] args) {
		Options options = createOptions();
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine commandLine = parser.parse(options, args);
			Main main = new Main();
			System.exit(main.execute(commandLine.getOptionValue("data-file"), commandLine.getOptionValue("dfid"),
					resolvePath(commandLine.getOptionValues("jfj"))));
		} catch (ParseException e) {
			LOG.error("argument parse error", e);
			new HelpFormatter().printHelp("resource-data-verifier", options);
			System.exit(1);
		} catch (FileNotFoundException e) {
			LOG.error("missing jar file", e);
			new HelpFormatter().printHelp("resource-data-verifier", options);
			System.exit(1);
		} catch (MalformedURLException e) {
			LOG.error(e.getMessage(), e);
			new HelpFormatter().printHelp("resource-data-verifier", options);
			System.exit(1);
		}
	}

	public int execute(String dataFilePath, String dfidClassName, URL[] jarFiles) {
		LOG.info("verifying data file...");
		Verifier<DirectFileInputDescription> verifier = new DirectFileInputVerifier();
		try {
			DirectFileInputDescription dfid = ReflectionUtils.getDirectFileInputDescription(dfidClassName, jarFiles);
			verifier.verify(new File(dataFilePath), dfid);
			LOG.info("verified data file");
			return 0;
		} catch (Throwable th) {
			LOG.error("failed to verify data file", th);
			return 1;
		}
	}

	@SuppressWarnings("static-access")
	private static Options createOptions() {
		Options result = new Options();

		Option directFileInputDescription = OptionBuilder.withArgName("classname").hasArg()
				.withDescription("DirectFileInputDescription class name").isRequired().withLongOpt("dfid").create('d');
		result.addOption(directFileInputDescription);

		Option dataFile = OptionBuilder.withArgName("file").hasArg().withDescription("data file path").isRequired()
				.withLongOpt("data-file").create('f');
		result.addOption(dataFile);

		Option jobflowJar = OptionBuilder
				.withArgName("jobflowJarFiles")
				.hasArgs()
				.withDescription(
						"jobflow-*.jar containing DirectFileInputDescription class(es) (not jobflow-*-sources.jar)")
				.isRequired().withLongOpt("jfj").create('j');
		result.addOption(jobflowJar);

		return result;
	}

	private static URL[] resolvePath(String[] filePaths) throws FileNotFoundException, MalformedURLException {
		List<URL> result = new ArrayList<>(filePaths.length);

		for (String path : filePaths) {
			File file = new File(path);
			if (!file.exists()) {
				String message = MessageFormat.format("jar file not found: {0}", path);
				LOG.error(message);
				throw new FileNotFoundException(message);
			}
			result.add(file.toURI().toURL());
		}

		return result.toArray(new URL[filePaths.length]);
	}
}
