/*
 * Copyright (c) 2018 David Boissier.
 * Modifications Copyright (c) 2022 Geetesh Gupta.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gg.plugins.mongo.view

import com.gg.plugins.mongo.config.ConfigurationException
import com.gg.plugins.mongo.config.ServerConfiguration
import com.gg.plugins.mongo.config.SshTunnelingConfiguration
import com.gg.plugins.mongo.config.ssh.AuthenticationMethod
import com.gg.plugins.mongo.service.MongoService
import com.gg.plugins.mongo.utils.GuiUtils
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComponentWithBrowseButton.BrowseFolderActionListener
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.Ref
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.NumberDocument
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBPasswordField
import com.mongodb.AuthenticationMechanism
import com.mongodb.ReadPreference
import org.apache.commons.lang.StringUtils
import java.awt.BorderLayout
import java.util.LinkedList
import javax.swing.ButtonGroup
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JRadioButton
import javax.swing.JTabbedPane
import javax.swing.JTextField

class ServerConfigurationPanel internal constructor(private val project: Project?, mongoService: MongoService) :
    JPanel() {
    private val mongoService: MongoService
    lateinit var rootPanel: JPanel
    lateinit var feedbackLabel: JLabel
    lateinit var sslConnectionField: JCheckBox
    lateinit var labelField: JTextField
    lateinit var serverUrlsField: JTextField
    lateinit var usernameField: JTextField
    lateinit var passwordField: JPasswordField
    lateinit var authenticationDatabaseField: JTextField
    lateinit var scramSHA1AuthRadioButton: JRadioButton
    lateinit var plainAuthMethodRadioButton: JRadioButton
    lateinit var userDatabaseField: JTextField
    lateinit var testConnectionButton: JButton
    lateinit var collectionsToIgnoreField: JTextField
    lateinit var mongoShellOptionsPanel: JPanel
    lateinit var shellWorkingDirField: TextFieldWithBrowseButton
    lateinit var shellArgumentsLineField: RawCommandLineEditor
    lateinit var readPreferenceComboBox: JComboBox<ReadPreference>
    lateinit var sshProxyUrlField: JTextField
    lateinit var sshProxyUserField: JTextField
    lateinit var settingTabbedPane: JTabbedPane
    lateinit var sshAuthenticationMethodComboBox: JComboBox<AuthenticationMethod>
    lateinit var sshProxyPasswordField: JBPasswordField
    lateinit var privateKeyPathLabel: JLabel
    lateinit var privateKeyPathField: TextFieldWithBrowseButton
    lateinit var passLabel: JLabel
    lateinit var defaultRowLimitTextField: JTextField

    init {
        layout = BorderLayout()
        add(rootPanel, BorderLayout.CENTER)
        this.mongoService = mongoService
        labelField.name = "labelField"
        feedbackLabel.name = "feedbackLabel"
        settingTabbedPane.name = "tabbedSettings"
        sslConnectionField.name = "sslConnectionField"
        readPreferenceComboBox.name = "readPreferenceComboBox"
        serverUrlsField.name = "serverUrlsField"
        usernameField.name = "usernameField"
        passwordField.name = "passwordField"
        authenticationDatabaseField.name = "authenticationDatabaseField"
        authenticationDatabaseField.toolTipText =
            "admin by default, otherwise set the user database here if restricted access"
        scramSHA1AuthRadioButton.name = "scramSHA1AuthField"
        plainAuthMethodRadioButton.name = "defaultAuthMethod"
        sshProxyUrlField.name = "sshProxyUrlField"
        sshProxyUserField.name = "sshProxyUsernameField"
        sshProxyPasswordField.name = "sshProxyPasswordField"
        userDatabaseField.name = "userDatabaseField"
        userDatabaseField.toolTipText =
            "If your access is restricted to a specific database (e.g.: MongoLab), you can set it right here"
        defaultRowLimitTextField.name = "defaultRowLimitTextField"
        mongoShellOptionsPanel.border = IdeBorderFactory.createTitledBorder("Mongo Shell Options", true)
        //		shellArgumentsLineField.setDialogCaption("Mongo arguments");
        defaultRowLimitTextField.columns = 7
        defaultRowLimitTextField.document = NumberDocument()
        testConnectionButton.name = "testConnection"
        readPreferenceComboBox.model = DefaultComboBoxModel(
            arrayOf(
                ReadPreference.primary(),
                ReadPreference.primaryPreferred(),
                ReadPreference.secondary(),
                ReadPreference.secondaryPreferred(),
                ReadPreference.nearest()
            )
        )
        readPreferenceComboBox.renderer = object : ColoredListCellRenderer<Any?>() {
            override fun customizeCellRenderer(
                list: JList<*>,
                value: Any?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                val readPreference = value as ReadPreference
                append(readPreference.name)
            }
        }
        readPreferenceComboBox.selectedItem = ReadPreference.primary()
        val authMethodGroup = ButtonGroup()
        authMethodGroup.add(scramSHA1AuthRadioButton)
        authMethodGroup.add(plainAuthMethodRadioButton)
        sshAuthenticationMethodComboBox.name = "sshAuthenticationMethodComboBox"
        sshAuthenticationMethodComboBox.model = DefaultComboBoxModel(AuthenticationMethod.values())
        passLabel.name = "passLabel"
        sshAuthenticationMethodComboBox.selectedItem = AuthenticationMethod.PRIVATE_KEY
        sshAuthenticationMethodComboBox.addItemListener {
            val selectedAuthMethod = sshAuthenticationMethodComboBox.selectedItem as AuthenticationMethod
            val shouldUsePrivateKey = AuthenticationMethod.PRIVATE_KEY == selectedAuthMethod
            privateKeyPathLabel.isVisible = shouldUsePrivateKey
            privateKeyPathField.isVisible = shouldUsePrivateKey
            if (!shouldUsePrivateKey) {
                passLabel.labelFor = sshProxyPasswordField
                passLabel.text = "Password:"
                privateKeyPathField.setText(null)
            } else {
                passLabel.text = "Passphrase:"
            }
        }
        privateKeyPathField.name = "sshPrivateKeyPathComponent"
        privateKeyPathField.textField.name = "sshPrivateKeyPathField"
        scramSHA1AuthRadioButton.isSelected = true
        shellWorkingDirField.setText(null)
        initListeners()
    }

    private fun initListeners() {
        testConnectionButton.addActionListener {
            val configuration = createServerConfigurationForTesting()
            val excRef = Ref<Exception>()
            val progressManager = ProgressManager.getInstance()
            progressManager.runProcessWithProgressSynchronously({
                val progressIndicator = progressManager.progressIndicator
                if (progressIndicator != null) {
                    progressIndicator.text = "Connecting to Mongo server..."
                }
                try {
                    mongoService.connect(configuration)
                } catch (ex: Exception) {
                    excRef.set(ex)
                }
            }, "Testing connection", true, project)
            if (!excRef.isNull) {
                Messages.showErrorDialog(rootPanel, excRef.get().message, "Connection Test Failed")
            } else {
                Messages.showInfoMessage(rootPanel, "Connection test successful", "Connection Test Successful")
            }
        }
    }

    private fun createServerConfigurationForTesting(): ServerConfiguration {
        val configuration: ServerConfiguration = ServerConfiguration.byDefault()
        configuration.serverUrls = serverUrls
        configuration.username = username
        configuration.password = password
        configuration.authenticationDatabase = authenticationDatabase
        configuration.userDatabase = userDatabase
        configuration.authenticationMechanism = authenticationMechanism
        configuration.isSslConnection = isSslConnection
        configuration.sshTunnelingConfiguration =
            if (isSshTunneling) createSshTunnelingSettings() else SshTunnelingConfiguration.EMPTY
        return configuration
    }

    private val serverUrls: List<String>
        get() {
            val serverUrls = serverUrlsField.text
            return if (StringUtils.isNotBlank(serverUrls)) {
                listOf(
                    *StringUtils.split(
                        StringUtils.deleteWhitespace(
                            serverUrls
                        ), ","
                    )
                )
            } else emptyList()
        }
    private val username: String?
        get() {
            val username = usernameField.text
            return if (StringUtils.isNotBlank(username)) {
                username
            } else null
        }
    private val password: String?
        get() {
            val password = passwordField.password
            return if (password != null && password.isNotEmpty()) {
                String(password)
            } else null
        }
    private val authenticationDatabase: String?
        get() {
            val authenticationDatabase = authenticationDatabaseField.text
            return if (StringUtils.isNotBlank(authenticationDatabase)) {
                authenticationDatabase
            } else null
        }
    private val userDatabase: String?
        get() {
            val userDatabase = userDatabaseField.text
            return if (StringUtils.isNotBlank(userDatabase)) {
                userDatabase
            } else null
        }
    private val authenticationMechanism: AuthenticationMechanism?
        get() = if (scramSHA1AuthRadioButton.isSelected) {
            AuthenticationMechanism.SCRAM_SHA_1
        } else null
    private val isSslConnection: Boolean
        get() = sslConnectionField.isSelected
    private val isSshTunneling: Boolean
        get() = StringUtils.isNotBlank(sshProxyHost)

    private fun createSshTunnelingSettings(): SshTunnelingConfiguration {
        return SshTunnelingConfiguration(
            sshProxyHost,
            sshProxyUser,
            sshAuthMethod,
            sshPrivateKeyPath,
            sshProxyPassword
        )
    }

    private val sshProxyHost: String?
        get() {
            val proxyHost = sshProxyUrlField.text
            return if (StringUtils.isNotBlank(proxyHost)) {
                proxyHost
            } else null
        }
    private val sshProxyUser: String?
        get() {
            val proxyUser = sshProxyUserField.text
            return if (StringUtils.isNotBlank(proxyUser)) {
                proxyUser
            } else null
        }
    private val sshAuthMethod: AuthenticationMethod
        get() {
            return sshAuthenticationMethodComboBox.selectedItem as AuthenticationMethod
        }
    private val sshPrivateKeyPath: String?
        get() {
            val shellPath = privateKeyPathField.text
            return if (StringUtils.isNotBlank(shellPath)) {
                shellPath
            } else null
        }
    private val sshProxyPassword: String?
        get() {
            val proxyUser = String(sshProxyPasswordField.password)
            return if (StringUtils.isNotBlank(proxyUser)) {
                proxyUser
            } else null
        }

    fun applyConfigurationData(configuration: ServerConfiguration) {
        validateLabel()
        validateUrls()
        configuration.label = label.toString()
        configuration.serverUrls = serverUrls
        configuration.isSslConnection = isSslConnection
        configuration.readPreference = readPreference
        configuration.username = username
        configuration.password = password
        configuration.userDatabase = userDatabase
        configuration.authenticationDatabase = authenticationDatabase
        configuration.collectionsToIgnore = collectionsToIgnore
        configuration.shellArgumentsLine = shellArgumentsLine
        configuration.shellWorkingDir = shellWorkingDir
        configuration.defaultRowLimit = defaultRowLimit
        configuration.authenticationMechanism = authenticationMechanism
        configuration.sshTunnelingConfiguration = if (isSshTunneling) createSshTunnelingSettings() else
            SshTunnelingConfiguration.EMPTY
    }

    private fun validateLabel() {
        val label = label
        if (StringUtils.isBlank(label)) {
            throw ConfigurationException("Label should be set")
        }
    }

    private fun validateUrls() {
        val serverUrls = serverUrls
        for (serverUrl in serverUrls) {
            val hostPort = serverUrl.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (hostPort.size < 2) {
                throw ConfigurationException(
                    String.format(
                        "URL '%s' format is incorrect. It should be " + "'host:port'", serverUrl
                    )
                )
            }
            try {
                Integer.valueOf(hostPort[1])
            } catch (e: NumberFormatException) {
                throw ConfigurationException(
                    String.format(
                        "Port in the URL '%s' is incorrect. It should be a number",
                        serverUrl
                    )
                )
            }
        }
    }

    private val label: String?
        get() {
            val label = labelField.text
            return if (StringUtils.isNotBlank(label)) {
                label
            } else null
        }
    private val readPreference: ReadPreference
        get() = readPreferenceComboBox.selectedItem as ReadPreference
    private val collectionsToIgnore: List<String?>
        get() {
            val collectionsToIgnoreText = collectionsToIgnoreField.text
            if (StringUtils.isNotBlank(collectionsToIgnoreText)) {
                val collectionsToIgnore =
                    collectionsToIgnoreText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val collections: MutableList<String?> = LinkedList()
                for (collectionToIgnore in collectionsToIgnore) {
                    collections.add(StringUtils.trim(collectionToIgnore))
                }
                return collections
            }
            return emptyList<String>()
        }
    private val shellArgumentsLine: String?
        get() {
            val shellArgumentsLine = shellArgumentsLineField.text
            return if (StringUtils.isNotBlank(shellArgumentsLine)) {
                shellArgumentsLine
            } else null
        }
    private val shellWorkingDir: String?
        get() {
            val shellWorkingDir = shellWorkingDirField.text
            return if (StringUtils.isNotBlank(shellWorkingDir)) {
                shellWorkingDir
            } else null
        }
    private val defaultRowLimit: Int
        get() {
            val defaultRowLimit = defaultRowLimitTextField.text
            return if (StringUtils.isNotBlank(defaultRowLimit)) {
                defaultRowLimit.toInt()
            } else ServerConfiguration.DEFAULT_ROW_LIMIT
        }

    fun loadConfigurationData(configuration: ServerConfiguration) {
        labelField.text = configuration.label
        serverUrlsField.text = configuration.getUrlsInSingleString()
        usernameField.text = configuration.username
        passwordField.text = configuration.password
        userDatabaseField.text = configuration.userDatabase
        authenticationDatabaseField.text = configuration.authenticationDatabase
        sslConnectionField.isSelected = configuration.isSslConnection
        readPreferenceComboBox.selectedItem = configuration.readPreference
        collectionsToIgnoreField.text = StringUtils.join(configuration.collectionsToIgnore, ",")
        shellArgumentsLineField.text = configuration.shellArgumentsLine
        shellWorkingDirField.text = configuration.shellWorkingDir.toString()
        defaultRowLimitTextField.text = configuration.defaultRowLimit.toString()
        val sshTunnelingConfiguration = configuration.sshTunnelingConfiguration
        if (sshTunnelingConfiguration != null && !SshTunnelingConfiguration.isEmpty(sshTunnelingConfiguration)) {
            sshProxyUrlField.text = sshTunnelingConfiguration.proxyUrl
            sshAuthenticationMethodComboBox.selectedItem = sshTunnelingConfiguration.authenticationMethod
            if (AuthenticationMethod.PRIVATE_KEY == sshTunnelingConfiguration.authenticationMethod) {
                privateKeyPathField.setText(sshTunnelingConfiguration.privateKeyPath)
            }
            sshProxyPasswordField.text = sshTunnelingConfiguration.proxyPassword
            sshProxyUserField.text = sshTunnelingConfiguration.proxyUser
        }
        val authenticationMethod = configuration.authenticationMechanism
        if (AuthenticationMechanism.SCRAM_SHA_1 == authenticationMethod) {
            scramSHA1AuthRadioButton.isSelected = true
        } else {
            plainAuthMethodRadioButton.isSelected = true
        }
    }

    private fun createUIComponents() {
        shellWorkingDirField = createShellWorkingDirField()
        privateKeyPathField = createPrivateKeyField()
    }

    private fun createShellWorkingDirField(): TextFieldWithBrowseButton {
        val shellWorkingDirField = TextFieldWithBrowseButton()
        val dirChooserDescriptor = FileChooserDescriptor(
            false, true, false, false, false,
            false
        )
        val browseFolderActionListener = BrowseFolderActionListener(
            "Mongo Shell Working Directory",
            null,
            shellWorkingDirField,
            project,
            dirChooserDescriptor,
            TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        )
        shellWorkingDirField.addActionListener(browseFolderActionListener)
        shellWorkingDirField.name = "shellWorkingDirField"
        return shellWorkingDirField
    }

    private fun createPrivateKeyField(): TextFieldWithBrowseButton {
        val privateKeyPathField = TextFieldWithBrowseButton()
        val fileChooserDescriptor =
            FileChooserDescriptor(
                true, false, false, false,
                false, false
            ).withShowHiddenFiles(true)
        val privateKeyBrowseFolderActionListener = BrowseFolderActionListener(
            "Private Key Path",
            null,
            privateKeyPathField,
            project,
            fileChooserDescriptor,
            TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        )
        privateKeyPathField.addActionListener(privateKeyBrowseFolderActionListener)
        return privateKeyPathField
    }

    fun setErrorMessage(message: String?) {
        feedbackLabel.icon = FAIL
        feedbackLabel.text = message
    }

    companion object {
        val SUCCESS = GuiUtils.loadIcon("success.png")
        val FAIL = GuiUtils.loadIcon("fail.png")
    }
}