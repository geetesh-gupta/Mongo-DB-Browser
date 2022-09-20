/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.config;

import com.gg.plugins.mongo.config.ssh.AuthenticationMethod;

import java.util.Objects;

public class SshTunnelingConfiguration implements Cloneable {

	public static final SshTunnelingConfiguration EMPTY = new SshTunnelingConfiguration();

	private String proxyUrl;

	private String proxyUser;

	private AuthenticationMethod authenticationMethod;

	private String proxyPassword;

	private String privateKeyPath;

	private SshTunnelingConfiguration() {
		proxyUrl = null;
		proxyUser = null;
		authenticationMethod = AuthenticationMethod.PRIVATE_KEY;
		privateKeyPath = null;
		proxyPassword = null;
	}

	public SshTunnelingConfiguration(String proxyUrl,
			String proxyUser,
			AuthenticationMethod authenticationMethod,
			String privateKeyPath,
			String proxyPassword) {
		this.proxyUrl = proxyUrl;
		this.proxyUser = proxyUser;
		this.authenticationMethod = authenticationMethod;
		this.privateKeyPath = privateKeyPath;
		this.proxyPassword = proxyPassword;
	}

	public static boolean isEmpty(SshTunnelingConfiguration sshTunnelingConfiguration) {
		return sshTunnelingConfiguration == null || EMPTY.equals(sshTunnelingConfiguration);
	}

	public String getProxyUrl() {
		return proxyUrl;
	}

	public void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public AuthenticationMethod getAuthenticationMethod() {
		return authenticationMethod;
	}

	public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
		this.authenticationMethod = authenticationMethod;
	}

	public String getPrivateKeyPath() {
		return privateKeyPath;
	}

	public void setPrivateKeyPath(String privateKeyPath) {
		this.privateKeyPath = privateKeyPath;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	@Override
	public int hashCode() {

		return Objects.hash(proxyUrl, proxyUser, authenticationMethod, proxyPassword, privateKeyPath);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SshTunnelingConfiguration))
			return false;
		SshTunnelingConfiguration that = (SshTunnelingConfiguration) o;
		return Objects.equals(proxyUrl, that.proxyUrl) && Objects.equals(proxyUser, that.proxyUser) &&
		       authenticationMethod == that.authenticationMethod && Objects.equals(proxyPassword,
				that.proxyPassword) &&
		       Objects.equals(privateKeyPath, that.privateKeyPath);
	}

	public ServerConfiguration clone() {
		try {
			return (ServerConfiguration) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
