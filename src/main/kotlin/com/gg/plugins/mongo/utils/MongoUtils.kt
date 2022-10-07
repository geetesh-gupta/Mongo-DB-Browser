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
package com.gg.plugins.mongo.utils

import com.gg.plugins.mongo.config.ServerConfiguration
import com.gg.plugins.mongo.model.MongoDatabase
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.vfs.CharsetToolkit
import com.mongodb.*
import org.apache.commons.lang.StringUtils
import org.bson.Document
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.DocumentCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.json.JsonWriterSettings
import java.util.*

object MongoUtils {
    val DOCUMENT_CODEC = DocumentCodec(
        CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry()),
        BsonTypeClassMap()
    )
    val WRITER_SETTINGS: JsonWriterSettings = JsonWriterSettings.builder().indent(true).build()

    @Throws(ExecutionException::class)
    fun checkMongoShellPath(mongoShellPath: String?): Boolean {
        if (StringUtils.isBlank(mongoShellPath)) {
            return false
        }
        val commandLine = GeneralCommandLine()
        commandLine.exePath = mongoShellPath!!
        commandLine.addParameter("--version")
        val handler = CapturingProcessHandler(
            commandLine.createProcess(),
            CharsetToolkit.getDefaultSystemCharset(),
            commandLine.commandLineString
        )
        val result = handler.runProcess(15 * 1000)
        return result.exitCode == 0
    }

    fun buildCommandLine(
        shellPath: String?,
        serverConfiguration: ServerConfiguration,
        database: MongoDatabase?
    ): GeneralCommandLine {
        val commandLine = GeneralCommandLine()
        commandLine.exePath = shellPath!!
        commandLine.addParameter(buildMongoUrl(serverConfiguration, database))
        val username = serverConfiguration.username
        if (StringUtils.isNotBlank(username)) {
            commandLine.addParameter("--username")
            commandLine.addParameter(username!!)
        }
        val password = serverConfiguration.password
        if (StringUtils.isNotBlank(password)) {
            commandLine.addParameter("--password")
            commandLine.addParameter(password!!)
        }
        val authenticationDatabase = serverConfiguration.authenticationDatabase
        if (StringUtils.isNotBlank(authenticationDatabase)) {
            commandLine.addParameter("--authenticationDatabase")
            commandLine.addParameter(authenticationDatabase!!)
        }
        val authenticationMechanism = serverConfiguration.authenticationMechanism
        if (authenticationMechanism != null) {
            commandLine.addParameter("--authenticationMechanism")
            commandLine.addParameter(authenticationMechanism.mechanismName)
        }
        val shellWorkingDir = serverConfiguration.shellWorkingDir
        if (StringUtils.isNotBlank(shellWorkingDir)) {
            commandLine.setWorkDirectory(shellWorkingDir)
        }
        val shellArgumentsLine = serverConfiguration.shellArgumentsLine
        if (StringUtils.isNotBlank(shellArgumentsLine)) {
            commandLine.addParameters(*shellArgumentsLine!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
        }
        return commandLine
    }

    private fun buildMongoUrl(serverConfiguration: ServerConfiguration, database: MongoDatabase?): String {
        return String.format(
            "%s/%s",
            serverConfiguration.serverUrls[0],
            database?.name ?: "test"
        )
    }

    fun stringifyList(list: List<*>?): String {
        val stringifiedObjects: MutableList<String?> = LinkedList()
        for (`object` in list!!) {
            if (`object` == null) {
                stringifiedObjects.add("null")
            } else if (`object` is String) {
                stringifiedObjects.add("\"" + `object` + "\"")
            } else if (`object` is Document) {
                stringifiedObjects.add(`object`.toJson(DOCUMENT_CODEC))
            } else if (`object` is List<*>) {
                stringifiedObjects.add(stringifyList(`object` as List<*>?))
            } else {
                stringifiedObjects.add(`object`.toString())
            }
        }
        return "[" + StringUtils.join(stringifiedObjects, ", ") + "]"
    }
}