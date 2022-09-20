/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.config.ssh;

import com.gg.plugins.mongo.config.ConfigurationException;
import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.config.SshTunnelingConfiguration;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang.StringUtils;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;

public class SshConnection implements Closeable {

	public static final int DEFAULT_SSH_REMOTE_PORT = 22;

	private static final int DEFAULT_TUNNEL_LOCAL_PORT = 9080;

	private final List<Session> sshSessions = new LinkedList<>();

	private SshConnection(List<String> serverUrls, SshTunnelingConfiguration sshTunnelingConfiguration) {
		if (sshTunnelingConfiguration == null) {
			throw new IllegalArgumentException("SSH Configuration should be set");
		}

		int localPort = DEFAULT_TUNNEL_LOCAL_PORT;
		for (String serverUrl : serverUrls) {
			Session session = createSshSession(sshTunnelingConfiguration,
					ServerConfiguration.extractHostAndPort(serverUrl),
					localPort++);
			sshSessions.add(session);
		}
	}

	private Session createSshSession(SshTunnelingConfiguration sshTunnelingConfiguration,
			ServerConfiguration.HostAndPort hostAndPort,
			int localPort) {
		try {
			int port = DEFAULT_SSH_REMOTE_PORT;
			JSch jsch = new JSch();

			String proxyHost = sshTunnelingConfiguration.getProxyUrl();
			if (proxyHost.contains(":")) {
				String[] host_port = StringUtils.split(proxyHost, ":");
				port = Integer.parseInt(host_port[1]);
				proxyHost = host_port[0];
			}
			AuthenticationMethod authenticationMethod = sshTunnelingConfiguration.getAuthenticationMethod();
			String proxyUser = sshTunnelingConfiguration.getProxyUser();
			String password = sshTunnelingConfiguration.getProxyPassword();
			Session session = jsch.getSession(proxyUser, proxyHost, port);
			if (AuthenticationMethod.PRIVATE_KEY.equals(authenticationMethod)) {
				jsch.addIdentity(sshTunnelingConfiguration.getPrivateKeyPath(),
						sshTunnelingConfiguration.getProxyPassword());
			} else {
				session.setPassword(password);
			}

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();

			String remoteMongoHost = hostAndPort.host;
			int remoteMongoPort = hostAndPort.port;
			session.setPortForwardingL(localPort, remoteMongoHost, remoteMongoPort);

			return session;

		} catch (JSchException ex) {
			throw new ConfigurationException(ex);
		}
	}

	public static SshConnection create(ServerConfiguration serverConfiguration) {
		return new SshConnection(serverConfiguration.getServerUrls(),
				serverConfiguration.getSshTunnelingConfiguration());
	}

	public void close() {
		for (Session sshSession : sshSessions) {
			sshSession.disconnect();
		}
	}
}
