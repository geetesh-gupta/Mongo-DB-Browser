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
package com.gg.plugins.mongo.config

import com.mongodb.AuthenticationMechanism
import com.mongodb.ReadPreference
import org.apache.commons.lang.StringUtils
import java.util.LinkedList
import java.util.Objects

class ServerConfiguration : Cloneable, Comparable<ServerConfiguration> {
    var label: String = ""
    var serverUrls: List<String> = LinkedList()
    var isSslConnection = false
    var readPreference: ReadPreference = ReadPreference.primary()
    var username: String? = null
    var password: String? = null
    var authenticationDatabase: String? = null
    var authenticationMechanism: AuthenticationMechanism? = null
    var userDatabase: String? = null
    var collectionsToIgnore: List<String?> = LinkedList()
    var shellArgumentsLine: String? = null
    var shellWorkingDir: String? = null
    var defaultRowLimit = DEFAULT_ROW_LIMIT
    var sshTunnelingConfiguration: SshTunnelingConfiguration? = null

    fun getUrlsInSingleString(): String? {
        return StringUtils.join(serverUrls, ",")
    }

    fun isSingleServer(): Boolean {
        return serverUrls.size == 1
    }

    override fun hashCode(): Int {
        return Objects.hash(
            label,
            serverUrls,
            isSslConnection,
            readPreference,
            username,
            password,
            authenticationDatabase,
            authenticationMechanism,
            userDatabase,
            collectionsToIgnore,
            shellArgumentsLine,
            shellWorkingDir,
            defaultRowLimit,
            sshTunnelingConfiguration
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServerConfiguration) return false
        return isSslConnection == other.isSslConnection && label == other.label
                && serverUrls == other.serverUrls && readPreference == other.readPreference
                && username == other.username && password == other.password
                && authenticationDatabase == other.authenticationDatabase
                && authenticationMechanism == other.authenticationMechanism && userDatabase == other.userDatabase
                && collectionsToIgnore == other.collectionsToIgnore && shellArgumentsLine == other.shellArgumentsLine
                && shellWorkingDir == other.shellWorkingDir && defaultRowLimit == other.defaultRowLimit
                && sshTunnelingConfiguration == other.sshTunnelingConfiguration
    }

    public override fun clone(): ServerConfiguration {
        return super.clone() as ServerConfiguration
    }

    override fun compareTo(other: ServerConfiguration): Int {
        return label.compareTo(other.label)
    }

    class HostAndPort(val host: String, val port: Int)

    companion object {
        const val DEFAULT_ROW_LIMIT = 10
        private const val DEFAULT_URL = "localhost"
        private const val DEFAULT_PORT = 27017

        fun byDefault(): ServerConfiguration {
            val serverConfiguration = ServerConfiguration()
            serverConfiguration.serverUrls = listOf(String.format("%s:%s", DEFAULT_URL, DEFAULT_PORT))
            serverConfiguration.sshTunnelingConfiguration = SshTunnelingConfiguration.EMPTY
            return serverConfiguration
        }

        fun extractHostAndPort(serverUrl: String): HostAndPort {
            val hostAndPort = StringUtils.split(serverUrl, ":")
            return HostAndPort(hostAndPort[0], hostAndPort[1].toInt())
        }
    }
}