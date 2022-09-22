/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.utils;

import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.model.MongoDatabase;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.json.JsonWriterSettings;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class MongoUtils {

	public static final DocumentCodec DOCUMENT_CODEC =
			new DocumentCodec(CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry()),
					new BsonTypeClassMap());

	public static final JsonWriterSettings WRITER_SETTINGS = JsonWriterSettings.builder().indent(true).build();

	private MongoUtils() {
	}

	public static boolean checkMongoShellPath(String mongoShellPath) throws ExecutionException {
		if (isBlank(mongoShellPath)) {
			return false;
		}

		GeneralCommandLine commandLine = new GeneralCommandLine();
		commandLine.setExePath(mongoShellPath);
		commandLine.addParameter("--version");
		CapturingProcessHandler handler = new CapturingProcessHandler(commandLine.createProcess(),
				CharsetToolkit.getDefaultSystemCharset(),
				commandLine.getCommandLineString());
		ProcessOutput result = handler.runProcess(15 * 1000);
		return result.getExitCode() == 0;
	}

	public static GeneralCommandLine buildCommandLine(String shellPath,
			ServerConfiguration serverConfiguration,
			MongoDatabase database) {
		GeneralCommandLine commandLine = new GeneralCommandLine();
		commandLine.setExePath(shellPath);
		commandLine.addParameter(buildMongoUrl(serverConfiguration, database));

		String username = serverConfiguration.getUsername();
		if (org.apache.commons.lang.StringUtils.isNotBlank(username)) {
			commandLine.addParameter("--username");
			commandLine.addParameter(username);
		}

		String password = serverConfiguration.getPassword();
		if (org.apache.commons.lang.StringUtils.isNotBlank(password)) {
			commandLine.addParameter("--password");
			commandLine.addParameter(password);
		}

		String authenticationDatabase = serverConfiguration.getAuthenticationDatabase();
		if (org.apache.commons.lang.StringUtils.isNotBlank(authenticationDatabase)) {
			commandLine.addParameter("--authenticationDatabase");
			commandLine.addParameter(authenticationDatabase);
		}

		AuthenticationMechanism authenticationMechanism = serverConfiguration.getAuthenticationMechanism();
		if (authenticationMechanism != null) {
			commandLine.addParameter("--authenticationMechanism");
			commandLine.addParameter(authenticationMechanism.getMechanismName());
		}

		String shellWorkingDir = serverConfiguration.getShellWorkingDir();
		if (org.apache.commons.lang.StringUtils.isNotBlank(shellWorkingDir)) {
			commandLine.setWorkDirectory(shellWorkingDir);
		}

		String shellArgumentsLine = serverConfiguration.getShellArgumentsLine();
		if (org.apache.commons.lang.StringUtils.isNotBlank(shellArgumentsLine)) {
			commandLine.addParameters(shellArgumentsLine.split(" "));
		}

		return commandLine;
	}

	static String buildMongoUrl(ServerConfiguration serverConfiguration, MongoDatabase database) {
		return String.format("%s/%s",
				serverConfiguration.getServerUrls().get(0),
				database == null ? "test" : database.getName());
	}

	public static String stringifyList(List list) {
		List<String> stringifiedObjects = new LinkedList<>();
		for (Object object : list) {
			if (object == null) {
				stringifiedObjects.add("null");
			} else if (object instanceof String) {
				stringifiedObjects.add("\"" + object + "\"");
			} else if (object instanceof Document) {
				stringifiedObjects.add(((Document) object).toJson(DOCUMENT_CODEC));
			} else if (object instanceof List) {
				stringifiedObjects.add(stringifyList(((List) object)));
			} else {
				stringifiedObjects.add(object.toString());
			}
		}

		return "[" + org.apache.commons.lang.StringUtils.join(stringifiedObjects, ", ") + "]";
	}
}
