/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view;

import com.gg.plugins.mongo.config.ConfigurationException;
import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.config.SshTunnelingConfiguration;
import com.gg.plugins.mongo.config.ssh.AuthenticationMethod;
import com.gg.plugins.mongo.service.MongoService;
import com.gg.plugins.mongo.utils.GuiUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.NumberDocument;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBPasswordField;
import com.mongodb.AuthenticationMechanism;
import com.mongodb.ReadPreference;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ServerConfigurationPanel extends JPanel {

	public static final Icon SUCCESS = GuiUtils.loadIcon("success.png");

	public static final Icon FAIL = GuiUtils.loadIcon("fail.png");

	private final Project project;

	private final MongoService mongoService;

	private JPanel rootPanel;

	private JLabel feedbackLabel;

	private JCheckBox sslConnectionField;

	private JTextField labelField;

	private JTextField serverUrlsField;

	private JTextField usernameField;

	private JPasswordField passwordField;

	private JTextField authenticationDatabaseField;

	private JRadioButton scramSHA1AuthRadioButton;

	private JRadioButton plainAuthMethodRadioButton;

	private JTextField userDatabaseField;

	private JButton testConnectionButton;

	private JTextField collectionsToIgnoreField;

	private JPanel mongoShellOptionsPanel;

	private TextFieldWithBrowseButton shellWorkingDirField;

	private RawCommandLineEditor shellArgumentsLineField;

	private JComboBox readPreferenceComboBox;

	private JTextField sshProxyUrlField;

	private JTextField sshProxyUserField;

	private JTabbedPane settingTabbedPane;

	private JComboBox sshAuthenticationMethodComboBox;

	private JBPasswordField sshProxyPasswordField;

	private JLabel privateKeyPathLabel;

	private TextFieldWithBrowseButton privateKeyPathField;

	private JLabel passLabel;

	private JTextField defaultRowLimitTextField;

	ServerConfigurationPanel(Project project, MongoService mongoService) {
		this.project = project;
		setLayout(new BorderLayout());
		add(rootPanel, BorderLayout.CENTER);
		this.mongoService = mongoService;

		labelField.setName("labelField");
		feedbackLabel.setName("feedbackLabel");
		settingTabbedPane.setName("tabbedSettings");

		sslConnectionField.setName("sslConnectionField");

		readPreferenceComboBox.setName("readPreferenceComboBox");
		serverUrlsField.setName("serverUrlsField");
		usernameField.setName("usernameField");
		passwordField.setName("passwordField");
		authenticationDatabaseField.setName("authenticationDatabaseField");
		authenticationDatabaseField.setToolTipText(
				"admin by default, otherwise set the user database here if restricted access");
		scramSHA1AuthRadioButton.setName("scramSHA1AuthField");
		plainAuthMethodRadioButton.setName("defaultAuthMethod");

		sshProxyUrlField.setName("sshProxyUrlField");
		sshProxyUserField.setName("sshProxyUsernameField");
		sshProxyPasswordField.setName("sshProxyPasswordField");

		userDatabaseField.setName("userDatabaseField");
		userDatabaseField.setToolTipText(
				"If your access is restricted to a specific database (e.g.: MongoLab), you can set it right here");

		defaultRowLimitTextField.setName("defaultRowLimitTextField");
		mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Mongo Shell Options", true));
		//		shellArgumentsLineField.setDialogCaption("Mongo arguments");

		defaultRowLimitTextField.setColumns(7);
		defaultRowLimitTextField.setDocument(new NumberDocument());

		testConnectionButton.setName("testConnection");

		readPreferenceComboBox.setModel(new DefaultComboBoxModel<>(new ReadPreference[]{ReadPreference.primary(),
		                                                                                ReadPreference.primaryPreferred(),
		                                                                                ReadPreference.secondary(),
		                                                                                ReadPreference.secondaryPreferred(),
		                                                                                ReadPreference.nearest()}));

		readPreferenceComboBox.setRenderer(new ColoredListCellRenderer() {
			@Override
			protected void customizeCellRenderer(@NotNull JList list,
					Object value,
					int index,
					boolean selected,
					boolean hasFocus) {
				ReadPreference readPreference = (ReadPreference) value;
				append(readPreference.getName());
			}
		});

		readPreferenceComboBox.setSelectedItem(ReadPreference.primary());

		ButtonGroup authMethodGroup = new ButtonGroup();
		authMethodGroup.add(scramSHA1AuthRadioButton);
		authMethodGroup.add(plainAuthMethodRadioButton);

		sshAuthenticationMethodComboBox.setName("sshAuthenticationMethodComboBox");
		sshAuthenticationMethodComboBox.setModel(new DefaultComboBoxModel<>(AuthenticationMethod.values()));

		passLabel.setName("passLabel");
		sshAuthenticationMethodComboBox.setSelectedItem(AuthenticationMethod.PRIVATE_KEY);
		sshAuthenticationMethodComboBox.addItemListener(e -> {
			AuthenticationMethod selectedAuthMethod =
					(AuthenticationMethod) sshAuthenticationMethodComboBox.getSelectedItem();
			boolean shouldUsePrivateKey = AuthenticationMethod.PRIVATE_KEY.equals(selectedAuthMethod);

			privateKeyPathLabel.setVisible(shouldUsePrivateKey);
			privateKeyPathField.setVisible(shouldUsePrivateKey);
			if (!shouldUsePrivateKey) {
				passLabel.setLabelFor(sshProxyPasswordField);
				passLabel.setText("Password:");
				privateKeyPathField.setText(null);
			} else {
				passLabel.setText("Passphrase:");
			}
		});

		privateKeyPathField.setName("sshPrivateKeyPathComponent");
		privateKeyPathField.getTextField().setName("sshPrivateKeyPathField");

		scramSHA1AuthRadioButton.setSelected(true);

		shellWorkingDirField.setText(null);

		initListeners();
	}

	private void initListeners() {
		testConnectionButton.addActionListener(actionEvent -> {
			ServerConfiguration configuration = createServerConfigurationForTesting();

			final Ref<Exception> excRef = new Ref<>();
			final ProgressManager progressManager = ProgressManager.getInstance();
			progressManager.runProcessWithProgressSynchronously(() -> {

				final ProgressIndicator progressIndicator = progressManager.getProgressIndicator();
				if (progressIndicator != null) {
					progressIndicator.setText("Connecting to Mongo server...");
				}
				try {
					mongoService.connect(configuration);
				} catch (Exception ex) {
					excRef.set(ex);
				}
			}, "Testing connection", true, ServerConfigurationPanel.this.project);

			if (!excRef.isNull()) {
				Messages.showErrorDialog(rootPanel, excRef.get().getMessage(), "Connection Test Failed");
			} else {
				Messages.showInfoMessage(rootPanel, "Connection test successful", "Connection Test Successful");
			}
		});
	}

	@NotNull
	private ServerConfiguration createServerConfigurationForTesting() {
		ServerConfiguration configuration = ServerConfiguration.byDefault();
		configuration.setServerUrls(getServerUrls());
		configuration.setUsername(getUsername());
		configuration.setPassword(getPassword());
		configuration.setAuthenticationDatabase(getAuthenticationDatabase());
		configuration.setUserDatabase(getUserDatabase());
		configuration.setAuthenticationMechanism(getAuthenticationMechanism());
		configuration.setSslConnection(isSslConnection());

		configuration.setSshTunnelingConfiguration(isSshTunneling() ?
		                                           createSshTunnelingSettings() :
		                                           SshTunnelingConfiguration.EMPTY);

		return configuration;
	}

	private List<String> getServerUrls() {
		String serverUrls = serverUrlsField.getText();
		if (StringUtils.isNotBlank(serverUrls)) {
			return Arrays.asList(StringUtils.split(StringUtils.deleteWhitespace(serverUrls), ","));
		}
		return null;
	}

	private String getUsername() {
		String username = usernameField.getText();
		if (StringUtils.isNotBlank(username)) {
			return username;
		}
		return null;
	}

	private String getPassword() {
		char[] password = passwordField.getPassword();
		if (password != null && password.length != 0) {
			return String.valueOf(password);
		}
		return null;
	}

	private String getAuthenticationDatabase() {
		String authenticationDatabase = authenticationDatabaseField.getText();
		if (StringUtils.isNotBlank(authenticationDatabase)) {
			return authenticationDatabase;
		}
		return null;
	}

	private String getUserDatabase() {
		String userDatabase = userDatabaseField.getText();
		if (StringUtils.isNotBlank(userDatabase)) {
			return userDatabase;
		}
		return null;
	}

	private AuthenticationMechanism getAuthenticationMechanism() {
		if (scramSHA1AuthRadioButton.isSelected()) {
			return AuthenticationMechanism.SCRAM_SHA_1;
		}
		return null;
	}

	private boolean isSslConnection() {
		return sslConnectionField.isSelected();
	}

	private boolean isSshTunneling() {
		return StringUtils.isNotBlank(getSshProxyHost());
	}

	private SshTunnelingConfiguration createSshTunnelingSettings() {
		return new SshTunnelingConfiguration(getSshProxyHost(),
				getSshProxyUser(),
				getSshAuthMethod(),
				getSshPrivateKeyPath(),
				getSshProxyPassword());
	}

	private String getSshProxyHost() {
		String proxyHost = sshProxyUrlField.getText();
		if (StringUtils.isNotBlank(proxyHost)) {
			return proxyHost;
		}
		return null;
	}

	private String getSshProxyUser() {
		String proxyUser = sshProxyUserField.getText();
		if (StringUtils.isNotBlank(proxyUser)) {
			return proxyUser;
		}
		return null;
	}

	private AuthenticationMethod getSshAuthMethod() {
		AuthenticationMethod selectedAuthMethod =
				(AuthenticationMethod) sshAuthenticationMethodComboBox.getSelectedItem();
		if (selectedAuthMethod != null) {
			return selectedAuthMethod;
		}
		return AuthenticationMethod.PASSWORD;
	}

	private String getSshPrivateKeyPath() {
		String shellPath = privateKeyPathField.getText();
		if (StringUtils.isNotBlank(shellPath)) {
			return shellPath;
		}

		return null;
	}

	private String getSshProxyPassword() {
		String proxyUser = String.valueOf(sshProxyPasswordField.getPassword());
		if (StringUtils.isNotBlank(proxyUser)) {
			return proxyUser;
		}
		return null;
	}

	public void applyConfigurationData(ServerConfiguration configuration) {
		validateLabel();
		validateUrls();

		configuration.setLabel(getLabel());
		configuration.setServerUrls(getServerUrls());
		configuration.setSslConnection(isSslConnection());
		configuration.setReadPreference(getReadPreference());

		configuration.setUsername(getUsername());
		configuration.setPassword(getPassword());
		configuration.setUserDatabase(getUserDatabase());
		configuration.setAuthenticationDatabase(getAuthenticationDatabase());

		configuration.setCollectionsToIgnore(getCollectionsToIgnore());
		configuration.setShellArgumentsLine(getShellArgumentsLine());
		configuration.setShellWorkingDir(getShellWorkingDir());
		configuration.setDefaultRowLimit(getDefaultRowLimit());

		configuration.setAuthenticationMechanism(getAuthenticationMechanism());

		configuration.setSshTunnelingConfiguration(isSshTunneling() ?
		                                           createSshTunnelingSettings() :
		                                           SshTunnelingConfiguration.EMPTY);
	}

	private void validateLabel() {
		String label = getLabel();
		if (StringUtils.isBlank(label)) {
			throw new ConfigurationException("Label should be set");
		}
	}

	private void validateUrls() {
		List<String> serverUrls = getServerUrls();
		if (serverUrls == null) {
			throw new ConfigurationException("URL(s) should be set");
		}
		for (String serverUrl : serverUrls) {
			String[] host_port = serverUrl.split(":");
			if (host_port.length < 2) {
				throw new ConfigurationException(String.format(
						"URL '%s' format is incorrect. It should be " + "'host:port'", serverUrl));
			}

			try {
				Integer.valueOf(host_port[1]);
			} catch (NumberFormatException e) {
				throw new ConfigurationException(String.format(
						"Port in the URL '%s' is incorrect. It should be a number",
						serverUrl));
			}
		}

	}

	private String getLabel() {
		String label = labelField.getText();
		if (StringUtils.isNotBlank(label)) {
			return label;
		}
		return null;
	}

	private ReadPreference getReadPreference() {
		return (ReadPreference) readPreferenceComboBox.getSelectedItem();
	}

	private List<String> getCollectionsToIgnore() {
		String collectionsToIgnoreText = collectionsToIgnoreField.getText();
		if (StringUtils.isNotBlank(collectionsToIgnoreText)) {
			String[] collectionsToIgnore = collectionsToIgnoreText.split(",");

			List<String> collections = new LinkedList<>();
			for (String collectionToIgnore : collectionsToIgnore) {
				collections.add(StringUtils.trim(collectionToIgnore));
			}
			return collections;
		}
		return Collections.emptyList();
	}

	private String getShellArgumentsLine() {
		String shellArgumentsLine = shellArgumentsLineField.getText();
		if (StringUtils.isNotBlank(shellArgumentsLine)) {
			return shellArgumentsLine;
		}

		return null;
	}

	private String getShellWorkingDir() {
		String shellWorkingDir = shellWorkingDirField.getText();
		if (StringUtils.isNotBlank(shellWorkingDir)) {
			return shellWorkingDir;
		}

		return null;
	}

	private Integer getDefaultRowLimit() {
		String defaultRowLimit = defaultRowLimitTextField.getText();
		if (StringUtils.isNotBlank(defaultRowLimit)) {
			return Integer.parseInt(defaultRowLimit);
		}
		return ServerConfiguration.DEFAULT_ROW_LIMIT;
	}

	public void loadConfigurationData(ServerConfiguration configuration) {
		labelField.setText(configuration.getLabel());
		serverUrlsField.setText(configuration.getUrlsInSingleString());
		usernameField.setText(configuration.getUsername());
		passwordField.setText(configuration.getPassword());
		userDatabaseField.setText(configuration.getUserDatabase());
		authenticationDatabaseField.setText(configuration.getAuthenticationDatabase());
		sslConnectionField.setSelected(configuration.isSslConnection());
		readPreferenceComboBox.setSelectedItem(configuration.getReadPreference());

		collectionsToIgnoreField.setText(StringUtils.join(configuration.getCollectionsToIgnore(), ","));
		shellArgumentsLineField.setText(configuration.getShellArgumentsLine());
		shellWorkingDirField.setText(configuration.getShellWorkingDir());
		defaultRowLimitTextField.setText(Integer.toString(configuration.getDefaultRowLimit()));

		SshTunnelingConfiguration sshTunnelingConfiguration = configuration.getSshTunnelingConfiguration();
		if (!SshTunnelingConfiguration.isEmpty(sshTunnelingConfiguration)) {
			sshProxyUrlField.setText(sshTunnelingConfiguration.getProxyUrl());
			sshAuthenticationMethodComboBox.setSelectedItem(sshTunnelingConfiguration.getAuthenticationMethod());
			if (AuthenticationMethod.PRIVATE_KEY.equals(sshTunnelingConfiguration.getAuthenticationMethod())) {
				privateKeyPathField.setText(sshTunnelingConfiguration.getPrivateKeyPath());
			}
			sshProxyPasswordField.setText(sshTunnelingConfiguration.getProxyPassword());
			sshProxyUserField.setText(sshTunnelingConfiguration.getProxyUser());
		}

		AuthenticationMechanism authenticationMethod = configuration.getAuthenticationMechanism();
		if (AuthenticationMechanism.SCRAM_SHA_1.equals(authenticationMethod)) {
			scramSHA1AuthRadioButton.setSelected(true);
		} else {
			plainAuthMethodRadioButton.setSelected(true);
		}
	}

	private void createUIComponents() {
		shellWorkingDirField = createShellWorkingDirField();
		privateKeyPathField = createPrivateKeyField();
	}

	private TextFieldWithBrowseButton createShellWorkingDirField() {
		TextFieldWithBrowseButton shellWorkingDirField = new TextFieldWithBrowseButton();
		FileChooserDescriptor dirChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false,
				false);
		ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> browseFolderActionListener =
				new ComponentWithBrowseButton.BrowseFolderActionListener<>("Mongo Shell Working Directory",
						null,
						shellWorkingDirField,
						project,
						dirChooserDescriptor,
						TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
		shellWorkingDirField.addActionListener(browseFolderActionListener);
		shellWorkingDirField.setName("shellWorkingDirField");
		return shellWorkingDirField;
	}

	private TextFieldWithBrowseButton createPrivateKeyField() {
		TextFieldWithBrowseButton privateKeyPathField = new TextFieldWithBrowseButton();
		FileChooserDescriptor fileChooserDescriptor =
				new FileChooserDescriptor(true, false, false, false, false, false).withShowHiddenFiles(true);
		ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> privateKeyBrowseFolderActionListener =
				new ComponentWithBrowseButton.BrowseFolderActionListener<>("Private Key Path",
						null,
						privateKeyPathField,
						project,
						fileChooserDescriptor,
						TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
		privateKeyPathField.addActionListener(privateKeyBrowseFolderActionListener);

		return privateKeyPathField;
	}

	public void setErrorMessage(String message) {
		feedbackLabel.setIcon(FAIL);
		feedbackLabel.setText(message);
	}
}
