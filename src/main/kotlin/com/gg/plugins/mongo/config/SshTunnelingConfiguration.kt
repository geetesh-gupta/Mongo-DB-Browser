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

import com.gg.plugins.mongo.config.ssh.AuthenticationMethod
import java.util.Objects

class SshTunnelingConfiguration : Cloneable {
    var proxyUrl: String?
    var proxyUser: String?
    var authenticationMethod: AuthenticationMethod
    var proxyPassword: String?
    var privateKeyPath: String?

    private constructor() {
        proxyUrl = null
        proxyUser = null
        authenticationMethod = AuthenticationMethod.PRIVATE_KEY
        privateKeyPath = null
        proxyPassword = null
    }

    constructor(
        proxyUrl: String?,
        proxyUser: String?,
        authenticationMethod: AuthenticationMethod,
        privateKeyPath: String?,
        proxyPassword: String?
    ) {
        this.proxyUrl = proxyUrl
        this.proxyUser = proxyUser
        this.authenticationMethod = authenticationMethod
        this.privateKeyPath = privateKeyPath
        this.proxyPassword = proxyPassword
    }

    override fun hashCode(): Int {
        return Objects.hash(proxyUrl, proxyUser, authenticationMethod, proxyPassword, privateKeyPath)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SshTunnelingConfiguration) return false
        val that = other
        return proxyUrl == that.proxyUrl && proxyUser == that.proxyUser && authenticationMethod == that.authenticationMethod && proxyPassword == that.proxyPassword && privateKeyPath == that.privateKeyPath
    }

    public override fun clone(): ServerConfiguration {
        return super.clone() as ServerConfiguration
    }

    companion object {
        val EMPTY = SshTunnelingConfiguration()
        fun isEmpty(sshTunnelingConfiguration: SshTunnelingConfiguration?): Boolean {
            return sshTunnelingConfiguration == null || EMPTY == sshTunnelingConfiguration
        }
    }
}