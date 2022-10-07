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
package com.gg.plugins.mongo.config.ssh

import com.gg.plugins.mongo.config.ConfigurationException
import com.gg.plugins.mongo.config.ServerConfiguration
import com.gg.plugins.mongo.config.SshTunnelingConfiguration
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.apache.commons.lang.StringUtils
import java.io.Closeable
import java.util.LinkedList
import java.util.Properties

class SshConnection private constructor(
    serverUrls: List<String>,
    sshTunnelingConfiguration: SshTunnelingConfiguration?
) : Closeable {
    private val sshSessions: MutableList<Session> = LinkedList()

    init {
        requireNotNull(sshTunnelingConfiguration) { "SSH Configuration should be set" }
        var localPort = DEFAULT_TUNNEL_LOCAL_PORT
        for (serverUrl in serverUrls) {
            val session = createSshSession(
                sshTunnelingConfiguration,
                ServerConfiguration.extractHostAndPort(serverUrl),
                localPort++
            )
            sshSessions.add(session)
        }
    }

    private fun createSshSession(
        sshTunnelingConfiguration: SshTunnelingConfiguration,
        hostAndPort: ServerConfiguration.HostAndPort,
        localPort: Int
    ): Session {
        return try {
            var port = DEFAULT_SSH_REMOTE_PORT
            val jsch = JSch()
            var proxyHost = sshTunnelingConfiguration.proxyUrl
            if (proxyHost!!.contains(":")) {
                val host_port = StringUtils.split(proxyHost, ":")
                port = host_port[1].toInt()
                proxyHost = host_port[0]
            }
            val authenticationMethod = sshTunnelingConfiguration.authenticationMethod
            val proxyUser = sshTunnelingConfiguration.proxyUser
            val password = sshTunnelingConfiguration.proxyPassword
            val session = jsch.getSession(proxyUser, proxyHost, port)
            if (AuthenticationMethod.PRIVATE_KEY == authenticationMethod) {
                jsch.addIdentity(
                    sshTunnelingConfiguration.privateKeyPath,
                    sshTunnelingConfiguration.proxyPassword
                )
            } else {
                session.setPassword(password)
            }
            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            session.setConfig(config)
            session.connect()
            val remoteMongoHost = hostAndPort.host
            val remoteMongoPort = hostAndPort.port
            session.setPortForwardingL(localPort, remoteMongoHost, remoteMongoPort)
            session
        } catch (ex: JSchException) {
            throw ConfigurationException(ex)
        }
    }

    override fun close() {
        for (sshSession in sshSessions) {
            sshSession.disconnect()
        }
    }

    companion object {
        const val DEFAULT_SSH_REMOTE_PORT = 22
        private const val DEFAULT_TUNNEL_LOCAL_PORT = 9080
        fun create(serverConfiguration: ServerConfiguration): SshConnection {
            return SshConnection(
                serverConfiguration.serverUrls,
                serverConfiguration.sshTunnelingConfiguration
            )
        }
    }
}